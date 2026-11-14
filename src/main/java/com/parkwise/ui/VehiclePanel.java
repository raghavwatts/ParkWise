package com.parkwise.ui;

import com.parkwise.exception.DuplicateVehicleException;
import com.parkwise.model.Vehicle;
import com.parkwise.model.VehicleType;
import com.parkwise.service.VehicleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vehicle management screen.
 * Supports add, search, update, and view operations.
 */
public class VehiclePanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(VehiclePanel.class.getName());

    private final VehicleService vehicleService = new VehicleService();

    private JTable          table;
    private DefaultTableModel tableModel;
    private JTextField      searchField;
    private List<Vehicle>   currentList;

    public VehiclePanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIHelper.titleLabel("🚗  Vehicle Management"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        searchField = UIHelper.textField(20);
        searchField.setToolTipText("Search by registration number or owner name");
        JButton searchBtn = UIHelper.primaryButton("🔍 Search");
        JButton addBtn    = UIHelper.successButton("+ Add Vehicle");
        JButton editBtn   = UIHelper.warningButton("✏ Edit");
        JButton deleteBtn = UIHelper.dangerButton("🗑 Delete");
        JButton refreshBtn= UIHelper.primaryButton("🔄 Refresh");
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        // Table
        String[] cols = {"ID", "Registration No.", "Type", "Owner Name", "Contact", "Registered On"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDE2E9)));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll,  BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Wire actions
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        deleteBtn.addActionListener(e -> doDelete());
        refreshBtn.addActionListener(e -> refresh());
    }

    // ── Data operations ───────────────────────────────────────────────────────

    public void refresh() {
        SwingWorker<List<Vehicle>, Void> w = new SwingWorker<>() {
            @Override protected List<Vehicle> doInBackground() throws Exception {
                return vehicleService.getAllVehicles();
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { UIHelper.showError(VehiclePanel.this, e.getMessage()); }
            }
        };
        w.execute();
    }

    private void doSearch() {
        String kw = searchField.getText().trim();
        SwingWorker<List<Vehicle>, Void> w = new SwingWorker<>() {
            @Override protected List<Vehicle> doInBackground() throws Exception {
                return vehicleService.search(kw);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { UIHelper.showError(VehiclePanel.this, e.getMessage()); }
            }
        };
        w.execute();
    }

    private void populateTable(List<Vehicle> vehicles) {
        this.currentList = vehicles;
        tableModel.setRowCount(0);
        java.time.format.DateTimeFormatter fmt =
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Stream + forEach with lambda
        vehicles.forEach(v -> tableModel.addRow(new Object[]{
            v.getId(),
            v.getRegistrationNumber(),
            v.getVehicleType().getDisplayName(),
            v.getOwnerName(),
            v.getContactNumber(),
            v.getCreatedAt() != null ? v.getCreatedAt().format(fmt) : ""
        }));
    }

    // ── Add/Edit dialogs ──────────────────────────────────────────────────────

    private void showAddDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Add New Vehicle", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(480, 320);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField regField   = UIHelper.textField(20);
        JTextField ownerField = UIHelper.textField(20);
        JTextField contactField= UIHelper.textField(20);
        JComboBox<VehicleType> typeCombo = new JComboBox<>(VehicleType.values());
        typeCombo.setFont(UIConstants.FONT_BODY);

        form.add(UIHelper.formRow("Registration No. *:", regField));   form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Vehicle Type *:",     typeCombo));  form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Owner Name *:",       ownerField)); form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Contact No. *:",      contactField));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save   = UIHelper.successButton("Save");
        JButton cancel = UIHelper.dangerButton("Cancel");
        buttons.add(cancel);
        buttons.add(save);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(buttons, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                Vehicle v = vehicleService.addVehicle(
                    regField.getText(),
                    (VehicleType) typeCombo.getSelectedItem(),
                    ownerField.getText(),
                    contactField.getText());
                UIHelper.showInfo(dlg, "Vehicle added successfully!\nID: " + v.getId());
                dlg.dispose();
                refresh();
            } catch (DuplicateVehicleException ex) {
                UIHelper.showError(dlg, ex.getMessage());
            } catch (Exception ex) {
                UIHelper.showError(dlg, "Error: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a vehicle to edit."); return; }
        Vehicle v = currentList.get(table.convertRowIndexToModel(row));

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Vehicle", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(480, 260);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel regLabel = UIHelper.bodyLabel(v.getRegistrationNumber() + " (cannot change)");
        JTextField ownerField = UIHelper.textField(20);
        ownerField.setText(v.getOwnerName());
        JTextField contactField = UIHelper.textField(20);
        contactField.setText(v.getContactNumber());
        JComboBox<VehicleType> typeCombo = new JComboBox<>(VehicleType.values());
        typeCombo.setSelectedItem(v.getVehicleType());
        typeCombo.setFont(UIConstants.FONT_BODY);

        form.add(UIHelper.formRow("Registration:", regLabel));   form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Type:",         typeCombo));  form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Owner Name:",   ownerField)); form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Contact No.:",  contactField));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = UIHelper.successButton("Update");
        JButton cancel = UIHelper.dangerButton("Cancel");
        btns.add(cancel); btns.add(save);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                v.setOwnerName(ownerField.getText());
                v.setContactNumber(contactField.getText());
                v.setVehicleType((VehicleType) typeCombo.getSelectedItem());
                vehicleService.updateVehicle(v);
                UIHelper.showInfo(dlg, "Vehicle updated.");
                dlg.dispose();
                refresh();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a vehicle to delete."); return; }
        Vehicle v = currentList.get(table.convertRowIndexToModel(row));
        if (!UIHelper.confirm(this, "Delete vehicle " + v.getRegistrationNumber() + "?\n"
                + "This will fail if the vehicle has parking history.")) return;
        try {
            vehicleService.deleteVehicle(v.getId());
            refresh();
        } catch (Exception e) {
            UIHelper.showError(this, "Cannot delete: " + e.getMessage());
        }
    }
}
