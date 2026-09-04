package com.sunrisedental.dao;

/** One row of the "appointments per dentist" report — reporting-specific, not a class-diagram entity. */
public record DentistLoad(String dentistName, int appointmentCount) {
}
