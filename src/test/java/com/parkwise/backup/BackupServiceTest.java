package com.parkwise.backup;

import com.parkwise.model.ParkingSlot;
import com.parkwise.model.SlotStatus;
import com.parkwise.model.Vehicle;
import com.parkwise.model.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for serialization backup / restore without a database connection.
 */
class BackupServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Snapshot serializes and deserializes successfully")
    void serializeDeserialize_roundTrip() throws Exception {
        Vehicle v = new Vehicle(1, "KA01AB1234", VehicleType.TWO_WHEELER,
                "Test Owner", "9876543210", LocalDateTime.now());
        ParkingSlot s = new ParkingSlot(1, "A01", VehicleType.TWO_WHEELER,
                SlotStatus.AVAILABLE, true);

        BackupSnapshot original = new BackupSnapshot(List.of(v), List.of(s));

        // Serialize to a temp file
        File file = tempDir.resolve("test.bak").toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(original);
        }

        // Deserialize and verify
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            assertInstanceOf(BackupSnapshot.class, obj);
            BackupSnapshot restored = (BackupSnapshot) obj;
            assertEquals(1, restored.getVehicles().size());
            assertEquals(1, restored.getSlots().size());
            assertEquals("KA01AB1234", restored.getVehicles().get(0).getRegistrationNumber());
            assertEquals("A01", restored.getSlots().get(0).getSlotNumber());
        }
    }

    @Test
    @DisplayName("Restoring a non-existent file throws FileNotFoundException")
    void restoreNonExistentFile_throws() {
        BackupService service = new BackupService();
        assertThrows(FileNotFoundException.class,
                () -> service.restoreBackup("/no/such/file.bak"));
    }

    @Test
    @DisplayName("Cloning a slot produces an equal but distinct object")
    void slotClone_isDistinct() {
        ParkingSlot original = new ParkingSlot(1, "B01", VehicleType.FOUR_WHEELER,
                SlotStatus.AVAILABLE, true);
        ParkingSlot clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.getSlotNumber(), clone.getSlotNumber());
        assertEquals(original.getSlotType(),   clone.getSlotType());
    }
}
