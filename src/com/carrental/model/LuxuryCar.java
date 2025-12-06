package com.carrental.model;

/**
 * LuxuryCar: extends Car and applies a surcharge.
 */
public class LuxuryCar extends Car {
    private double surcharge = 0.50; // 50%

    public LuxuryCar(String id, String model, double ratePerDay, boolean available, String extraInfo) {
        super(id, model, ratePerDay, available, extraInfo);
    }

    @Override
    public double calculateRent(int days) {
        double base = getRatePerDay();
        return (base + base * surcharge) * days;
    }

    @Override
    public String getType() {
        return "Luxury";
    }
}
