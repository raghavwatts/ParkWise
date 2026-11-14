package com.parkwise.report;

import com.parkwise.dao.ParkingSessionDAO;
import com.parkwise.dao.PaymentDAO;
import com.parkwise.dao.ReservationDAO;
import com.parkwise.model.ParkingSession;
import com.parkwise.model.Payment;
import com.parkwise.model.Reservation;
import com.parkwise.util.AppConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report generation service. Provides parking history, revenue reports,
 * slot utilization stats, and CSV export.
 */
public class ReportService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT);

    private final ParkingSessionDAO sessionDAO = new ParkingSessionDAO();
    private final ReservationDAO    reservationDAO = new ReservationDAO();
    private final PaymentDAO        paymentDAO = new PaymentDAO();

    // ── Report data methods ───────────────────────────────────────────────────

    /**
     * Returns all sessions in a date range, sorted by entry time.
     */
    public List<ParkingSession> getParkingHistory(LocalDate from, LocalDate to) throws SQLException {
        // Stream: filter + sorted
        return sessionDAO.findByDateRange(from, to).stream()
                .sorted(Comparator.comparing(ParkingSession::getEntryTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns sessions with duration > thresholdMinutes (long-duration parking).
     */
    public List<ParkingSession> getLongDurationSessions(int thresholdMinutes) throws SQLException {
        return sessionDAO.findAll().stream()
                .filter(s -> s.getDurationMinutes() != null
                          && s.getDurationMinutes() > thresholdMinutes)
                .sorted(Comparator.comparingInt(ParkingSession::getDurationMinutes).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns daily revenue grouped by date, sorted by date ascending.
     * Uses TreeMap to keep dates sorted.
     */
    public TreeMap<LocalDate, BigDecimal> getDailyRevenue(LocalDate from, LocalDate to)
            throws SQLException {
        List<ParkingSession> sessions = sessionDAO.findByDateRange(from, to);

        // Stream groupingBy — returns sorted TreeMap
        return sessions.stream()
                .filter(s -> s.getFeeAmount() != null
                          && s.getPaymentStatus() == ParkingSession.PaymentStatus.PAID
                          && s.getExitTime() != null)
                .collect(Collectors.groupingBy(
                    s -> s.getExitTime().toLocalDate(),
                    TreeMap::new,
                    Collectors.reducing(BigDecimal.ZERO,
                                        ParkingSession::getFeeAmount,
                                        BigDecimal::add)
                ));
    }

    /**
     * Returns monthly revenue grouped by year-month string.
     */
    public TreeMap<String, BigDecimal> getMonthlyRevenue() throws SQLException {
        List<ParkingSession> sessions = sessionDAO.findAll();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        return sessions.stream()
                .filter(s -> s.getFeeAmount() != null
                          && s.getPaymentStatus() == ParkingSession.PaymentStatus.PAID
                          && s.getExitTime() != null)
                .collect(Collectors.groupingBy(
                    s -> s.getExitTime().format(monthFmt),
                    TreeMap::new,
                    Collectors.reducing(BigDecimal.ZERO,
                                        ParkingSession::getFeeAmount,
                                        BigDecimal::add)
                ));
    }

    /**
     * Returns the most frequently used slot numbers and their usage counts.
     */
    public List<Map.Entry<String, Long>> getMostUsedSlots(int topN) throws SQLException {
        Map<String, Long> counts = sessionDAO.findAll().stream()
                .filter(s -> s.getSlotNumber() != null)
                .collect(Collectors.groupingBy(ParkingSession::getSlotNumber, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the average parking duration (minutes) for completed sessions.
     */
    public OptionalDouble getAverageDuration() throws SQLException {
        return sessionDAO.findAll().stream()
                .filter(s -> s.getDurationMinutes() != null
                          && s.getSessionStatus() == ParkingSession.SessionStatus.COMPLETED)
                .mapToInt(ParkingSession::getDurationMinutes)
                .average();
    }

    /**
     * Returns all cancelled reservations.
     */
    public List<Reservation> getCancelledReservations() throws SQLException {
        return reservationDAO.findByStatus(Reservation.ReservationStatus.CANCELLED);
    }

    // ── CSV Export ────────────────────────────────────────────────────────────

    /**
     * Exports a list of parking sessions to a CSV file.
     *
     * @param sessions list of sessions to export
     * @param filename suggested filename (without path)
     * @return the absolute path of the written file
     */
    public String exportSessionsToCsv(List<ParkingSession> sessions, String filename)
            throws IOException {

        File dir = new File(AppConfig.EXPORT_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create export directory: " + dir.getAbsolutePath());
        }
        File output = new File(dir, filename);

        // BufferedWriter wraps FileWriter — character stream (not byte stream)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            // Header
            bw.write("Session ID,Registration,Vehicle Type,Slot,Entry Time,Exit Time,"
                    + "Duration (min),Fee (INR),Payment Status,Session Status");
            bw.newLine();

            // Rows — lambda + method references
            for (ParkingSession s : sessions) {
                bw.write(String.join(",",
                    String.valueOf(s.getId()),
                    csvEscape(s.getRegistrationNumber()),
                    csvEscape(s.getVehicleType() != null ? s.getVehicleType().getDisplayName() : ""),
                    csvEscape(s.getSlotNumber()),
                    s.getEntryTime() != null ? s.getEntryTime().format(FMT) : "",
                    s.getExitTime()  != null ? s.getExitTime().format(FMT)  : "",
                    s.getDurationMinutes() != null ? String.valueOf(s.getDurationMinutes()) : "",
                    s.getFeeAmount() != null ? s.getFeeAmount().toPlainString() : "",
                    s.getPaymentStatus().name(),
                    s.getSessionStatus().name()
                ));
                bw.newLine();
            }
        }

        return output.getAbsolutePath();
    }

    /**
     * Exports daily revenue to CSV.
     */
    public String exportDailyRevenueToCsv(TreeMap<LocalDate, BigDecimal> revenue,
                                           String filename) throws IOException {
        File dir = new File(AppConfig.EXPORT_DIR);
        if (!dir.exists()) dir.mkdirs();
        File output = new File(dir, filename);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            bw.write("Date,Revenue (INR)");
            bw.newLine();
            revenue.forEach((date, amount) -> {
                try {
                    bw.write(date + "," + amount.toPlainString());
                    bw.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return output.getAbsolutePath();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Escapes a CSV field by wrapping in quotes if it contains comma, quote or newline. */
    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
