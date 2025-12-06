package com.carrental.model;
/**
 * EconomyCar: extends Car and applies a discount rate.
 */
public class EconomyCar extends Car {
    private double discount = 0.10; // 10%

    public EconomyCar(String id, String model, double ratePerDay, boolean available, String extraInfo) {
        super(id, model, ratePerDay, available, extraInfo);
    }

    @Override
    public double calculateRent(int days) {
        double raw = getRatePerDay() * days;
        return raw * (1 - discount);
    }

    @Override
    public String getType() {
        return "Economy";
    }
}

