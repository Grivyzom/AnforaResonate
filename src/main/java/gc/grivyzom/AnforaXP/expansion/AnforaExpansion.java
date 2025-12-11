package gc.grivyzom.AnforaXP.expansion;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.PlayerData;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import gc.grivyzom.AnforaXP.utils.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnforaExpansion extends PlaceholderExpansion {

    private final AnforaMain plugin;

    public AnforaExpansion(AnforaMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "anforaxp";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Global placeholders
        if (params.equalsIgnoreCase("global_total_anforas")) {
            return String.valueOf(plugin.getAnforaDataManager().getAllAnforas().size());
        }

        if (params.equalsIgnoreCase("global_total_xp")) {
            long totalXp = plugin.getAnforaDataManager().getAllAnforas().stream()
                    .mapToLong(AnforaData::getExperience)
                    .sum();
            return NumberFormatter.format(totalXp);
        }

        if (params.startsWith("anfora_")) {
            Block targetBlock = player.getTargetBlock(null, 5);
            String anforaId = plugin.getAnforaDataManager().getAnforaIdByLocation(targetBlock.getLocation());
            if (anforaId != null) {
                AnforaData anfora = plugin.getAnforaDataManager().loadAnfora(anforaId);
                if (anfora != null) {
                    switch (params.toLowerCase()) {
                        case "anfora_level":
                            return String.valueOf(anfora.getLevel());
                        case "anfora_exp":
                            return String.valueOf(anfora.getExperience());
                        case "anfora_exp_max":
                            return String.valueOf(LevelManager.getXpCapacityForLevel(anfora.getLevel()));
                        case "anfora_exp_formatted":
                            return NumberFormatter.format(anfora.getExperience());
                        case "anfora_exp_max_formatted":
                            return NumberFormatter.format(LevelManager.getXpCapacityForLevel(anfora.getLevel()));
                    }
                }
            }
            return "N/A";
        }

        PlayerData playerData = plugin.getPlayerDataManager().loadPlayer(player.getUniqueId());
        if (playerData == null) {
            return "0";
        }

        if (params.equalsIgnoreCase("count")) {
            return String.valueOf(playerData.getAnforaCount());
        }

        if (params.equalsIgnoreCase("anforas_max")) {
            return String.valueOf(getMaxAnforasForPlayer(player));
        }

        if (params.equalsIgnoreCase("is_active")) {
            return playerData.isActive() ? "true" : "false";
        }

        List<AnforaData> anforas = plugin.getAnforaDataManager().getAnforasByOwner(player.getUniqueId());
        if (params.equalsIgnoreCase("total_xp")) {
            long totalXp = anforas.stream().mapToLong(AnforaData::getExperience).sum();
            return String.valueOf(totalXp);
        }

        if (params.equalsIgnoreCase("total_xp_formatted")) {
            long totalXp = anforas.stream().mapToLong(AnforaData::getExperience).sum();
            return NumberFormatter.format(totalXp);
        }

        if (params.equalsIgnoreCase("total_capacity")) {
            long totalCapacity = anforas.stream()
                    .mapToLong(anfora -> LevelManager.getXpCapacityForLevel(anfora.getLevel()))
                    .sum();
            return String.valueOf(totalCapacity);
        }

        if (params.equalsIgnoreCase("total_capacity_formatted")) {
            long totalCapacity = anforas.stream()
                    .mapToLong(anfora -> LevelManager.getXpCapacityForLevel(anfora.getLevel()))
                    .sum();
            return NumberFormatter.format(totalCapacity);
        }

        return null;
    }

    /**
     * Determina el número máximo de ánforas que un jugador puede tener
     * basándose en sus permisos (anforaresonante.max.N)
     * 
     * @param player El jugador a verificar
     * @return El número máximo de ánforas permitidas
     */
    private int getMaxAnforasForPlayer(Player player) {
        // Verificar permiso ilimitado
        if (player.hasPermission("anforaresonante.max.unlimited")) {
            return Integer.MAX_VALUE;
        }

        // Buscar el permiso más alto anforaresonante.max.N
        int maxAnforas = 1; // Por defecto 1
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("anforaresonante.max." + i)) {
                return i;
            }
        }

        return maxAnforas;
    }
}
