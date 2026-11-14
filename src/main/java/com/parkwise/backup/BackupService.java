package com.parkwise.backup;

import com.parkwise.dao.ParkingSlotDAO;
import com.parkwise.dao.VehicleDAO;
import com.parkwise.model.ParkingSlot;
import com.parkwise.model.Vehicle;
import com.parkwise.util.AppConfig;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serialization-based backup and restore service.
 * Snapshots vehicles and slots to a binary file that can be restored later.
 *
 * <p>Note: This backup covers in-memory vehicle and slot data only.
 * For a full database backup, run mysqldump on parkwise_db.</p>
 */
public class BackupService {

    private static final Logger LOG = Logger.getLogger(BackupService.class.getName());
    private static final DateTimeFormatter FILE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final VehicleDAO     vehicleDAO  = new VehicleDAO();
    private final ParkingSlotDAO slotDAO     = new ParkingSlotDAO();

    /**
     * Creates a serialized backup snapshot of vehicles and slots.
     *
     * @return the path of the written backup file
     */
    public String createBackup() throws IOException, SQLException {
        // Load data from DB
        List<Vehicle>     vehicles = vehicleDAO.findAll();
        List<ParkingSlot> slots    = slotDAO.findAll();

        BackupSnapshot snapshot = new BackupSnapshot(vehicles, slots);

        // Ensure backup directory exists
        File dir = new File(AppConfig.BACKUP_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create backup directory: " + dir.getAbsolutePath());
        }

        String filename = "parkwise_backup_" + LocalDateTime.now().format(FILE_FMT) + ".bak";
        File   file     = new File(dir, filename);

        // Byte stream: ObjectOutputStream wraps FileOutputStream
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(snapshot);
        }

        LOG.info("Backup created: " + file.getAbsolutePath() + " — " + snapshot);
        return file.getAbsolutePath();
    }

    /**
     * Restores a snapshot from a backup file.
     * Validates that the file contains a {@link BackupSnapshot} before accepting it.
     *
     * @param backupFilePath absolute path to the .bak file
     * @return the restored snapshot (caller decides what to do with it)
     */
    public BackupSnapshot restoreBackup(String backupFilePath)
            throws IOException, ClassNotFoundException {

        File file = new File(backupFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Backup file not found: " + backupFilePath);
        }

        // Byte stream: ObjectInputStream wraps FileInputStream
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (!(obj instanceof BackupSnapshot)) {
                throw new InvalidObjectException(
                    "File does not contain a valid ParkWise backup snapshot");
            }
            BackupSnapshot snapshot = (BackupSnapshot) obj;
            LOG.info("Backup restored: " + snapshot);
            return snapshot;
        }
    }

    /** Lists backup files in the configured backup directory. */
    public File[] listBackupFiles() {
        File dir = new File(AppConfig.BACKUP_DIR);
        if (!dir.exists()) return new File[0];
        File[] files = dir.listFiles((d, name) -> name.endsWith(".bak"));
        return files != null ? files : new File[0];
    }
}
