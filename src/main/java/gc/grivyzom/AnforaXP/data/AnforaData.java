package gc.grivyzom.AnforaXP.data;

import org.bukkit.Location;

import java.util.UUID;

public class AnforaData {

    private final String id;
    private final UUID uniqueId;
    private final UUID ownerUUID;
    private final String ownerName; // Nuevo campo para el nombre del propietario
    private Location location;
    private int level;
    private int experience;

    public AnforaData(String id, UUID uniqueId, UUID ownerUUID, String ownerName) { // Constructor actualizado
        this.id = id;
        this.uniqueId = uniqueId;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.level = 1; // Initialize level to 1
        this.experience = 0;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() { // Getter para el nombre del propietario
        return ownerName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }
}

