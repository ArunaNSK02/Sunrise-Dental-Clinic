package com.sunrisedental.service;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SMS channel — one concrete {@link NotificationChannel} strategy.
 *
 * <p><b>Honest limitation, not glossed over:</b> this doesn't call a real
 * SMS gateway (Twilio, etc.) — that needs a paid account and API
 * credentials this project doesn't have. What's real: the message is
 * built, logged, and recorded in the {@code notifications} table via
 * {@link NotificationService} regardless (a genuine audit trail, not
 * just a claim), so the feature is fully exercised end to end except for
 * the actual carrier hop. Swapping in a real gateway later means writing
 * one new class implementing {@link NotificationChannel} and changing
 * which implementation {@link NotificationService} is constructed with —
 * nothing else in the app would need to change, which is exactly the
 * point of using Strategy here rather than hard-coding the delivery
 * mechanism into the booking flow.</p>
 */
public class SmsNotificationChannel implements NotificationChannel {

    private static final Logger LOG = Logger.getLogger(SmsNotificationChannel.class.getName());

    @Override
    public boolean send(String recipient, String message) {
        LOG.log(Level.INFO, "[SMS -> {0}] {1}", new Object[]{recipient, message});
        return true;
    }

    @Override
    public String name() {
        return "SMS";
    }
}
