package com.carrental.task;

import com.carrental.storage.Inventory;
import com.carrental.model.Rental;
import com.carrental.exception.CarNotAvailableException;

import javax.swing.*;

/**
 * Runnable used for concurrent rental simulation.
 */
public class RentalTask implements Runnable {
    private final Inventory inventory;
    private final String carId;
    private final String custId;
    private final int days;
    private final JTextArea output;

    public RentalTask(Inventory inventory, String carId, String custId, int days, JTextArea output) {
        this.inventory = inventory;
        this.carId = carId;
        this.custId = custId;
        this.days = days;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            Rental r = inventory.rentCar(carId, custId, days);
            SwingUtilities.invokeLater(() -> output.append("SUCCESS: " + r + "\n"));
        } catch (CarNotAvailableException e) {
            SwingUtilities.invokeLater(() -> output.append("FAILED: " + e.getMessage() + " (Car:" + carId + ", Cust:" + custId + ")\n"));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> output.append("ERROR: " + e.getMessage() + "\n"));
        }
    }
}
