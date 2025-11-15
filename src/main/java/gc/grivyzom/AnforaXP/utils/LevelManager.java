package gc.grivyzom.AnforaXP.utils;

import java.util.HashMap;
import java.util.Map;

public class LevelManager {

    public static class LevelInfo {
        private final int maxExperience;
        private final int upgradeCost;

        public LevelInfo(int maxExperience, int upgradeCost) {
            this.maxExperience = maxExperience;
            this.upgradeCost = upgradeCost;
        }

        public int getMaxExperience() {
            return maxExperience;
        }

        public int getUpgradeCost() {
            return upgradeCost;
        }
    }

    private static final Map<Integer, LevelInfo> levelData = new HashMap<>();

    static {
        // Level I is the base, its "upgrade cost" is the cost to reach Level II
        levelData.put(1, new LevelInfo(1395, 315));
        levelData.put(2, new LevelInfo(1695, 415));
        levelData.put(3, new LevelInfo(1995, 515));
        levelData.put(4, new LevelInfo(2295, 615));
        levelData.put(5, new LevelInfo(2595, 715));
        levelData.put(6, new LevelInfo(2895, 815));
        levelData.put(7, new LevelInfo(3195, 915));
        levelData.put(8, new LevelInfo(3495, 1015));
        levelData.put(9, new LevelInfo(3795, 1115));
        levelData.put(10, new LevelInfo(4095, 1215));
        levelData.put(11, new LevelInfo(4395, 1315));
        levelData.put(12, new LevelInfo(4695, 1415));
        levelData.put(13, new LevelInfo(4995, 1515));
        levelData.put(14, new LevelInfo(5295, 1615));
        levelData.put(15, new LevelInfo(5595, 1715));
        levelData.put(16, new LevelInfo(5895, 1815));
        levelData.put(17, new LevelInfo(6195, 1915));
        levelData.put(18, new LevelInfo(6495, 2015));
        levelData.put(19, new LevelInfo(6795, 2115));
        levelData.put(20, new LevelInfo(7095, 0)); // Max level, no upgrade cost
    }

    public static LevelInfo getLevelInfo(int level) {
        return levelData.get(level);
    }

    public static int getMaxLevel() {
        return 20;
    }
}
