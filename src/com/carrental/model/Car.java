package com.carrental.model;

/**
 * Base Car class (parent for EconomyCar, LuxuryCar).
 * Encapsulation: private fields with getters/setters.
 * Polymorphism: calculateRent can be overridden by subclasses.
 */
public class Car {
    private String id;
    private String model;
    private double ratePerDay;
    private boolean available;
    private String extraInfo;

    public Car(String id, String model, double ratePerDay, boolean available, String extraInfo) {
        this.id = id;
        this.model = model;
        this.ratePerDay = ratePerDay;
        this.available = available;
        this.extraInfo = extraInfo;
    }

    // Getters/setters (encapsulation)
    public String getId() { return id; }
    public String getModel() { return model; }
    public double getRatePerDay() { return ratePerDay; }
    public boolean isAvailable() { return available; }
    public String getExtraInfo() { return extraInfo; }

    public void setModel(String model) { this.model = model; }
    public void setRatePerDay(double ratePerDay) { this.ratePerDay = ratePerDay; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }

    // Polymorphic method
    public double calculateRent(int days) {
        return ratePerDay * days;
    }

    public String getType() {
        return "Standard";
    }

    @Override
    public String toString() {
        return id + " | " + model + " | " + getType() + " | ₹" + ratePerDay + "/day | " +
                (available ? "Available" : "Unavailable") + (extraInfo == null || extraInfo.isEmpty() ? "" : " | " + extraInfo);
    }

    // CSV serialization for persistence
    public String toCSV() {
        return escape(id) + "," + escape(model) + "," + ratePerDay + "," + (available ? "1":"0") + "," + escape(extraInfo) + "," + getType();
    }

    protected String escape(String s) {
        if (s == null) return "";
        return s.replace(",", ";");
    }

    public static Car fromCSV(String line) {
        // format: id,model,rate,avail,extra,type
        String[] p = line.split(",", -1);
        if (p.length < 6) return null;
        String id = p[0];
        String model = p[1].replace(";", ",");
        double rate = 0;
        try { rate = Double.parseDouble(p[2]); } catch (Exception ignored) {}
        boolean avail = "1".equals(p[3]);
        String extra = p[4].replace(";", ",");
        String type = p[5];
        if ("Economy".equalsIgnoreCase(type)) {
            return new EconomyCar(id, model, rate, avail, extra);
        } else if ("Luxury".equalsIgnoreCase(type)) {
            return new LuxuryCar(id, model, rate, avail, extra);
        } else {
            return new Car(id, model, rate, avail, extra);
        }
    }
}
