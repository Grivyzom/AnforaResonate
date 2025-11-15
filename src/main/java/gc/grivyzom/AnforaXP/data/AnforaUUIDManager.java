package gc.grivyzom.AnforaXP.data;

import java.util.HashSet;
import java.util.Set;

public class AnforaUUIDManager {

    private final Set<String> placedAnforaUUIDs = new HashSet<>();

    public AnforaUUIDManager(StorageEngine storageEngine) {
        // Load all unique IDs of placed anforas from the database at startup
        Set<String> allUUIDs = storageEngine.getAllPlacedAnforaUUIDs();
        if (allUUIDs != null) {
            placedAnforaUUIDs.addAll(allUUIDs);
        }
    }

    /**
     * Registers a new anfora as being placed in the world.
     * @param uuid The unique ID of the anfora.
     */
    public void addPlacedAnfora(String uuid) {
        placedAnforaUUIDs.add(uuid);
    }

    /**
     * Unregisters an anfora that has been broken or removed from the world.
     * @param uuid The unique ID of the anfora.
     */
    public void removePlacedAnfora(String uuid) {
        placedAnforaUUIDs.remove(uuid);
    }

    /**
     * Checks if an anfora with the given UUID is already considered placed in the world.
     * @param uuid The unique ID of the anfora to check.
     * @return true if the anfora is already placed, false otherwise.
     */
    public boolean isAnforaPlaced(String uuid) {
        return placedAnforaUUIDs.contains(uuid);
    }

    /**
     * Gets the number of anforas currently placed in the world.
     * @return The count of placed anforas.
     */
    public int getPlacedAnforaCount() {
        return placedAnforaUUIDs.size();
    }
}
