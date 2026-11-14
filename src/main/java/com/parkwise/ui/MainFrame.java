package com.parkwise.ui;

import com.parkwise.observer.ParkingEvent;
import com.parkwise.observer.ParkingEventPublisher;
import com.parkwise.service.AuthService;
import com.parkwise.util.AppConfig;
import com.parkwise.util.DashboardRefreshTask;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window.
 * Contains a sidebar navigation panel and a CardLayout content area.
 * Registers as an Observer for parking events and routes Swing updates to the EDT.
 */
public class MainFrame extends JFrame {

    private final AuthService          authService;
    private final DashboardRefreshTask refreshTask = new DashboardRefreshTask();

    // Panel references
    private DashboardPanel     dashboardPanel;
    private VehiclePanel       vehiclePanel;
    private SlotPanel          slotPanel;
    private EntryExitPanel     entryExitPanel;
    private ReservationPanel   reservationPanel;
    private TariffPanel        tariffPanel;
    private ReportPanel        reportPanel;
    private SettingsPanel      settingsPanel;

    private JPanel     contentArea;
    private CardLayout cardLayout;

    private static final String DASH     = "Dashboard";
    private static final String VEHICLES = "Vehicles";
    private static final String SLOTS    = "Slots";
    private static final String ENTRY    = "Entry/Exit";
    private static final String RESERVATIONS = "Reservations";
    private static final String TARIFFS  = "Tariffs";
    private static final String REPORTS  = "Reports";
    private static final String SETTINGS = "Settings";

    public MainFrame(AuthService authService) {
        this.authService = authService;
        buildUI();
        startBackgroundTasks();
    }

    private void buildUI() {
        setTitle(AppConfig.APP_NAME);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setPreferredSize(new Dimension(1280, 800));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                onExit();
            }
        });

        setLayout(new BorderLayout());

        // ── Sidebar ──────────────────────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Content area ─────────────────────────────────────────────────────
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIConstants.LIGHT_BG);

        dashboardPanel   = new DashboardPanel();
        vehiclePanel     = new VehiclePanel();
        slotPanel        = new SlotPanel();
        entryExitPanel   = new EntryExitPanel();
        reservationPanel = new ReservationPanel();
        tariffPanel      = new TariffPanel();
        reportPanel      = new ReportPanel();
        settingsPanel    = new SettingsPanel(authService);

        contentArea.add(dashboardPanel,   DASH);
        contentArea.add(vehiclePanel,     VEHICLES);
        contentArea.add(slotPanel,        SLOTS);
        contentArea.add(entryExitPanel,   ENTRY);
        contentArea.add(reservationPanel, RESERVATIONS);
        contentArea.add(tariffPanel,      TARIFFS);
        contentArea.add(reportPanel,      REPORTS);
        contentArea.add(settingsPanel,    SETTINGS);

        add(contentArea, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIConstants.PRIMARY);
        sidebar.setPreferredSize(new Dimension(200, 700));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // App logo area
        JLabel logo = new JLabel("🚗 ParkWise");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        sidebar.add(logo);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 1));
        sep.setForeground(new Color(0xFFFFFF, true));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        // Nav buttons
        String[] items = {DASH, VEHICLES, SLOTS, ENTRY, RESERVATIONS, TARIFFS, REPORTS, SETTINGS};
        String[] icons = {"📊", "🚗", "🅿️", "🔑", "📅", "💰", "📈", "⚙️"};
        for (int i = 0; i < items.length; i++) {
            sidebar.add(navButton(icons[i] + "  " + items[i], items[i]));
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalGlue());

        // Logged-in user label
        JLabel userLbl = new JLabel("👤 " + authService.getCurrentAdmin().getUsername());
        userLbl.setFont(UIConstants.FONT_SMALL);
        userLbl.setForeground(new Color(0xAEC6CF));
        userLbl.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 8));
        sidebar.add(userLbl);

        // Logout button
        JButton logoutBtn = new JButton("⏻  Logout");
        logoutBtn.setFont(UIConstants.FONT_BUTTON);
        logoutBtn.setForeground(UIConstants.WARNING);
        logoutBtn.setBackground(UIConstants.PRIMARY);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(200, 40));
        logoutBtn.addActionListener(e -> onLogout());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(12));

        return sidebar;
    }

    private JButton navButton(String label, String card) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(0xD0E8FF));
        btn.setBackground(UIConstants.PRIMARY);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cardLayout.show(contentArea, card);
            // Trigger refresh on the newly shown panel
            refreshActivePanel(card);
        });
        return btn;
    }

    /** Refreshes whichever panel just became visible. */
    private void refreshActivePanel(String card) {
        switch (card) {
            case DASH     -> dashboardPanel.refresh();
            case VEHICLES -> vehiclePanel.refresh();
            case SLOTS    -> slotPanel.refresh();
            case ENTRY    -> entryExitPanel.refresh();
            case RESERVATIONS -> reservationPanel.refresh();
            case TARIFFS  -> tariffPanel.refresh();
            case REPORTS  -> reportPanel.refresh();
            default -> {}
        }
    }

    /** Starts background executor tasks. */
    private void startBackgroundTasks() {
        // Subscribe to parking events for dashboard refresh
        ParkingEventPublisher.getInstance().subscribe(event -> {
            // Safely update Swing on EDT
            SwingUtilities.invokeLater(() -> {
                if (event.getEventType() == ParkingEvent.EventType.DASHBOARD_REFRESH ||
                    event.getEventType() == ParkingEvent.EventType.VEHICLE_ENTERED  ||
                    event.getEventType() == ParkingEvent.EventType.VEHICLE_EXITED) {
                    dashboardPanel.refresh();
                }
            });
        });

        // Auto-refresh dashboard every 30 seconds
        refreshTask.start(30);
    }

    private void onLogout() {
        if (UIHelper.confirm(this, "Are you sure you want to logout?")) {
            refreshTask.shutdown();
            authService.logout();
            dispose();
            new LoginScreen(new AuthService()).setVisible(true);
        }
    }

    private void onExit() {
        if (UIHelper.confirm(this, "Exit ParkWise?")) {
            refreshTask.shutdown();
            com.parkwise.util.DBConnection.getInstance().shutdown();
            System.exit(0);
        }
    }
}
