package gc.grivyzom.AnforaXP.data;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int anforaCount;
    private boolean isActive; // Nuevo campo para el estado de la cuenta

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.anforaCount = 0;
        this.isActive = false; // Por defecto, la cuenta está inactiva
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getAnforaCount() {
        return anforaCount;
    }

    public void setAnforaCount(int anforaCount) {
        this.anforaCount = anforaCount;
    }

    public boolean isActive() { // Getter para el nuevo campo
        return isActive;
    }

    public void setActive(boolean active) { // Setter para el nuevo campo
        isActive = active;
    }
}
