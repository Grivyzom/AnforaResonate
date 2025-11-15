package gc.grivyzom.AnforaXP.utils;

import org.bukkit.entity.Player;

public class ExperienceManager {

    public static void setTotalExperience(Player player, int exp) {
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        if (exp > 0) {
            player.giveExp(exp);
        }
    }

    public static int getTotalExperience(Player player) {
        return getTotalExperience(player.getLevel(), player.getExp());
    }

    public static int getTotalExperience(int level, float expBar) {
        int exp = 0;
        if (level >= 0 && level <= 15) {
            exp = (int) (Math.pow(level, 2) + 6 * level);
        } else if (level >= 16 && level <= 30) {
            exp = (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else if (level >= 31) {
            exp = (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
        exp += Math.round(getExpToNextLevel(level) * expBar);
        return exp;
    }

    private static int getExpToNextLevel(int level) {
        if (level >= 31) {
            return 9 * level - 158;
        } else if (level >= 16) {
            return 5 * level - 38;
        } else {
            return 2 * level + 7;
        }
    }
}
