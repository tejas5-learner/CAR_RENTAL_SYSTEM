package com.carrental.ui;

import com.carrental.model.*;
import com.carrental.storage.Inventory;
import com.carrental.task.RentalTask;
import com.carrental.exception.CarNotAvailableException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CarRentalApp: advanced Swing GUI (main entry point).
 * Use View -> Tabs: Cars, Rent/Return, Customers, Rentals, Simulate
 */
public class CarRentalApp {
    private Inventory inventory;
    private JFrame frame;
    private DefaultTableModel carsModel, customersModel, rentalsModel;
    private JTable carsTable, customersTable, rentalsTable;
    private JTextArea simOutput;

    public CarRentalApp() {
        inventory = new Inventory();
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        frame = new JFrame("Car Rental System (Java OOP) - Advanced GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // Cars tab
        JPanel carsPanel = new JPanel(new BorderLayout());
        carsModel = new DefaultTableModel(new Object[]{"ID","Model","Type","Rate/day","Available","Extra"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        carsTable = new JTable(carsModel);
        refreshCars();
        carsPanel.add(new JScrollPane(carsTable), BorderLayout.CENTER);

        JPanel carBtns = new JPanel();
        JButton addCar = new JButton("Add Car");
        JButton editCar = new JButton("Edit Selected");
        JButton removeCar = new JButton("Remove Selected");
        JButton refreshCarsBtn = new JButton("Refresh");
        carBtns.add(addCar); carBtns.add(editCar); carBtns.add(removeCar); carBtns.add(refreshCarsBtn);
        carsPanel.add(carBtns, BorderLayout.SOUTH);
        addCar.addActionListener(e -> showAddCarDialog());
        editCar.addActionListener(e -> showEditCarDialog());
        removeCar.addActionListener(e -> removeSelectedCar());
        refreshCarsBtn.addActionListener(e -> refreshCars());
        tabs.addTab("Cars", carsPanel);

        // Rent/Return tab
        JPanel rentPanel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextField rentCarId = new JTextField();
        form.add(new JLabel("Car ID:")); form.add(rentCarId);

        JButton rentBtn = new JButton("Rent");
        JButton returnBtn = new JButton("Return");
        form.add(rentBtn); form.add(returnBtn);
        rentPanel.add(form, BorderLayout.NORTH);

        JTextArea rentOut = new JTextArea(8,40);
        rentOut.setEditable(false);
        rentPanel.add(new JScrollPane(rentOut), BorderLayout.CENTER);

        rentBtn.addActionListener(e -> {
            String carId = rentCarId.getText().trim();
            if (carId.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter Car ID");
                return;
            }

            // Ask for Customer ID
            String custId = JOptionPane.showInputDialog(frame, "Enter Customer ID:");
            if (custId == null || custId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Customer ID required");
                return;
            }
            custId = custId.trim();

            // Check if customer exists
            Customer cust = inventory.getCustomer(custId);
            if (cust == null) {
                // Ask for Name and Phone
                JTextField nameF = new JTextField();
                JTextField phoneF = new JTextField();
                Object[] message = {
                        "Name:", nameF,
                        "Phone:", phoneF
                };
                int option = JOptionPane.showConfirmDialog(frame, message, "New Customer Details", JOptionPane.OK_CANCEL_OPTION);
                if (option != JOptionPane.OK_OPTION) return;
                String name = nameF.getText().trim();
                String phone = phoneF.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Name is required");
                    return;
                }
                inventory.addCustomer(new Customer(custId, name, phone));
                refreshCustomers();
                JOptionPane.showMessageDialog(frame, "New customer added.");
            }

            // Ask for days
            String ds = JOptionPane.showInputDialog(frame, "Enter number of days to rent:");
            if (ds == null || ds.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Days required");
                return;
            }
            try {
                int days = Integer.parseInt(ds.trim());
                Rental r = inventory.rentCar(carId, custId, days);
                rentOut.append("Rented: " + r + "\n");
                refreshCars(); refreshRentals();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Days must be numeric");
            } catch (CarNotAvailableException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        returnBtn.addActionListener(e -> {
            String carId = JOptionPane.showInputDialog(frame, "Enter Car ID to return:");
            if (carId != null && !carId.trim().isEmpty()) {
                inventory.returnCar(carId.trim());
                JOptionPane.showMessageDialog(frame, "Return processed (if car existed).");
                refreshCars();
            }
        });

        tabs.addTab("Rent/Return", rentPanel);

        // Customers tab
        JPanel custPanel = new JPanel(new BorderLayout());
        customersModel = new DefaultTableModel(new Object[]{"ID","Name","Phone"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        customersTable = new JTable(customersModel);
        refreshCustomers();
        custPanel.add(new JScrollPane(customersTable), BorderLayout.CENTER);
        JPanel custBtns = new JPanel();
        JButton addCust = new JButton("Add Customer");
        JButton refreshCust = new JButton("Refresh");
        custBtns.add(addCust); custBtns.add(refreshCust);
        custPanel.add(custBtns, BorderLayout.SOUTH);
        addCust.addActionListener(e -> showAddCustomerDialog());
        refreshCust.addActionListener(e -> refreshCustomers());
        tabs.addTab("Customers", custPanel);

        // Rentals tab
        JPanel rentalsPanel = new JPanel(new BorderLayout());
        rentalsModel = new DefaultTableModel(new Object[]{"ID","CarID","CustID","Days","Total","Date"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rentalsTable = new JTable(rentalsModel);
        refreshRentals();
        rentalsPanel.add(new JScrollPane(rentalsTable), BorderLayout.CENTER);
        JButton refreshRentalsBtn = new JButton("Refresh");
        rentalsPanel.add(refreshRentalsBtn, BorderLayout.SOUTH);
        refreshRentalsBtn.addActionListener(e -> refreshRentals());
        tabs.addTab("Rentals", rentalsPanel);

        // Simulate tab
        JPanel simPanel = new JPanel(new BorderLayout());
        JPanel simTop = new JPanel(new BorderLayout());
        JTextArea simInput = new JTextArea(6,50);
        simTop.add(new JLabel("<html>Enter lines: <b>CarID,CustomerID,Days</b>. One per line.</html>"), BorderLayout.NORTH);
        simTop.add(new JScrollPane(simInput), BorderLayout.CENTER);
        JButton runSim = new JButton("Run Simulation");
        simTop.add(runSim, BorderLayout.SOUTH);
        simPanel.add(simTop, BorderLayout.NORTH);
        simOutput = new JTextArea(10,80);
        simOutput.setEditable(false);
        simPanel.add(new JScrollPane(simOutput), BorderLayout.CENTER);
        runSim.addActionListener(e -> runSimulation(simInput.getText()));
        tabs.addTab("Simulate", simPanel);

        frame.getContentPane().add(tabs);
        frame.setVisible(true);
    }

    private Object showEditCarDialog() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'showEditCarDialog'");
    }

    /* ---------- helper methods ---------- */
    private void refreshCars() {
        SwingUtilities.invokeLater(() -> {
            carsModel.setRowCount(0);
            List<Car> list = inventory.listAllCars();
            for (Car c : list) {
                carsModel.addRow(new Object[]{c.getId(), c.getModel(), c.getType(), c.getRatePerDay(), c.isAvailable() ? "Yes":"No", c.getExtraInfo()});
            }
        });
    }

    private void refreshCustomers() {
        SwingUtilities.invokeLater(() -> {
            customersModel.setRowCount(0);
            for (Customer cu : inventory.listCustomers()) {
                customersModel.addRow(new Object[]{cu.getId(), cu.getName(), cu.getPhone()});
            }
        });
    }

    private void refreshRentals() {
        SwingUtilities.invokeLater(() -> {
            rentalsModel.setRowCount(0);
            for (Rental r : inventory.listRentals()) {
                String[] p = r.toCSV().split(",", -1);
                rentalsModel.addRow(new Object[]{p[0], p[1], p[2], p[3], p[4], p[5]});
            }
        });
    }

    private void showAddCarDialog() {
        JDialog d = new JDialog(frame, "Add Car", true);
        d.setSize(420,300); d.setLocationRelativeTo(frame);
        JPanel p = new JPanel(new GridLayout(0,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JTextField idF = new JTextField();
        JTextField modelF = new JTextField();
        JTextField rateF = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Standard","Economy","Luxury"});
        JTextField extraF = new JTextField();
        p.add(new JLabel("Car ID:")); p.add(idF);
        p.add(new JLabel("Model:")); p.add(modelF);
        p.add(new JLabel("Rate/day:")); p.add(rateF);
        p.add(new JLabel("Type:")); p.add(typeBox);
        p.add(new JLabel("Extra info:")); p.add(extraF);
        JButton addBtn = new JButton("Add"); JButton cancelBtn = new JButton("Cancel");
        p.add(addBtn); p.add(cancelBtn);
        d.add(p);
        addBtn.addActionListener(e -> {
            String id = idF.getText().trim();
            String model = modelF.getText().trim();
            String rateS = rateF.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String extra = extraF.getText().trim();
            if (id.isEmpty() || model.isEmpty() || rateS.isEmpty()) {
                JOptionPane.showMessageDialog(d, "ID, model and rate required");
                return;
            }
            try {
                double rate = Double.parseDouble(rateS);
                Car c;
                if ("Economy".equalsIgnoreCase(type)) c = new EconomyCar(id, model, rate, true, extra);
                else if ("Luxury".equalsIgnoreCase(type)) c = new LuxuryCar(id, model, rate, true, extra);
                else c = new Car(id, model, rate, true, extra);
                inventory.addCar(c);
                refreshCars();
                JOptionPane.showMessageDialog(d, "Car added.");
                d.dispose();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(d, "Rate must be a number");
            }
        });
        cancelBtn.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    private void showAddCustomerDialog() {
        JDialog d = new JDialog(frame, "Add Customer", true);
        d.setSize(360,220); d.setLocationRelativeTo(frame);
        JPanel p = new JPanel(new GridLayout(0,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JTextField idF = new JTextField();
        JTextField nameF = new JTextField();
        JTextField phoneF = new JTextField();
        p.add(new JLabel("Customer ID:")); p.add(idF);
        p.add(new JLabel("Name:")); p.add(nameF);
        p.add(new JLabel("Phone:")); p.add(phoneF);
        JButton add = new JButton("Add"); JButton cancel = new JButton("Cancel");
        p.add(add); p.add(cancel);
        d.add(p);
        add.addActionListener(e -> {
            String id = idF.getText().trim();
            String name = nameF.getText().trim();
            String phone = phoneF.getText().trim();
            if (id.isEmpty() || name.isEmpty()) { JOptionPane.showMessageDialog(d, "ID and name required"); return; }
            inventory.addCustomer(new Customer(id, name, phone));
            refreshCustomers();
            JOptionPane.showMessageDialog(d, "Customer added");
            d.dispose();
        });
        cancel.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    private void removeSelectedCar() {
        int r = carsTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(frame, "Select a car to remove"); return; }
        String carId = (String) carsTable.getValueAt(r, 0);
        int ch = JOptionPane.showConfirmDialog(frame, "Remove car " + carId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ch == JOptionPane.YES_OPTION) {
            inventory.removeCar(carId);
            refreshCars();
        }
    }

    private void runSimulation(String input) {
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter simulation lines");
            return;
        }
        String[] lines = input.split("\\r?\\n");
        ExecutorService exec = Executors.newFixedThreadPool(Math.min(lines.length, 10));
        simOutput.setText("");
        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length < 3) {
                simOutput.append("Skipping invalid: " + line + "\n");
                continue;
            }
            String carId = p[0].trim();
            String custId = p[1].trim();
            int days;
            try { days = Integer.parseInt(p[2].trim()); } catch (NumberFormatException nfe) {
                simOutput.append("Invalid days: " + line + "\n"); continue;
            }
            exec.submit(new RentalTask(inventory, carId, custId, days, simOutput));
        }
        exec.shutdown();
        new Thread(() -> {
            try {
                exec.awaitTermination(30, TimeUnit.SECONDS);
                SwingUtilities.invokeLater(() -> {
                    refreshCars(); refreshRentals();
                    simOutput.append("Simulation finished.\n");
                });
            } catch (InterruptedException e) {
                SwingUtilities.invokeLater(() -> simOutput.append("Simulation interrupted.\n"));
            }
        }).start();
    }

    public static void main(String[] args) {
        new CarRentalApp();
    }
}