package com.parkwise.service;

import com.parkwise.dao.VehicleDAO;
import com.parkwise.exception.DuplicateVehicleException;
import com.parkwise.exception.VehicleNotFoundException;
import com.parkwise.model.Vehicle;
import com.parkwise.util.ValidationUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for vehicle management.
 * Uses an in-memory HashMap cache for fast registration-based lookups.
 */
public class VehicleService {

    private final VehicleDAO vehicleDAO;

    // In-memory cache: HashMap for O(1) lookups by registration number
    private final Map<String, Vehicle> vehicleCache = new HashMap<>();

    public VehicleService() {
        this.vehicleDAO = new VehicleDAO();
    }

    /**
     * Registers a new vehicle.
     *
     * @throws DuplicateVehicleException if the registration number already exists
     * @throws IllegalArgumentException  on validation failure
     */
    public Vehicle addVehicle(String regNum, com.parkwise.model.VehicleType type,
                              String ownerName, String contact)
            throws SQLException, DuplicateVehicleException {

        // Validation
        ValidationUtil.require(!ValidationUtil.isBlank(regNum),       "Registration number is required");
        ValidationUtil.require(!ValidationUtil.isBlank(ownerName),    "Owner name is required");
        ValidationUtil.require(!ValidationUtil.isBlank(contact),      "Contact number is required");
        ValidationUtil.require(type != null,                          "Vehicle type is required");
        ValidationUtil.require(ValidationUtil.isValidRegistrationNumber(regNum),
                "Invalid registration number format (e.g. KA01AB1234)");
        ValidationUtil.require(ValidationUtil.isValidContactNumber(contact),
                "Invalid contact number (10-digit Indian mobile)");

        String normalised = regNum.trim().toUpperCase();
        if (vehicleDAO.existsByRegistrationNumber(normalised)) {
            throw new DuplicateVehicleException(
                    "Vehicle with registration number " + normalised + " already exists");
        }

        Vehicle v = new Vehicle();
        v.setRegistrationNumber(normalised);
        v.setVehicleType(type);
        v.setOwnerName(ownerName.trim());
        v.setContactNumber(contact.trim());
        int id = vehicleDAO.insert(v);
        v.setId(id);

        vehicleCache.put(normalised, v);  // update cache
        return v;
    }

    /** Returns a vehicle by registration number. */
    public Vehicle findByRegistrationNumber(String regNum)
            throws SQLException, VehicleNotFoundException {

        String normalised = regNum.trim().toUpperCase();

        // Check HashMap cache first (autoboxing: String key → hashCode)
        if (vehicleCache.containsKey(normalised)) {
            return vehicleCache.get(normalised);
        }

        Optional<Vehicle> opt = vehicleDAO.findByRegistrationNumber(normalised);
        if (opt.isEmpty()) {
            throw new VehicleNotFoundException(
                    "No vehicle found with registration number: " + normalised);
        }
        vehicleCache.put(normalised, opt.get());
        return opt.get();
    }

    /** Returns all registered vehicles. */
    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> all = vehicleDAO.findAll();
        // Repopulate cache — Stream + lambda
        vehicleCache.clear();
        all.forEach(v -> vehicleCache.put(v.getRegistrationNumber(), v));
        return all;
    }

    /** Updates vehicle details (registration number is immutable). */
    public void updateVehicle(Vehicle vehicle) throws SQLException {
        ValidationUtil.require(!ValidationUtil.isBlank(vehicle.getOwnerName()), "Owner name required");
        ValidationUtil.require(ValidationUtil.isValidContactNumber(vehicle.getContactNumber()),
                "Invalid contact number");
        vehicleDAO.update(vehicle);
        vehicleCache.put(vehicle.getRegistrationNumber(), vehicle);
    }

    /** Deletes a vehicle. Will throw if active sessions exist. */
    public void deleteVehicle(int vehicleId) throws SQLException {
        vehicleDAO.delete(vehicleId);
        // Remove from cache — use entrySet stream to find by id
        vehicleCache.entrySet().removeIf(e -> e.getValue().getId() == vehicleId);
    }

    /** Returns vehicles whose registration or owner name contains the keyword. */
    public List<Vehicle> search(String keyword) throws SQLException {
        if (ValidationUtil.isBlank(keyword)) return getAllVehicles();
        List<Vehicle> results = vehicleDAO.search(keyword.trim());

        // Stream API: collect distinct registration numbers using HashSet
        Set<String> seen = new HashSet<>();
        return results.stream()
                .filter(v -> seen.add(v.getRegistrationNumber()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
