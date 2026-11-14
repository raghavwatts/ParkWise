package com.parkwise.ui;

import com.parkwise.model.Tariff;
import com.parkwise.service.TariffService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;

/**
 * Tariff configuration screen.
 */
public class TariffPanel extends JPanel {

    private final TariffService tariffService = new TariffService();

    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Tariff>      currentList;

    public TariffPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new java.awt.BorderLayout());
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(UIHelper.titleLabel("💰  Tariff Configuration"), java.awt.BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        JButton editBtn    = UIHelper.warningButton("✏ Edit Tariff");
        JButton refreshBtn = UIHelper.primaryButton("🔄 Refresh");
        toolbar.add(editBtn);
        toolbar.add(refreshBtn);

        String[] cols = {"ID","Vehicle Type","1st Hour Fee (₹)","Additional Hour (₹)","Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xDDE2E9)));

        JPanel center = new JPanel(new java.awt.BorderLayout());
        center.setOpaque(false);
        center.add(toolbar, java.awt.BorderLayout.NORTH);
        center.add(scroll,  java.awt.BorderLayout.CENTER);
        add(center, java.awt.BorderLayout.CENTER);

        editBtn.addActionListener(e -> showEditDialog());
        refreshBtn.addActionListener(e -> refresh());
    }

    public void refresh() {
        SwingWorker<List<Tariff>, Void> w = new SwingWorker<>() {
            @Override protected List<Tariff> doInBackground() throws Exception {
                return tariffService.getAllTariffs();
            }
            @Override protected void done() {
                try {
                    currentList = get();
                    tableModel.setRowCount(0);
                    currentList.forEach(t -> tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getVehicleType().getDisplayName(),
                        t.getFirstHourFee().toPlainString(),
                        t.getAdditionalHourFee().toPlainString(),
                        t.isActive() ? "Yes" : "No"
                    }));
                } catch (Exception e) {
                    UIHelper.showError(TariffPanel.this, e.getMessage());
                }
            }
        };
        w.execute();
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a tariff to edit."); return; }
        Tariff t = currentList.get(table.convertRowIndexToModel(row));

        JDialog dlg = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Tariff", true);
        dlg.setSize(380, 220);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new java.awt.BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField firstFeeField = UIHelper.textField(10);
        firstFeeField.setText(t.getFirstHourFee().toPlainString());
        JTextField addFeeField = UIHelper.textField(10);
        addFeeField.setText(t.getAdditionalHourFee().toPlainString());

        form.add(UIHelper.bodyLabel("Vehicle Type: " + t.getVehicleType().getDisplayName()));
        form.add(Box.createVerticalStrut(12));
        form.add(UIHelper.formRow("1st Hour Fee (₹):",    firstFeeField)); form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Additional Hour (₹):", addFeeField));

        JPanel btns = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JButton save = UIHelper.successButton("Save");
        JButton cancel = UIHelper.dangerButton("Cancel");
        btns.add(cancel); btns.add(save);
        dlg.add(form, java.awt.BorderLayout.CENTER);
        dlg.add(btns, java.awt.BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                t.setFirstHourFee(new BigDecimal(firstFeeField.getText().trim()));
                t.setAdditionalHourFee(new BigDecimal(addFeeField.getText().trim()));
                tariffService.updateTariff(t);
                UIHelper.showInfo(dlg, "Tariff updated.");
                dlg.dispose();
                refresh();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }
}
