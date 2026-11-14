package com.parkwise.factory;

import javax.swing.*;

/**
 * Notification implementation that displays a Swing dialog.
 * Used as the primary channel in the desktop application.
 */
public class DialogNotificationService implements NotificationService {

    @Override
    public void sendNotification(String title, String message) {
        // Must update Swing on the EDT
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
        );
    }
}
