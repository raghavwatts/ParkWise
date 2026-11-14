package com.parkwise.ui;

import com.parkwise.exception.ReservationOverlapException;
import com.parkwise.exception.SlotNotAvailableException;
import com.parkwise.model.Reservation;
import com.parkwise.model.VehicleType;
import com.parkwise.service.ParkingSlotService;
import com.parkwise.service.ReservationService;
import com.parkwise.service.VehicleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Reservation management screen.
 */
public class ReservationPanel extends JPanel {

    private final ReservationService reservationService = new ReservationService();
    private final VehicleService     vehicleService     = new VehicleService();
    private final ParkingSlotService slotService        = new ParkingSlotService();

    private JTable             table;
    private DefaultTableModel  tableModel;
    private List<Reservation>  currentList;

    public ReservationPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(UIHelper.titleLabel("📅  Reservation Management"), BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        JButton addBtn     = UIHelper.successButton("+ New Reservation");
        JButton cancelBtn  = UIHelper.dangerButton("✘ Cancel");
        JButton refreshBtn = UIHelper.primaryButton("🔄 Refresh");
        toolbar.add(addBtn);
        toolbar.add(cancelBtn);
        toolbar.add(refreshBtn);

        String[] cols = {"ID","Vehicle","Owner","Slot","Start Time","End Time","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDE2E9)));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll,  BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> showAddDialog());
        cancelBtn.addActionListener(e -> doCancel());
        refreshBtn.addActionListener(e -> refresh());
    }

    public void refresh() {
        SwingWorker<List<Reservation>, Void> w = new SwingWorker<>() {
            @Override protected List<Reservation> doInBackground() throws Exception {
                return reservationService.getAllReservations();
            }
            @Override protected void done() {
                try { populate(get()); }
                catch (Exception e) { UIHelper.showError(ReservationPanel.this, e.getMessage()); }
            }
        };
        w.execute();
    }

    private void populate(List<Reservation> list) {
        this.currentList = list;
        tableModel.setRowCount(0);
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        list.forEach(r -> tableModel.addRow(new Object[]{
            r.getId(),
            r.getRegistrationNumber(),
            r.getOwnerName(),
            r.getSlotNumber(),
            r.getStartTime().format(fmt),
            r.getEndTime().format(fmt),
            r.getReservationStatus().name()
        }));
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "New Reservation", true);
        dlg.setSize(480, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField regField   = UIHelper.textField(15);
        JTextField startField = UIHelper.textField(15);
        JTextField endField   = UIHelper.textField(15);
        startField.setToolTipText("dd-MM-yyyy HH:mm");
        endField.setToolTipText("dd-MM-yyyy HH:mm");

        JLabel hint = new JLabel("Enter 0 for slot ID to auto-allocate");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(Color.GRAY);
        JTextField slotIdField = UIHelper.textField(5);
        slotIdField.setText("0");

        form.add(UIHelper.formRow("Vehicle Reg. No. *:", regField));   form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Start (dd-MM-yyyy HH:mm):", startField)); form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("End (dd-MM-yyyy HH:mm):",   endField));   form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Slot ID (0 = auto):", slotIdField));
        form.add(Box.createVerticalStrut(4));
        form.add(hint);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save   = UIHelper.successButton("Create");
        JButton cancel = UIHelper.dangerButton("Cancel");
        btns.add(cancel); btns.add(save);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                java.time.format.DateTimeFormatter fmt =
                        java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                LocalDateTime start = LocalDateTime.parse(startField.getText().trim(), fmt);
                LocalDateTime end   = LocalDateTime.parse(endField.getText().trim(), fmt);
                int vehicleId = vehicleService
                        .findByRegistrationNumber(regField.getText().trim()).getId();
                int slotId = Integer.parseInt(slotIdField.getText().trim());
                reservationService.createReservation(vehicleId, slotId, start, end);
                UIHelper.showInfo(dlg, "Reservation created successfully!");
                dlg.dispose();
                refresh();
            } catch (ReservationOverlapException | SlotNotAvailableException ex) {
                UIHelper.showError(dlg, ex.getMessage());
            } catch (Exception ex) {
                UIHelper.showError(dlg, "Error: " + ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }

    private void doCancel() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a reservation to cancel."); return; }
        Reservation r = currentList.get(table.convertRowIndexToModel(row));
        if (r.getReservationStatus() != Reservation.ReservationStatus.CONFIRMED) {
            UIHelper.showError(this, "Only CONFIRMED reservations can be cancelled.");
            return;
        }
        if (!UIHelper.confirm(this, "Cancel reservation #" + r.getId() + "?")) return;
        try {
            reservationService.cancelReservation(r.getId());
            refresh();
        } catch (Exception e) {
            UIHelper.showError(this, e.getMessage());
        }
    }
}
