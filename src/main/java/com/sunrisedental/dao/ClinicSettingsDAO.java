package com.sunrisedental.dao;

/**
 * Backs the flat clinic-wide consultation fee (DESIGN.md decisions 26-27)
 * — a persistence-tier config value, not a class-diagram entity, so this
 * interface is deliberately narrower than the other DAOs (no findById/
 * save/delete — there's exactly one row).
 */
public interface ClinicSettingsDAO {

    double getConsultationFee();
}
