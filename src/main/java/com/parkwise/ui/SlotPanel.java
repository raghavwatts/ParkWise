package com.parkwise.ui;

import com.parkwise.model.ParkingSlot;
import com.parkwise.model.SlotStatus;
import com.parkwise.model.VehicleType;
import com.parkwise.service.ParkingSlotService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Parking slot management screen.
 */
public class SlotPanel extends JPanel {

    private final ParkingSlotService slotService = new ParkingSlotService();

    private JTable             table;
    private DefaultTableModel  tableModel;
    private JComboBox<String>  filterCombo;
    private List<ParkingSlot>  currentList;

    public SlotPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        add(UIHelper.titleLabel("🅿️  Parking Slot Management"), BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        filterCombo = new JComboBox<>(new String[]{"All","Available","Occupied","Reserved","Maintenance"});
        filterCombo.setFont(UIConstants.FONT_BODY);
        JButton filterBtn  = UIHelper.primaryButton("Filter");
        JButton addBtn     = UIHelper.successButton("+ Add Slot");
        JButton editBtn    = UIHelper.warningButton("✏ Edit");
        JButton refreshBtn = UIHelper.primaryButton("🔄 Refresh");
        toolbar.add(new JLabel("Filter:"));
        toolbar.add(filterCombo);
        toolbar.add(filterBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(refreshBtn);

        // Table
        String[] cols = {"ID","Slot No.","Type","Status","Active"};
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

        // Wire
        filterBtn.addActionListener(e -> applyFilter());
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        refreshBtn.addActionListener(e -> refresh());
    }

    public void refresh() {
        SwingWorker<List<ParkingSlot>, Void> w = new SwingWorker<>() {
            @Override protected List<ParkingSlot> doInBackground() throws Exception {
                return slotService.getAllSlots();
            }
            @Override protected void done() {
                try { populate(get()); }
                catch (Exception e) { UIHelper.showError(SlotPanel.this, e.getMessage()); }
            }
        };
        w.execute();
    }

    private void applyFilter() {
        String sel = (String) filterCombo.getSelectedItem();
        SwingWorker<List<ParkingSlot>, Void> w = new SwingWorker<>() {
            @Override protected List<ParkingSlot> doInBackground() throws Exception {
                return switch (sel) {
                    case "Available"   -> slotService.filterByStatus(SlotStatus.AVAILABLE);
                    case "Occupied"    -> slotService.filterByStatus(SlotStatus.OCCUPIED);
                    case "Reserved"    -> slotService.filterByStatus(SlotStatus.RESERVED);
                    case "Maintenance" -> slotService.filterByStatus(SlotStatus.MAINTENANCE);
                    default            -> slotService.getAllSlots();
                };
            }
            @Override protected void done() {
                try { populate(get()); }
                catch (Exception e) { UIHelper.showError(SlotPanel.this, e.getMessage()); }
            }
        };
        w.execute();
    }

    private void populate(List<ParkingSlot> slots) {
        this.currentList = slots;
        tableModel.setRowCount(0);
        slots.forEach(s -> tableModel.addRow(new Object[]{
            s.getId(), s.getSlotNumber(),
            s.getSlotType().getDisplayName(),
            s.getSlotStatus().getDisplayName(),
            s.isActive() ? "Yes" : "No"
        }));
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Slot", true);
        dlg.setSize(380, 200);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField numField = UIHelper.textField(10);
        JComboBox<VehicleType> typeCombo = new JComboBox<>(VehicleType.values());
        typeCombo.setFont(UIConstants.FONT_BODY);
        form.add(UIHelper.formRow("Slot Number (e.g. A01):", numField)); form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Type:", typeCombo));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = UIHelper.successButton("Save");
        JButton cancel = UIHelper.dangerButton("Cancel");
        btns.add(cancel); btns.add(save);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                slotService.addSlot(numField.getText(), (VehicleType) typeCombo.getSelectedItem());
                UIHelper.showInfo(dlg, "Slot added.");
                dlg.dispose();
                refresh();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a slot to edit."); return; }
        ParkingSlot s = currentList.get(table.convertRowIndexToModel(row));

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Slot", true);
        dlg.setSize(400, 240);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel slotLabel = UIHelper.bodyLabel(s.getSlotNumber());
        JComboBox<VehicleType>  typeCombo   = new JComboBox<>(VehicleType.values());
        JComboBox<SlotStatus>   statusCombo = new JComboBox<>(SlotStatus.values());
        JCheckBox activeCheck = new JCheckBox("Active", s.isActive());
        typeCombo.setSelectedItem(s.getSlotType());
        statusCombo.setSelectedItem(s.getSlotStatus());
        typeCombo.setFont(UIConstants.FONT_BODY);
        statusCombo.setFont(UIConstants.FONT_BODY);

        form.add(UIHelper.formRow("Slot Number:", slotLabel));   form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Type:",        typeCombo));   form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Status:",      statusCombo)); form.add(Box.createVerticalStrut(8));
        form.add(activeCheck);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = UIHelper.successButton("Update");
        JButton cancel = UIHelper.dangerButton("Cancel");
        btns.add(cancel); btns.add(save);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                s.setSlotType((VehicleType) typeCombo.getSelectedItem());
                s.setSlotStatus((SlotStatus) statusCombo.getSelectedItem());
                s.setActive(activeCheck.isSelected());
                slotService.updateSlot(s);
                UIHelper.showInfo(dlg, "Slot updated.");
                dlg.dispose();
                refresh();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }
}
