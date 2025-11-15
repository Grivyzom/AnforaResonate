package gc.grivyzom.AnforaXP.data;

public class AnforaDataManager {

    private final StorageEngine storage;

    public AnforaDataManager(StorageEngine storage) {
        this.storage = storage;
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
}