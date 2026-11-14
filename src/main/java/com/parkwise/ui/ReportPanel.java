package com.parkwise.ui;

import com.parkwise.model.ParkingSession;
import com.parkwise.report.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reports and analytics screen.
 */
public class ReportPanel extends JPanel {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ReportService reportService = new ReportService();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        fromField, toField;
    private JLabel            statusLabel;

    public ReportPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(UIHelper.titleLabel("📈  Reports & Analytics"), BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);

        fromField = UIHelper.textField(10);
        fromField.setText(LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        fromField.setToolTipText("dd-MM-yyyy");
        toField = UIHelper.textField(10);
        toField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        JButton histBtn    = UIHelper.primaryButton("Parking History");
        JButton revenueBtn = UIHelper.successButton("Daily Revenue");
        JButton slotsBtn   = UIHelper.warningButton("Most Used Slots");
        JButton exportBtn  = UIHelper.primaryButton("📥 Export CSV");

        toolbar.add(new JLabel("From:")); toolbar.add(fromField);
        toolbar.add(new JLabel("To:"));   toolbar.add(toField);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(histBtn);
        toolbar.add(revenueBtn);
        toolbar.add(slotsBtn);
        toolbar.add(exportBtn);

        // Table
        tableModel = new DefaultTableModel(new String[]{"—"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDE2E9)));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(Color.GRAY);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll,  BorderLayout.CENTER);
        center.add(statusLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Wire buttons
        histBtn.addActionListener(e -> loadParkingHistory());
        revenueBtn.addActionListener(e -> loadDailyRevenue());
        slotsBtn.addActionListener(e -> loadMostUsedSlots());
        exportBtn.addActionListener(e -> exportCurrentView());
    }

    public void refresh() { loadParkingHistory(); }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    // ── Parking history ───────────────────────────────────────────────────────

    private void loadParkingHistory() {
        LocalDate from = parseDate(fromField.getText().trim());
        LocalDate to   = parseDate(toField.getText().trim());
        statusLabel.setText("Loading…");

        SwingWorker<List<ParkingSession>, Void> w = new SwingWorker<>() {
            @Override protected List<ParkingSession> doInBackground() throws Exception {
                return reportService.getParkingHistory(from, to);
            }
            @Override protected void done() {
                try {
                    List<ParkingSession> sessions = get();
                    String[] cols = {"ID","Registration","Type","Slot","Entry","Exit","Duration (min)","Fee (₹)","Payment","Status"};
                    tableModel.setColumnIdentifiers(cols);
                    tableModel.setRowCount(0);
                    sessions.forEach(s -> tableModel.addRow(new Object[]{
                        s.getId(), s.getRegistrationNumber(),
                        s.getVehicleType() != null ? s.getVehicleType().getDisplayName() : "",
                        s.getSlotNumber(),
                        s.getEntryTime() != null ? s.getEntryTime().format(FMT) : "",
                        s.getExitTime()  != null ? s.getExitTime().format(FMT)  : "",
                        s.getDurationMinutes() != null ? s.getDurationMinutes() : "",
                        s.getFeeAmount()       != null ? s.getFeeAmount().toPlainString() : "",
                        s.getPaymentStatus().name(),
                        s.getSessionStatus().name()
                    }));
                    statusLabel.setText("Showing " + sessions.size() + " sessions from " + from + " to " + to);
                } catch (Exception e) {
                    UIHelper.showError(ReportPanel.this, e.getMessage());
                    statusLabel.setText("Error loading report");
                }
            }
        };
        w.execute();
    }

    // ── Daily revenue ─────────────────────────────────────────────────────────

    private void loadDailyRevenue() {
        LocalDate from = parseDate(fromField.getText().trim());
        LocalDate to   = parseDate(toField.getText().trim());

        SwingWorker<TreeMap<LocalDate, java.math.BigDecimal>, Void> w = new SwingWorker<>() {
            @Override protected TreeMap<LocalDate, java.math.BigDecimal> doInBackground() throws Exception {
                return reportService.getDailyRevenue(from, to);
            }
            @Override protected void done() {
                try {
                    TreeMap<LocalDate, java.math.BigDecimal> data = get();
                    String[] cols = {"Date", "Revenue (₹)"};
                    tableModel.setColumnIdentifiers(cols);
                    tableModel.setRowCount(0);
                    data.forEach((date, amount) ->
                        tableModel.addRow(new Object[]{date.toString(), amount.toPlainString()})
                    );
                    statusLabel.setText("Daily revenue — " + data.size() + " days");
                } catch (Exception e) {
                    UIHelper.showError(ReportPanel.this, e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── Most used slots ───────────────────────────────────────────────────────

    private void loadMostUsedSlots() {
        SwingWorker<List<Map.Entry<String, Long>>, Void> w = new SwingWorker<>() {
            @Override protected List<Map.Entry<String, Long>> doInBackground() throws Exception {
                return reportService.getMostUsedSlots(10);
            }
            @Override protected void done() {
                try {
                    List<Map.Entry<String, Long>> data = get();
                    String[] cols = {"Slot No.", "Sessions Count"};
                    tableModel.setColumnIdentifiers(cols);
                    tableModel.setRowCount(0);
                    data.forEach(e -> tableModel.addRow(new Object[]{e.getKey(), e.getValue()}));
                    statusLabel.setText("Top " + data.size() + " most-used slots");
                } catch (Exception e) {
                    UIHelper.showError(ReportPanel.this, e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── CSV export ────────────────────────────────────────────────────────────

    private void exportCurrentView() {
        LocalDate from = parseDate(fromField.getText().trim());
        LocalDate to   = parseDate(toField.getText().trim());
        statusLabel.setText("Exporting…");

        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                List<ParkingSession> sessions = reportService.getParkingHistory(from, to);
                String filename = "parking_history_" + from + "_to_" + to + ".csv";
                return reportService.exportSessionsToCsv(sessions, filename);
            }
            @Override protected void done() {
                try {
                    String path = get();
                    UIHelper.showInfo(ReportPanel.this, "CSV exported to:\n" + path);
                    statusLabel.setText("Exported: " + path);
                } catch (Exception e) {
                    UIHelper.showError(ReportPanel.this, "Export failed: " + e.getMessage());
                    statusLabel.setText("Export error");
                }
            }
        };
        w.execute();
    }
}
