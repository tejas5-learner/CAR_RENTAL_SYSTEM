package com.carrental.storage;

import com.carrental.model.*;
import com.carrental.exception.CarNotAvailableException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Inventory: thread-safe manager for cars, customers, rentals.
 * Uses simple CSV text files for persistence.
 */
public class Inventory {
    private final Map<String, Car> cars = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();
    private final List<Rental> rentals = new ArrayList<>();

    private final File carsFile = new File("cars.txt");
    private final File customersFile = new File("customers.txt");
    private final File rentalsFile = new File("rentals.txt");

    private final Object lock = new Object();

    public Inventory() {
        loadAll();
    }

    private void loadAll() {
        loadCars();
        loadCustomers();
        loadRentals();
        if (cars.isEmpty()) {
            addCar(new com.carrental.model.EconomyCar("C001","Hyundai i10",800,true,"AC"));
            addCar(new Car("C002","Mahindra Scorpio",2000,true,"7 seats"));
            addCar(new com.carrental.model.LuxuryCar("C003","Mercedes E-Class",7000,true,"Leather"));
            addCar(new com.carrental.model.EconomyCar("C004","Toyota Etios",1000,true,"AC"));
        }
        if (customers.isEmpty()) {
            addCustomer(new Customer("U001","Alice","9876543210"));
            addCustomer(new Customer("U002","Rahul","9123456780"));
        }
    }

    /* ---------- persistence ---------- */
    private void loadCars() {
        synchronized (lock) {
            cars.clear();
            if (!carsFile.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(carsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Car c = Car.fromCSV(line);
                    if (c != null) cars.put(c.getId(), c);
                }
            } catch (IOException e) {
                System.err.println("loadCars error: " + e.getMessage());
            }
        }
    }

    private void saveCars() {
        synchronized (lock) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(carsFile))) {
                for (Car c : cars.values()) {
                    bw.write(c.toCSV());
                    bw.newLine();
                }
            } catch (IOException e) {
                System.err.println("saveCars error: " + e.getMessage());
            }
        }
    }

    private void loadCustomers() {
        synchronized (lock) {
            customers.clear();
            if (!customersFile.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(customersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Customer cu = Customer.fromCSV(line);
                    if (cu != null) customers.put(cu.getId(), cu);
                }
            } catch (IOException e) {
                System.err.println("loadCustomers error: " + e.getMessage());
            }
        }
    }

    private void saveCustomers() {
        synchronized (lock) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(customersFile))) {
                for (Customer cu : customers.values()) {
                    bw.write(cu.toCSV());
                    bw.newLine();
                }
            } catch (IOException e) {
                System.err.println("saveCustomers error: " + e.getMessage());
            }
        }
    }

    private void loadRentals() {
        synchronized (lock) {
            rentals.clear();
            if (!rentalsFile.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(rentalsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Rental r = Rental.fromCSV(line);
                    if (r != null) rentals.add(r);
                }
            } catch (IOException e) {
                System.err.println("loadRentals error: " + e.getMessage());
            }
        }
    }

    private void saveRentals() {
        synchronized (lock) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(rentalsFile))) {
                for (Rental r : rentals) {
                    bw.write(r.toCSV());
                    bw.newLine();
                }
            } catch (IOException e) {
                System.err.println("saveRentals error: " + e.getMessage());
            }
        }
    }

    /* ---------- car operations ---------- */
    public void addCar(Car car) {
        synchronized (lock) {
            cars.put(car.getId(), car);
            saveCars();
        }
    }

    public void removeCar(String carId) {
        synchronized (lock) {
            cars.remove(carId);
            saveCars();
        }
    }

    public void updateCar(Car car) {
        synchronized (lock) {
            cars.put(car.getId(), car);
            saveCars();
        }
    }

    public Car getCar(String carId) {
        synchronized (lock) {
            return cars.get(carId);
        }
    }

    public List<Car> listAllCars() {
        synchronized (lock) {
            return new ArrayList<>(cars.values());
        }
    }

    public List<Car> listAvailableCars() {
        synchronized (lock) {
            List<Car> res = new ArrayList<>();
            for (Car c : cars.values()) if (c.isAvailable()) res.add(c);
            return res;
        }
    }

    /* ---------- customer operations ---------- */
    public void addCustomer(Customer cu) {
        synchronized (lock) {
            customers.put(cu.getId(), cu);
            saveCustomers();
        }
    }

    public Customer getCustomer(String id) {
        synchronized (lock) {
            return customers.get(id);
        }
    }

    public List<Customer> listCustomers() {
        synchronized (lock) {
            return new ArrayList<>(customers.values());
        }
    }

    /* ---------- rental operations ---------- */
    public Rental rentCar(String carId, String customerId, int days) throws CarNotAvailableException {
        if (days <= 0) throw new IllegalArgumentException("Days must be > 0");
        synchronized (lock) {
            Car c = cars.get(carId);
            if (c == null) throw new CarNotAvailableException("Car not found: " + carId);
            if (!c.isAvailable()) throw new CarNotAvailableException("Car not available: " + carId);
            c.setAvailable(false);
            saveCars();
            double cost = c.calculateRent(days);
            String rid = "R" + (rentals.size() + 1 + new Random().nextInt(1000));
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Rental r = new Rental(rid, carId, customerId, days, cost, date);
            rentals.add(r);
            saveRentals();
            return r;
        }
    }

    public void returnCar(String carId) {
        synchronized (lock) {
            Car c = cars.get(carId);
            if (c != null) {
                c.setAvailable(true);
                saveCars();
            }
        }
    }

    public List<Rental> listRentals() {
        synchronized (lock) {
            return new ArrayList<>(rentals);
        }
    }
}
