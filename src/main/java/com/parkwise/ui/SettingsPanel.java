package com.parkwise.ui;

import com.parkwise.backup.BackupService;
import com.parkwise.service.AuthService;
import com.parkwise.service.VehicleService;
import com.parkwise.service.ParkingSlotService;
import com.parkwise.util.AppConfig;
import com.parkwise.util.ConcurrentAllocationSimulator;
import com.parkwise.model.Vehicle;
import com.parkwise.model.VehicleType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings screen for strategy configuration, backup/restore, password management,
 * and concurrency testing.
 */
public class SettingsPanel extends JPanel {

    private final AuthService   authService;
    private final BackupService backupService = new BackupService();

    public SettingsPanel(AuthService authService) {
        this.authService = authService;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(UIHelper.titleLabel("⚙️  Settings & Backup"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(20, 0, 0, 0));
        grid.add(buildStrategyCard());
        grid.add(buildBackupCard());
        grid.add(buildPasswordCard());
        add(grid, BorderLayout.CENTER);

        JPanel bottom = buildConcurrencyCard();
        add(bottom, BorderLayout.SOUTH);
    }

    // ── Allocation strategy card ──────────────────────────────────────────────

    private JPanel buildStrategyCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = UIHelper.bodyLabel("Allocation Strategy");
        title.setFont(UIConstants.FONT_SUBTITLE);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        String[] strategies = {"FIRST_AVAILABLE", "BEST_FIT"};
        JComboBox<String> combo = new JComboBox<>(strategies);
        combo.setFont(UIConstants.FONT_BODY);
        combo.setSelectedItem(AppConfig.getInstance().getAllocationStrategy());
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        card.add(combo);
        card.add(Box.createVerticalStrut(8));

        JButton save = UIHelper.successButton("Apply");
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        save.addActionListener(e -> {
            AppConfig.getInstance().setAllocationStrategy((String) combo.getSelectedItem());
            UIHelper.showInfo(this, "Strategy set to: " + combo.getSelectedItem());
        });
        card.add(save);

        card.add(Box.createVerticalStrut(12));
        JTextArea desc = new JTextArea(
            "FIRST_AVAILABLE: assigns the\nlowest-numbered eligible slot.\n\n"
           + "BEST_FIT: selects the most\nsuitable slot by type matching.");
        desc.setFont(UIConstants.FONT_SMALL);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        card.add(desc);
        return card;
    }

    // ── Backup card ───────────────────────────────────────────────────────────

    private JPanel buildBackupCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = UIHelper.bodyLabel("Backup & Restore");
        title.setFont(UIConstants.FONT_SUBTITLE);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        JButton backupBtn  = UIHelper.primaryButton("💾 Create Backup");
        JButton restoreBtn = UIHelper.warningButton("📂 Restore Backup");
        backupBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        restoreBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(backupBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(restoreBtn);
        card.add(Box.createVerticalStrut(12));

        JTextArea note = new JTextArea(
            "ℹ Backup saves a snapshot of vehicles\nand slots to a .bak file.\n"
           + "For full database backup, also\nrun mysqldump on parkwise_db.");
        note.setFont(UIConstants.FONT_SMALL);
        note.setForeground(UIConstants.WARNING);
        note.setEditable(false);
        note.setOpaque(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        card.add(note);

        backupBtn.addActionListener(e -> doBackup());
        restoreBtn.addActionListener(e -> doRestore());
        return card;
    }

    private void doBackup() {
        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return backupService.createBackup();
            }
            @Override protected void done() {
                try { UIHelper.showInfo(SettingsPanel.this, "Backup created:\n" + get()); }
                catch (Exception e) { UIHelper.showError(SettingsPanel.this, "Backup failed: " + e.getMessage()); }
            }
        };
        w.execute();
    }

    private void doRestore() {
        JFileChooser fc = new JFileChooser(AppConfig.BACKUP_DIR);
        fc.setDialogTitle("Select a .bak backup file");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            SwingWorker<String, Void> w = new SwingWorker<>() {
                @Override protected String doInBackground() throws Exception {
                    var snap = backupService.restoreBackup(path);
                    return "Snapshot from " + snap.getCreatedAt()
                         + "\n  Vehicles: " + snap.getVehicles().size()
                         + "\n  Slots: " + snap.getSlots().size();
                }
                @Override protected void done() {
                    try { UIHelper.showInfo(SettingsPanel.this, "Restore successful:\n" + get()); }
                    catch (Exception e) { UIHelper.showError(SettingsPanel.this, "Restore failed: " + e.getMessage()); }
                }
            };
            w.execute();
        }
    }

    // ── Change password card ──────────────────────────────────────────────────

    private JPanel buildPasswordCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = UIHelper.bodyLabel("Change Password");
        title.setFont(UIConstants.FONT_SUBTITLE);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        JPasswordField oldPwd  = new JPasswordField(15);
        JPasswordField newPwd  = new JPasswordField(15);
        JPasswordField confPwd = new JPasswordField(15);
        oldPwd.setFont(UIConstants.FONT_BODY);
        newPwd.setFont(UIConstants.FONT_BODY);
        confPwd.setFont(UIConstants.FONT_BODY);

        card.add(UIHelper.formRow("Current Password:", oldPwd));  card.add(Box.createVerticalStrut(8));
        card.add(UIHelper.formRow("New Password:",     newPwd));  card.add(Box.createVerticalStrut(8));
        card.add(UIHelper.formRow("Confirm New:",      confPwd)); card.add(Box.createVerticalStrut(12));

        JButton changeBtn = UIHelper.warningButton("Change Password");
        changeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        changeBtn.addActionListener(e -> {
            String np = new String(newPwd.getPassword());
            String cp = new String(confPwd.getPassword());
            if (!np.equals(cp)) { UIHelper.showError(this, "New passwords do not match."); return; }
            try {
                boolean ok = authService.changePassword(
                    new String(oldPwd.getPassword()), np);
                if (ok) UIHelper.showInfo(this, "Password changed successfully.");
                else UIHelper.showError(this, "Current password is incorrect.");
            } catch (Exception ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        });
        card.add(changeBtn);
        return card;
    }

    // ── Concurrency simulation card ───────────────────────────────────────────

    private JPanel buildConcurrencyCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UIConstants.ACCENT),
            new EmptyBorder(16, 16, 16, 16)));

        JLabel title = UIHelper.bodyLabel("🧵 Concurrent Allocation Test");
        title.setFont(UIConstants.FONT_SUBTITLE);

        JTextArea resultArea = new JTextArea(5, 60);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setEditable(false);

        JButton runBtn = UIHelper.primaryButton("Run Concurrent Test");
        runBtn.addActionListener(e -> {
            runBtn.setEnabled(false);
            resultArea.setText("Launching concurrent allocation threads…\n");
            SwingWorker<List<String>, Void> w = new SwingWorker<>() {
                @Override protected List<String> doInBackground() throws Exception {
                    ConcurrentAllocationSimulator sim = new ConcurrentAllocationSimulator();
                    // Build test vehicles from DB
                    List<Vehicle> testVehicles = new VehicleService().getAllVehicles();
                    if (testVehicles.isEmpty()) {
                        return List.of("No registered vehicles found. Add vehicles first.");
                    }
                    // Take up to 5 vehicles for the test
                    List<Vehicle> subset = testVehicles.subList(0, Math.min(5, testVehicles.size()));
                    return sim.simulate(subset);
                }
                @Override protected void done() {
                    try {
                        StringBuilder sb = new StringBuilder();
                        for (String r : get()) sb.append(r).append("\n");
                        sb.append("\nTest complete. Only one thread should succeed per available slot.");
                        resultArea.setText(sb.toString());
                    } catch (Exception ex) {
                        resultArea.setText("Error: " + ex.getMessage());
                    }
                    runBtn.setEnabled(true);

                }
            };
            w.execute();
        });

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(runBtn, BorderLayout.CENTER);
        card.add(left, BorderLayout.WEST);
        card.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return card;
    }
}
