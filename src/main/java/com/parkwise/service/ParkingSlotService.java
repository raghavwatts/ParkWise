package com.parkwise.service;

import com.parkwise.dao.ParkingSlotDAO;
import com.parkwise.model.ParkingSlot;
import com.parkwise.model.SlotStatus;
import com.parkwise.model.VehicleType;
import com.parkwise.util.ValidationUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for parking slot management.
 */
public class ParkingSlotService {

    private final ParkingSlotDAO slotDAO;

    public ParkingSlotService() {
        this.slotDAO = new ParkingSlotDAO();
    }

    /** Adds a new parking slot. */
    public ParkingSlot addSlot(String slotNumber, VehicleType type) throws SQLException {
        ValidationUtil.require(!ValidationUtil.isBlank(slotNumber), "Slot number is required");
        ValidationUtil.require(type != null, "Slot type is required");
        ValidationUtil.require(ValidationUtil.isValidSlotNumber(slotNumber),
                "Invalid slot number format (e.g. A01)");

        if (slotDAO.existsBySlotNumber(slotNumber.trim().toUpperCase())) {
            throw new IllegalArgumentException("Slot " + slotNumber + " already exists");
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(slotNumber.trim().toUpperCase());
        slot.setSlotType(type);
        slot.setSlotStatus(SlotStatus.AVAILABLE);
        slot.setActive(true);
        int id = slotDAO.insert(slot);
        slot.setId(id);
        return slot;
    }

    /** Returns all slots. */
    public List<ParkingSlot> getAllSlots() throws SQLException {
        return slotDAO.findAll();
    }

    /** Returns available slots for the given vehicle type. */
    public List<ParkingSlot> getAvailableSlots(VehicleType type) throws SQLException {
        return slotDAO.findAvailableByType(type);
    }

    /** Updates a slot's details. Cannot deactivate an occupied/reserved slot. */
    public void updateSlot(ParkingSlot slot) throws SQLException {
        ParkingSlot existing = slotDAO.findById(slot.getId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        // Guard: cannot deactivate an occupied or reserved slot
        if (!slot.isActive() && existing.isActive()) {
            SlotStatus s = existing.getSlotStatus();
            if (s == SlotStatus.OCCUPIED || s == SlotStatus.RESERVED) {
                throw new IllegalStateException(
                    "Cannot deactivate slot " + existing.getSlotNumber()
                    + " because it is currently " + s.getDisplayName() + ".");
            }
        }
        slotDAO.update(slot);
    }

        /** Filters slots by status. */
    public List<ParkingSlot> filterByStatus(SlotStatus status) throws SQLException {
        // Stream filter + collect
        return slotDAO.findAll().stream()
                .filter(s -> s.getSlotStatus() == status)
                .sorted(Comparator.comparing(ParkingSlot::getSlotNumber))
                .collect(Collectors.toList());
    }

    /** Returns dashboard counts: [total, available, occupied, reserved]. */
    public int[] getDashboardCounts() throws SQLException {
        return slotDAO.countByStatus();
    }
}
