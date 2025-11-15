package gc.grivyzom.AnforaXP.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnforaDataManager {

    private final StorageEngine storage;
    private final AnforaUUIDManager anforaUUIDManager;

    public AnforaDataManager(StorageEngine storage, AnforaUUIDManager anforaUUIDManager) {
        this.storage = storage;
        this.anforaUUIDManager = anforaUUIDManager;
    }

    public void saveAnfora(AnforaData data) {
        storage.saveAnfora(data);
    }

    public AnforaData loadAnfora(String anforaId) {
        return storage.loadAnfora(anforaId);
    }

    public void deleteAnfora(String anforaId) {
        storage.deleteAnfora(anforaId);
    }

    public List<AnforaData> getAnforasByOwner(UUID ownerUUID) {
        List<AnforaData> ownerAnforas = new ArrayList<>();
        for (String anforaId : storage.getAllAnforaIds()) { // Use getAllAnforaIds from storage
            AnforaData anforaData = storage.loadAnfora(anforaId); // Load using the correct anforaId
            if (anforaData != null && anforaData.getOwnerUUID().equals(ownerUUID)) {
                ownerAnforas.add(anforaData);
            }
        }
        return ownerAnforas;
    }
}