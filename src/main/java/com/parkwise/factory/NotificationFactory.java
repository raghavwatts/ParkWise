package com.parkwise.factory;

/**
 * Factory for creating {@link NotificationService} instances.
 *
 * <p>Design Pattern: <b>Factory Method</b> — centralises the creation
 * of notification implementations, decoupling clients from concrete types.
 * The caller requests a channel by name; the factory decides which class
 * to instantiate.</p>
 */
public final class NotificationFactory {

    private NotificationFactory() {}

    /**
     * Creates a notification service for the requested channel.
     *
     * @param channel "DIALOG" for Swing dialogs, "LOG" for log output
     * @return the appropriate NotificationService implementation
     * @throws IllegalArgumentException for unknown channels
     */
    public static NotificationService create(String channel) {
        return switch (channel.toUpperCase()) {
            case "DIALOG" -> new DialogNotificationService();
            case "LOG"    -> new LogNotificationService();
            default       -> throw new IllegalArgumentException("Unknown notification channel: " + channel);
        };
    }

    /** Convenience: creates the default desktop channel. */
    public static NotificationService createDefault() {
        return create("DIALOG");
    }
}
