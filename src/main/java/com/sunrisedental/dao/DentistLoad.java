package com.sunrisedental.dao;

/**
 * One row of the "appointments per dentist" report — reporting-specific,
 * not a class-diagram entity. {@code revenue} comes from the
 * {@code sp_dentist_daily_revenue} stored procedure (schema.sql), not a
 * plain Java-side SELECT.
 */
public record DentistLoad(String dentistName, int appointmentCount, double revenue) {
}
