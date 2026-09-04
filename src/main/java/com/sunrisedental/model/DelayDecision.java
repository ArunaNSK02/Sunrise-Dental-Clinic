package com.sunrisedental.model;

/**
 * Outcome of the manual decision recorded for a patient-caused delay, per
 * the class diagram («enumeration» DelayDecision, docs/DESIGN.md,
 * decisions 11 and 18). WAIT cascades exactly like a dentist-caused delay;
 * SKIP hands the appointment to {@link Appointment#reschedule}.
 */
public enum DelayDecision {
    WAIT,
    SKIP
}
