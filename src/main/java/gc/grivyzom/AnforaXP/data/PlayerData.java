package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaType;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int anforaCount;
    private boolean isActive; // Nuevo campo para el estado de la cuenta
    private AnforaType anforaType; // Nuevo campo para el tipo de ánfora del jugador

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.anforaCount = 0;
        this.isActive = false; // Por defecto, la cuenta está inactiva
        this.anforaType = null; // Inicializar a null
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

    public AnforaType getAnforaType() {
        return anforaType;
    }

    public void setAnforaType(AnforaType anforaType) {
        this.anforaType = anforaType;
    }
}
