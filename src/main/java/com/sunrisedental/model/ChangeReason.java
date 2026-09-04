package com.sunrisedental.model;

/**
 * Who caused an appointment cancellation or delay, per the class diagram
 * («enumeration» ChangeReason, docs/DESIGN.md, decisions 7 and 21).
 * One {@link Appointment#cancel(ChangeReason)} / delay-recording pair of
 * methods handles both causes rather than splitting into separate use
 * cases per cause — the cause is just an attribute on the record.
 */
public enum ChangeReason {
    PATIENT,
    DENTIST
}
