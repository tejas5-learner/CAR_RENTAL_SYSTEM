package com.carrental.model;

/**
 * Customer: simple data holder for customer details.
 */
public class Customer {
    private String id;
    private String name;
    private String phone;

    public Customer(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return id + " | " + name + " | " + phone;
    }

    public String toCSV() {
        return id + "," + name.replace(",", ";") + "," + phone;
    }

    public static Customer fromCSV(String line) {
        String[] p = line.split(",", -1);
        String id = p.length>0 ? p[0] : "";
        String name = p.length>1 ? p[1].replace(";", ",") : "";
        String phone = p.length>2 ? p[2] : "";
        return new Customer(id, name, phone);
    }
}
