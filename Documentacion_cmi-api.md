You can find the source code for CMI-API, and download it as a .jar file from github.com/Zrips/CMI-API

Add to pom.yml

<dependency>
  <groupId>com.github.Zrips</groupId>
  <artifactId>CMI-API</artifactId>
  <version>9.7.14.3</version>
</dependency>

<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
Player/User Object
Most things related to the player can be accessed through users object

CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
This can return NULL in some rare situations, so perform NPE check.

You can get the offline player object from this one by using

Player player = user.getPlayer();
This can be used to access some of the player’s data information even if he is offline. Keep in mind that this actually loads players’ information and is highly not recommended to be used to load hundreds or even thousands of players with this method as this will undoubtedly cause some strain on the server. You can tho update some of the player data but you will need to save it by (ONLY if the player is offline) using

player.saveData();
or by using

CMI.getInstance().save(player);
This is more safe approach as the player object will be checked for appropriate save.

Holograms
As of 9.6.5.0 version there is allot more efficient way in showing personal holograms to the players. These will only be shown to the target player without being saved or being processed by few default checks. Simple example of it:

// Code can be safely run in async, use this method to be Folia compatible
CMIScheduler.runTaskAsynchronously(() -> {
// Create hologram for defined location, player object and text lines which needs to be shown. Placeholders need to be translated before passing those over.
// This is specific constructor which will create lightweight hologram
CMIHologram h = new CMIHologram(loc, player, Arrays.asList(text));
// In case you want multi version support you can enable better hologram method for 1.19.3+ servers
if (Version.isCurrentEqualOrHigher(Version.v1_19_R3)) {
h.setNewDisplayMethod(true);
h.setBillboard(CMIBillboard.CENTER);
}
// Will remove itself after 20 ticks (1 second) so no need to create additional schedulers. Not setting timer will keep hologram visible for the player until player relogs or leaves area
h.setSelfDestructIn(20);
// This method only works when using this specific hologram builder and will show for predefined player in constructor only
h.showToPlayer();
});
To create new custom hologram which can be persistent and shown to everyone on server we will need center location and some unique name

CMIHologram holo = new CMIHologram("TestHologram", center);
Next lets set some lines for it with

holo.setLines(Arrays.asList("Line 1","Line 2"));
Lets add new hologram to cache. This is essential step to inform plugin that we have new hologram which should be included into cache. This will perform extra actions needed for hologram to work properly.

CMI.getInstance().getHologramManager().addHologram(holo);
And finally, you can update it for every player who is near it to show it.

holo.update();
Final code should look something like

CMIHologram holo = new CMIHologram("TestHologram", center);
holo.setLines(Arrays.asList("Line 1","Line 2"));
CMI.getInstance().getHologramManager().addHologram(holo);
holo.update();
Attention! If you want to save hologram file, use this. By default, hologram will be visible only until you remove it or server restarts.

holo.makePersistent();
If you want to save new information into file

CMI.getInstance().getHologramManager().save();
If you want to hide hologram temporary, then you can use

holo.hide();
This will only hide it until player reenters hologram area.

If you want to disable hologram entirely, then use

holo.disable();
Enabling hologram is as easy as it gets

holo.enable();
In case you have updated hologram lines and want then to be displayed. Use

holo.refresh();
Changing hologram location can be done with

holo.setLoc(center);
After which you will need to refresh hologram

And if you want to remove hologram, use

holo.remove();
Managers
Quite a few things can be accessed through appropriate managers. They all can be accessed through the basic method

CMI.getInstance().get[managerName]();
For example, to access the portal manager use

CMI.getInstance().getPortalManager();
Worth
Get Items worth
// Item stack used to get worth
ItemStack item;
WorthItem worth = CMI.getInstance().getWorthManager().getWorth(item);
if (worth == null){
// Worthless item so we can return null or 0D, whatever is needed in your case
return null;
}
// Buy price used in exploit detection
Double buyPrice  = worth.getBuyPrice();
// Sell price defines actual worth of the file
Double sellPrice = worth.getSellPrice();
Set items worth
Custom Events
CMIAfkEnterEvent – Fired when player enters AFK mode.

CMIAfkKickEvent – Fired when player should be kicked from server after being AFK.

CMIAfkLeaveEvent – Fired when players leaves AFK mode.

CMIAnvilItemRenameEvent – Fired on item rename in anvil.

CMIAnvilItemRepairEvent – Fired on item repair action with anvil.

CMIArmorChangeEvent – Fired when player changes items in armor slots.

CMIAsyncPlayerTeleportEvent – Fired when player is being teleported in Async mode. Can be canceled.

CMIBackpackOpenEvent – Fired on backpack open

CMIChequeCreationEvent – Fired before cheque is created

CMIChequeUsageEvent – Fired on cheque usage

CMIChunkChangeEvent – Fired when player changes chunk.

CMIConfigReloadEvent – Fired on config reload

CMIEventCommandEvent – Fired on event command

CMIIpBanEvent – Fired when IP gets ban.

CMIIpUnBanEvent – Fired when IP gets unban.

CMIHologramClickEvent – Fired on interaction with interactable hologram.

CMIPlayerBanEvent – Fired when player gets ban.

CMIPlayerFakeEntityInteractEvent – Fired when player interacts with fake entity, in most cases this will be hologram button interaction.

CMIPlayerItemsSellEvent – Fired on sell item action with command directly or when closing sell GUI

CMIPlayerItemsSellEvent – Fired on player item selling with worth command

CMIPlayerJailEvent – Fired on player jail

CMIPlayerKickEvent – Fired on player kick

CMIPlayerNickNameChangeEvent – Fired on player nickname change

CMIPlayerOpenArmorStandEditorEvent – Fired on player opening armor stand editor

CMIPlayerSitEvent – Fired on player sit action

CMIPlayerTeleportRequestEvent – Fired after player teleportation.

CMIPlayerUnBanEvent – Fired when player gets unban.

CMIPlayerUnjailEvent – Fired on player unjail

CMIPlayerUnVanishEvent – Fired when player exits vanish event.

CMIPlayerVanishEvent – Fired when player enters vanish event.

CMIPlayerWarnEvent – Fired when player gets a warning

CMIPlayerWarpEvent – Fired before warping player to target location

CMIPortalCreateEvent – Fired on nether portal creation event.

CMIPortalUseEvent – Fired on CMI portal use event.

CMIPvEStartEventAsync – Fired on pve start

CMIPvEEndEventAsync – Fired on pve end

CMIPvPStartEventAsync – Fired on pvpstart event.

CMIPvPEndEventAsync– Fired on pvpend event.

CMISelectionEvent – Fired on selection

CMISelectionVisualizationEvent – Fired before selection visualization starts showing. Can be canceled.

CMIStaffMessageEvent – Fired on staff message being sent

CMIUserBalanceChangeEvent – Fired on user balance change, if CMI Economy is used. Change types include: setBalance, Withdraw, Deposit.

