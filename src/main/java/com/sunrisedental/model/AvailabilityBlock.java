package com.sunrisedental.model;

import java.time.LocalDateTime;

/**
 * A date/time range a {@link Dentist} has marked unavailable — a day off,
 * a recurring lunch break, a half-day, etc. (class diagram, docs/DESIGN.md).
 * Composed by Dentist (decision 19): a block has no meaning outside the
 * dentist who set it and should be removed automatically when the dentist
 * is.
 */
public class AvailabilityBlock {

    private int blockId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String reason;

    public AvailabilityBlock() {
    }

    public AvailabilityBlock(int blockId, LocalDateTime startDateTime, LocalDateTime endDateTime, String reason) {
        this.blockId = blockId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.reason = reason;
    }

    /** Whether the given moment falls inside this unavailable range. */
    public boolean covers(LocalDateTime dateTime) {
        return !dateTime.isBefore(startDateTime) && dateTime.isBefore(endDateTime);
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
