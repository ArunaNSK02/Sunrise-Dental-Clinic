package com.sunrisedental.model;

/**
 * A billable treatment/procedure type (class diagram, docs/DESIGN.md).
 * Independent, reusable record referenced by many appointments — plain
 * association to {@link Appointment}, not aggregation/composition
 * (decision 19).
 *
 * <p>{@code durationMinutes} was added in Class Diagram rev. 2 (decision
 * 24): real dental scheduling assigns a default slot length per procedure
 * type (a checkup and a root canal don't occupy the same time), and this
 * feeds the availability clash-check window and how far a delay cascade
 * shifts later appointments.</p>
 */
public class Treatment {

    private int treatmentId;
    private String name;
    private double cost;
    private int durationMinutes;

    public Treatment() {
    }

    public Treatment(int treatmentId, String name, double cost, int durationMinutes) {
        this.treatmentId = treatmentId;
        this.name = name;
        this.cost = cost;
        this.durationMinutes = durationMinutes;
    }

    public double getCost() {
        return cost;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
