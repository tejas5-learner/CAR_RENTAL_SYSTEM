package com.carrental.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Rental: record of a rent transaction.
 */
public class Rental {
    private String id;
    private String carId;
    private String customerId;
    private int days;
    private double total;
    private String date;

    public Rental(String id, String carId, String customerId, int days, double total, String date) {
        this.id = id;
        this.carId = carId;
        this.customerId = customerId;
        this.days = days;
        this.total = total;
        this.date = date;
    }

    public String getId() { return id; }
    public String getCarId() { return carId; }
    public String getCustomerId() { return customerId; }
    public int getDays() { return days; }
    public double getTotal() { return total; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return id + " | Car:" + carId + " | Cust:" + customerId + " | Days:" + days + " | ₹" + total + " | " + date;
    }

    public String toCSV() {
        return id + "," + carId + "," + customerId + "," + days + "," + total + "," + date;
    }

    public static Rental fromCSV(String line) {
        String[] p = line.split(",", -1);
        try {
            String id = p[0];
            String carId = p[1];
            String cust = p[2];
            int days = p.length>3 && !p[3].isEmpty() ? Integer.parseInt(p[3]) : 0;
            double total = p.length>4 && !p[4].isEmpty() ? Double.parseDouble(p[4]) : 0.0;
            String date = p.length>5 ? p[5] : new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            return new Rental(id, carId, cust, days, total, date);
        } catch (Exception e) {
            return null;
        }
    }
}
