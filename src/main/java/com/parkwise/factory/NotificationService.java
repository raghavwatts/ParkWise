package com.parkwise.factory;

/**
 * Notification service interface.
 *
 * <p>Design Pattern: <b>Factory</b> — the concrete implementation is created
 * by {@link NotificationFactory} based on the configured notification channel.</p>
 */
public interface NotificationService {

    /**
     * Sends a notification message.
     *
     * @param title   the notification title
     * @param message the notification body
     */
    void sendNotification(String title, String message);
}
