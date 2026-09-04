package com.sunrisedental.model;

/**
 * Lifecycle states for an {@link Appointment}, per the class diagram
 * («enumeration» AppointmentStatus, docs/DESIGN.md, decision 21).
 */
public enum AppointmentStatus {
    SCHEDULED,
    DELAYED,
    CANCELLED,
    RESCHEDULED,
    COMPLETED
}
