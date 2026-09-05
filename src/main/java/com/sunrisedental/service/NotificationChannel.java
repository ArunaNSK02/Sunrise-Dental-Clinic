package com.sunrisedental.service;

/**
 * Strategy pattern (CLAUDE.md's suggested pattern list): {@link NotificationService}
 * depends on this interface, not a specific delivery mechanism, so how a
 * notification actually reaches someone can be swapped — SMS today, email
 * or push in future — without touching the business logic that decides
 * <i>when</i> to notify (booking, cancellation, etc.).
 */
public interface NotificationChannel {

    /** @return true if delivery succeeded (or was accepted for delivery) */
    boolean send(String recipient, String message);

    String name();
}
