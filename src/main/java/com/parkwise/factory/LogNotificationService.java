package com.parkwise.factory;

import java.util.logging.Logger;

/**
 * Notification implementation that logs to java.util.logging.
 * Used during headless tests or when no Swing environment is available.
 */
public class LogNotificationService implements NotificationService {

    private static final Logger LOG = Logger.getLogger(LogNotificationService.class.getName());

    @Override
    public void sendNotification(String title, String message) {
        LOG.info("[NOTIFICATION] " + title + ": " + message);
    }
}
