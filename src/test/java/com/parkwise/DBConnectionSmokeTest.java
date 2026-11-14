package com.parkwise;

import com.parkwise.dao.AdminDAO;
import com.parkwise.dao.ParkingSlotDAO;
import com.parkwise.dao.VehicleDAO;
import com.parkwise.model.Admin;
import com.parkwise.model.ParkingSlot;
import com.parkwise.model.Vehicle;
import com.parkwise.util.DBConnection;
import com.parkwise.util.DatabaseInitializer;
import com.parkwise.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

/**
 * Smoke test that verifies the full database connectivity chain:
 * 1. DBConnection loads db.properties
 * 2. DatabaseInitializer creates the default admin + tariffs
 * 3. Admin can be retrieved and password verified
 * 4. Sample data (slots, vehicles) is queryable
 */
public class DBConnectionSmokeTest {

    public static void main(String[] args) {
        System.out.println("=== ParkWise DB Connection Smoke Test ===\n");

        boolean allPassed = true;

        // 1. Initialize the database
        System.out.print("[1] Running DatabaseInitializer... ");
        try {
            DatabaseInitializer.initialize();
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
            allPassed = false;
        }

        // 2. Verify admin was created and password verifies
        System.out.print("[2] Verifying admin account... ");
        try {
            AdminDAO adminDAO = new AdminDAO();
            Optional<Admin> admin = adminDAO.findByUsername("admin");
            if (admin.isEmpty()) {
                System.out.println("FAIL: admin not found");
                allPassed = false;
            } else {
                boolean verified = PasswordUtil.verifyPassword("Admin@123",
                        admin.get().getPasswordHash());
                if (verified) {
                    System.out.println("OK (username=admin, password verified)");
                } else {
                    System.out.println("FAIL: password verification failed");
                    allPassed = false;
                }
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
            allPassed = false;
        }

        // 3. Verify parking slots are queryable
        System.out.print("[3] Querying parking slots... ");
        try {
            ParkingSlotDAO slotDAO = new ParkingSlotDAO();
            List<ParkingSlot> slots = slotDAO.findAll();
            if (slots.size() > 0) {
                System.out.println("OK (" + slots.size() + " slots found)");
            } else {
                System.out.println("FAIL: no slots found");
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
            allPassed = false;
        }

        // 4. Verify vehicles are queryable
        System.out.print("[4] Querying vehicles... ");
        try {
            VehicleDAO vehicleDAO = new VehicleDAO();
            List<Vehicle> vehicles = vehicleDAO.findAll();
            if (vehicles.size() > 0) {
                System.out.println("OK (" + vehicles.size() + " vehicles found)");
            } else {
                System.out.println("FAIL: no vehicles found");
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
            allPassed = false;
        }

        // 5. Shutdown the connection pool
        System.out.print("[5] Shutting down connection pool... ");
        try {
            DBConnection.getInstance().shutdown();
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
            allPassed = false;
        }

        System.out.println("\n=== Result: " + (allPassed ? "ALL CHECKS PASSED" : "SOME CHECKS FAILED") + " ===");
        System.exit(allPassed ? 0 : 1);
    }
}
