package com.parkwise.ui;

import com.parkwise.service.AuthService;
import com.parkwise.util.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Login screen — first window shown at application startup.
 * Validates empty fields, incorrect credentials, and shows clear error messages.
 */
public class LoginScreen extends JFrame {

    private final AuthService authService;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         statusLabel;
    private JButton        loginButton;

    public LoginScreen(AuthService authService) {
        this.authService = authService;
        buildUI();
    }

    private void buildUI() {
        setTitle(AppConfig.APP_NAME + " — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Outer panel
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UIConstants.PRIMARY);
        setContentPane(outer);

        // Login card
        JPanel card = UIHelper.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(400, 350));

        // Logo / title
        JLabel logo = new JLabel("🚗 ParkWise");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(UIConstants.PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logo);

        card.add(Box.createVerticalStrut(4));
        JLabel subtitle = new JLabel("Intelligent Parking Allocation System");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createVerticalStrut(28));

        // Username
        usernameField = UIHelper.textField(20);
        card.add(UIHelper.formRow("Username:", usernameField));
        card.add(Box.createVerticalStrut(12));

        // Password
        passwordField = new JPasswordField(20);
        passwordField.setFont(UIConstants.FONT_BODY);
        card.add(UIHelper.formRow("Password:", passwordField));
        card.add(Box.createVerticalStrut(20));

        // Login button
        loginButton = UIHelper.primaryButton("Login");
        loginButton.setPreferredSize(new Dimension(300, UIConstants.BUTTON_HEIGHT));
        loginButton.setMaximumSize(new Dimension(300, UIConstants.BUTTON_HEIGHT));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(this::onLogin);
        card.add(loginButton);

        card.add(Box.createVerticalStrut(12));

        // Status label (shows errors)
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);

        outer.add(card);

        // Allow Enter key to trigger login
        getRootPane().setDefaultButton(loginButton);

        pack();
        setLocationRelativeTo(null);
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            statusLabel.setText("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            statusLabel.setText("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Authenticating…");

        // Run authentication in background so EDT stays responsive
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        dispose();
                        new MainFrame(authService).setVisible(true);
                    } else {
                        statusLabel.setText("Invalid username or password.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                        loginButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Login error: " + ex.getMessage());
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
