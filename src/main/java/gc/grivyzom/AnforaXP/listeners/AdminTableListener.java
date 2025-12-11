package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.utils.AdminGuiManager;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class AdminTableListener implements Listener {

    private final AnforaMain plugin;
    private final AdminGuiManager adminGuiManager;
    private final MessageManager messageManager;

    public AdminTableListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.adminGuiManager = new AdminGuiManager(plugin);
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (ItemFactory.isAdminTable(item)) {
            Block block = event.getBlockPlaced();
            Player player = event.getPlayer();

            if (block.getState() instanceof TileState) {
                TileState state = (TileState) block.getState();
                PersistentDataContainer container = state.getPersistentDataContainer();

                // Marcar como mesa de administración
                container.set(ItemFactory.ADMIN_TABLE_KEY, PersistentDataType.STRING, "true");

                // Almacenar UUID del propietario
                container.set(ItemFactory.ADMIN_TABLE_OWNER_KEY, PersistentDataType.STRING,
                        player.getUniqueId().toString());

                // Almacenar nombre del propietario
                container.set(ItemFactory.ADMIN_TABLE_OWNER_NAME_KEY, PersistentDataType.STRING, player.getName());

                state.update();
                player.sendMessage(messageManager.getMessage("admin_table_placed"));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.LECTERN)
            return;

        if (block.getState() instanceof TileState) {
            TileState state = (TileState) block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();

            if (container.has(ItemFactory.ADMIN_TABLE_KEY, PersistentDataType.STRING)) {
                Player player = event.getPlayer();

                // Obtener UUID del propietario
                String ownerUUIDStr = container.get(ItemFactory.ADMIN_TABLE_OWNER_KEY, PersistentDataType.STRING);
                String ownerName = container.get(ItemFactory.ADMIN_TABLE_OWNER_NAME_KEY, PersistentDataType.STRING);
                UUID ownerUUID = ownerUUIDStr != null ? UUID.fromString(ownerUUIDStr) : null;

                // Verificar si el jugador es el propietario
                if (ownerUUID != null && !player.getUniqueId().equals(ownerUUID)) {
                    event.setCancelled(true);
                    player.sendMessage(messageManager.getMessage("admin_table_not_owner_break"));
                    return;
                }

                // Cancelar drop vanilla
                event.setDropItems(false);

                // Crear el ítem con los datos preservados
                ItemStack adminTableItem = ItemFactory.createAdminTableItem(plugin, ownerUUID, ownerName);

                // Dropear el ítem
                block.getWorld().dropItemNaturally(block.getLocation(), adminTableItem);

                player.sendMessage(messageManager.getMessage("admin_table_picked_up"));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LECTERN)
            return;

        if (block.getState() instanceof TileState) {
            TileState state = (TileState) block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();

            if (container.has(ItemFactory.ADMIN_TABLE_KEY, PersistentDataType.STRING)) {
                event.setCancelled(true); // Prevent opening the lectern book interface

                Player player = event.getPlayer();

                // Obtener UUID del propietario
                String ownerUUIDStr = container.get(ItemFactory.ADMIN_TABLE_OWNER_KEY, PersistentDataType.STRING);
                UUID ownerUUID = ownerUUIDStr != null ? UUID.fromString(ownerUUIDStr) : null;

                // Verificar si el jugador es el propietario
                if (ownerUUID != null && !player.getUniqueId().equals(ownerUUID)) {
                    String ownerName = container.get(ItemFactory.ADMIN_TABLE_OWNER_NAME_KEY, PersistentDataType.STRING);
                    player.sendMessage(messageManager.getMessage("admin_table_not_owner_interact")
                            .replace("{owner}", ownerName != null ? ownerName : "Desconocido"));
                    return;
                }

                adminGuiManager.openAdminGui(player);
            }
        }
    }

    public AdminGuiManager getAdminGuiManager() {
        return adminGuiManager;
    }
}
