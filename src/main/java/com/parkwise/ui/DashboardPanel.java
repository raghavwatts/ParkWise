package com.parkwise.ui;

import com.parkwise.dao.ParkingSessionDAO;
import com.parkwise.service.ParkingService;
import com.parkwise.service.TariffService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dashboard panel showing live parking statistics.
 * Statistics are loaded via a SwingWorker so the UI remains responsive.
 */
public class DashboardPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(DashboardPanel.class.getName());

    private final com.parkwise.service.ParkingSlotService slotService =
            new com.parkwise.service.ParkingSlotService();
    private final ParkingService parkingService =
            new ParkingService(new TariffService());

    // Stat labels
    private JLabel lblTotal, lblAvailable, lblOccupied, lblReserved;
    private JLabel lblActive, lblCompleted, lblRevenue;
    private JLabel lblStatus;

    public DashboardPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIHelper.titleLabel("📊  Dashboard"), BorderLayout.WEST);
        lblStatus = UIHelper.bodyLabel("Last updated: —");
        lblStatus.setForeground(Color.GRAY);
        header.add(lblStatus, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Stats grid
        JPanel grid = new JPanel(new GridLayout(2, 4, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(24, 0, 0, 0));

        lblTotal     = createStatCard(grid, "Total Slots",          "—", UIConstants.PRIMARY);
        lblAvailable = createStatCard(grid, "Available",            "—", UIConstants.SUCCESS);
        lblOccupied  = createStatCard(grid, "Occupied",             "—", UIConstants.DANGER);
        lblReserved  = createStatCard(grid, "Reserved",             "—", UIConstants.WARNING);
        lblActive    = createStatCard(grid, "Vehicles Parked",      "—", UIConstants.ACCENT);
        lblCompleted = createStatCard(grid, "Sessions Today",       "—", UIConstants.PRIMARY);
        lblRevenue   = createStatCard(grid, "Today's Revenue (₹)", "—", UIConstants.SUCCESS);
        createStatCard(grid, "System",                              "Online", UIConstants.ACCENT);

        add(grid, BorderLayout.CENTER);

        // Refresh button
        JButton refreshBtn = UIHelper.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Creates a stat card and adds it to the grid.
     * @return the value label so the refresh method can update it
     */
    private JLabel createStatCard(JPanel grid, String title, String initialValue, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, color),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_SMALL);
        titleLbl.setForeground(Color.GRAY);
        card.add(titleLbl, BorderLayout.NORTH);

        JLabel valueLbl = new JLabel(initialValue);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLbl.setForeground(color);
        card.add(valueLbl, BorderLayout.CENTER);

        grid.add(card);
        return valueLbl;
    }

    /** Loads stats from the database in a background thread. */
    public void refresh() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            private int completedToday;
            private BigDecimal revenue;
            private int activeSessions;

            @Override
            protected int[] doInBackground() throws Exception {
                int[] counts = slotService.getDashboardCounts();  // [total, avail, occ, res]
                completedToday = parkingService.getCompletedTodayCount();
                revenue        = parkingService.getTodayRevenue();
                activeSessions = parkingService.getAllActiveSessions().size();
                return counts;
            }

            @Override
            protected void done() {
                try {
                    int[] c = get();
                    lblTotal.setText(String.valueOf(c[0]));
                    lblAvailable.setText(String.valueOf(c[1]));
                    lblOccupied.setText(String.valueOf(c[2]));
                    lblReserved.setText(String.valueOf(c[3]));
                    lblActive.setText(String.valueOf(activeSessions));
                    lblCompleted.setText(String.valueOf(completedToday));
                    lblRevenue.setText("₹" + (revenue != null ? revenue.toPlainString() : "0.00"));
                    lblStatus.setText("Last updated: "
                        + java.time.LocalTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Dashboard refresh error", e);
                    lblStatus.setText("Refresh error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
