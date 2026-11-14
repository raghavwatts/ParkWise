package com.parkwise.ui;

import com.parkwise.exception.InvalidParkingSessionException;
import com.parkwise.exception.SlotNotAvailableException;
import com.parkwise.exception.VehicleNotFoundException;
import com.parkwise.model.Payment;
import com.parkwise.model.ParkingSession;
import com.parkwise.model.Vehicle;
import com.parkwise.service.AllocationService;
import com.parkwise.service.ParkingService;
import com.parkwise.service.TariffService;
import com.parkwise.service.VehicleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vehicle Entry / Exit screen.
 * Left panel: Entry (allocate slot). Right panel: Exit (checkout + receipt).
 * Bottom table: currently active sessions.
 */
public class EntryExitPanel extends JPanel {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final VehicleService    vehicleService    = new VehicleService();
    private final AllocationService allocationService = new AllocationService();
    private final ParkingService    parkingService    = new ParkingService(new TariffService());

    // Entry widgets
    private JTextField entryRegField;
    private JLabel     entryStatusLabel;

    // Exit widgets
    private JTextField exitRegField;
    private JComboBox<Payment.PaymentMethod> paymentMethodCombo;
    private JLabel     exitStatusLabel;

    // Active sessions table
    private JTable            activeTable;
    private DefaultTableModel activeModel;

    public EntryExitPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(UIHelper.titleLabel("🔑  Vehicle Entry / Exit"), BorderLayout.NORTH);

        // Top: entry + exit panels side by side
        JPanel topRow = new JPanel(new GridLayout(1, 2, 16, 0));
        topRow.setOpaque(false);
        topRow.add(buildEntryCard());
        topRow.add(buildExitCard());
        add(topRow, BorderLayout.CENTER);

        // Bottom: active sessions
        add(buildActiveSessionsPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildEntryCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = UIHelper.bodyLabel("🟢  VEHICLE ENTRY");
        title.setFont(UIConstants.FONT_SUBTITLE);
        title.setForeground(UIConstants.SUCCESS);
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        entryRegField = UIHelper.textField(20);
        entryRegField.setToolTipText("Enter vehicle registration number");
        card.add(UIHelper.formRow("Registration No.:", entryRegField));
        card.add(Box.createVerticalStrut(12));

        JButton entryBtn = UIHelper.successButton("✔ Enter & Allocate Slot");
        entryBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        entryBtn.addActionListener(e -> doEntry());
        card.add(entryBtn);
        card.add(Box.createVerticalStrut(8));

        entryStatusLabel = new JLabel(" ");
        entryStatusLabel.setFont(UIConstants.FONT_BODY);
        card.add(entryStatusLabel);
        return card;
    }

    private JPanel buildExitCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = UIHelper.bodyLabel("🔴  VEHICLE EXIT");
        title.setFont(UIConstants.FONT_SUBTITLE);
        title.setForeground(UIConstants.DANGER);
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        exitRegField = UIHelper.textField(20);
        card.add(UIHelper.formRow("Registration No.:", exitRegField));
        card.add(Box.createVerticalStrut(8));

        paymentMethodCombo = new JComboBox<>(Payment.PaymentMethod.values());
        paymentMethodCombo.setFont(UIConstants.FONT_BODY);
        card.add(UIHelper.formRow("Payment Method:", paymentMethodCombo));
        card.add(Box.createVerticalStrut(12));

        JButton exitBtn = UIHelper.dangerButton("✔ Exit & Checkout");
        exitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        exitBtn.addActionListener(e -> doExit());
        card.add(exitBtn);
        card.add(Box.createVerticalStrut(8));

        exitStatusLabel = new JLabel(" ");
        exitStatusLabel.setFont(UIConstants.FONT_BODY);
        card.add(exitStatusLabel);
        return card;
    }

    private JPanel buildActiveSessionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 260));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(UIHelper.bodyLabel("Currently Parked Vehicles"), BorderLayout.WEST);
        JButton refreshBtn = UIHelper.primaryButton("🔄");
        refreshBtn.setPreferredSize(new Dimension(60, 28));
        refreshBtn.addActionListener(e -> refresh());
        hdr.add(refreshBtn, BorderLayout.EAST);
        panel.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Session ID","Registration","Type","Slot","Entry Time"};
        activeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        activeTable = new JTable(activeModel);
        UIHelper.styleTable(activeTable);
        JScrollPane scroll = new JScrollPane(activeTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDE2E9)));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Entry ─────────────────────────────────────────────────────────────────

    private void doEntry() {
        String reg = entryRegField.getText().trim().toUpperCase();
        if (reg.isEmpty()) { entryStatusLabel.setText("⚠ Enter a registration number."); return; }

        SwingWorker<ParkingSession, Void> w = new SwingWorker<>() {
            String errorMsg;
            @Override protected ParkingSession doInBackground() throws Exception {
                Vehicle v = vehicleService.findByRegistrationNumber(reg);
                return allocationService.allocateAndEnter(v);
            }
            @Override protected void done() {
                try {
                    ParkingSession s = get();
                    entryStatusLabel.setForeground(UIConstants.SUCCESS);
                    entryStatusLabel.setText("✔ Allocated slot: " + s.getSlotNumber()
                            + " | Session ID: " + s.getId());
                    entryRegField.setText("");
                    refresh();
                } catch (Exception e) {
                    entryStatusLabel.setForeground(UIConstants.DANGER);
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    entryStatusLabel.setText("✘ " + cause.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── Exit ──────────────────────────────────────────────────────────────────

    private void doExit() {
        String reg = exitRegField.getText().trim().toUpperCase();
        if (reg.isEmpty()) { exitStatusLabel.setText("⚠ Enter a registration number."); return; }
        Payment.PaymentMethod method =
                (Payment.PaymentMethod) paymentMethodCombo.getSelectedItem();

        SwingWorker<ParkingSession, Void> w = new SwingWorker<>() {
            @Override protected ParkingSession doInBackground() throws Exception {
                Vehicle v = vehicleService.findByRegistrationNumber(reg);
                ParkingSession active = parkingService.getActiveSession(v.getId())
                        .orElseThrow(() -> new InvalidParkingSessionException(
                            reg + " has no active parking session."));
                return parkingService.processExit(active.getId(), method);
            }
            @Override protected void done() {
                try {
                    ParkingSession s = get();
                    showReceipt(s);
                    exitStatusLabel.setForeground(UIConstants.SUCCESS);
                    exitStatusLabel.setText("✔ Exit processed. Fee: ₹" + s.getFeeAmount());
                    exitRegField.setText("");
                    refresh();
                } catch (Exception e) {
                    exitStatusLabel.setForeground(UIConstants.DANGER);
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    exitStatusLabel.setText("✘ " + cause.getMessage());
                }
            }
        };
        w.execute();
    }

    /** Displays a parking receipt dialog. */
    private void showReceipt(ParkingSession s) {
        int hours = (s.getDurationMinutes() != null ? s.getDurationMinutes() : 0) / 60;
        int mins  = (s.getDurationMinutes() != null ? s.getDurationMinutes() : 0) % 60;
        String receipt = """
                ═══════════════════════════════
                       PARKWISE RECEIPT
                ═══════════════════════════════
                Session ID   : %d
                Vehicle      : %s
                Type         : %s
                Slot         : %s
                ───────────────────────────────
                Entry        : %s
                Exit         : %s
                Duration     : %dh %dm
                ───────────────────────────────
                Fee Amount   : ₹%s
                Payment      : PAID
                ═══════════════════════════════
                       Thank you! Drive safe.
                ═══════════════════════════════
                """.formatted(
                s.getId(),
                s.getRegistrationNumber(),
                s.getVehicleType() != null ? s.getVehicleType().getDisplayName() : "",
                s.getSlotNumber(),
                s.getEntryTime() != null ? s.getEntryTime().format(FMT) : "",
                s.getExitTime()  != null ? s.getExitTime().format(FMT)  : "",
                hours, mins,
                s.getFeeAmount() != null ? s.getFeeAmount().toPlainString() : "0.00"
        );

        JTextArea ta = new JTextArea(receipt);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
                "Parking Receipt — Session #" + s.getId(), JOptionPane.PLAIN_MESSAGE);
    }

    public void refresh() {
        SwingWorker<List<ParkingSession>, Void> w = new SwingWorker<>() {
            @Override protected List<ParkingSession> doInBackground() throws Exception {
                return parkingService.getAllActiveSessions();
            }
            @Override protected void done() {
                try {
                    List<ParkingSession> sessions = get();
                    activeModel.setRowCount(0);
                    sessions.forEach(s -> activeModel.addRow(new Object[]{
                        s.getId(),
                        s.getRegistrationNumber(),
                        s.getVehicleType() != null ? s.getVehicleType().getDisplayName() : "",
                        s.getSlotNumber(),
                        s.getEntryTime() != null ? s.getEntryTime().format(FMT) : ""
                    }));
                } catch (Exception e) {
                    /* ignore during background refresh */
                }
            }
        };
        w.execute();
    }
}
