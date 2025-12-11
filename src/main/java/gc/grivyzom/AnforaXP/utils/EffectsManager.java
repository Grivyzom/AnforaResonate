package gc.grivyzom.AnforaXP.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EffectsManager {

    public static void playLevelUpEffect(Location location) {
        if (location == null || location.getWorld() == null) return;
        
        location.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, location.clone().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public static void playWithdrawEffect(Player player, Location location) {
        if (location == null || location.getWorld() == null) return;
        
        location.getWorld().spawnParticle(Particle.WITCH, location.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
        player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public static void playDepositEffect(Player player, Location location) {
        if (location == null || location.getWorld() == null) return;
        
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
        player.playSound(location, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 1.0f, 1.0f);
    }
}
