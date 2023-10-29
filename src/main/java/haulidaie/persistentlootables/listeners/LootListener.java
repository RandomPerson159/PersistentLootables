package haulidaie.persistentlootables.listeners;

import haulidaie.persistentlootables.PersistentLootables;
import haulidaie.persistentlootables.utilities.LootUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;

import static haulidaie.persistentlootables.utilities.LootUtils.DecodeFrames;
import static haulidaie.persistentlootables.utilities.LootUtils.DecodeHeads;

public class LootListener implements Listener {
    private final PersistentLootables plugin = PersistentLootables.GetInstance();

    enum lootType {
        none,
        mystery,
        individual,
        everyone,
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!e.getView().getTitle().equals(plugin.lootTitle)) {
            //Ready variables
            Player p = (Player) e.getPlayer();
            UUID pid = p.getUniqueId();
            Inventory inv = e.getInventory();
            Block b = p.getTargetBlock(null, 10);
            BlockState bState = b.getState();

            if (bState instanceof TileState tState) {
                //Targeted block has a valid tile state to store data
                PersistentDataContainer c = tState.getPersistentDataContainer();
                NamespacedKey invKey = new NamespacedKey(plugin, "inventoryLoot");
                NamespacedKey typeKey = new NamespacedKey(plugin, "lootType");
                NamespacedKey amountKey = new NamespacedKey(plugin, "lootAmount");
                NamespacedKey timeKey = new NamespacedKey(plugin, "lootTime");
                NamespacedKey notifyKey = new NamespacedKey(plugin, "lootNotify");

                //Check if the event's block is a lootable container
                if (c.has(invKey, PersistentDataType.STRING)) {
                    //Choose some loot to put in the accessible inventory
                    ArrayList<ItemStack> items = LootUtils.DecodeItems(c.get(invKey, PersistentDataType.STRING));
                    lootType type = lootType.valueOf(c.get(typeKey, PersistentDataType.STRING));
                    int amount = c.get(amountKey, PersistentDataType.INTEGER);
                    double time = c.get(timeKey, PersistentDataType.DOUBLE);
                    HashMap<UUID, Long> playerTimes = LootUtils.LoadPlayerTimes(tState);
                    boolean notify = c.get(notifyKey, PersistentDataType.BOOLEAN);

                    //Create a new hashmap if the block doesn't have one
                    if (playerTimes == null)
                        playerTimes = new HashMap<>();

                    //Check if the current player has looted recently
                    Long milliTime = Math.round(time * 60 * 1000);
                    playerTimes = LootUtils.RemoveOverduePlayers(playerTimes, milliTime);
                    Long timestamp = playerTimes.get(pid);
                    Long currentTime = System.currentTimeMillis();

                    //Behaviour for everyone boxes
                    if (type == lootType.everyone && !playerTimes.isEmpty()) {
                        Long first = currentTime;
                        for (Long t : playerTimes.values()) {
                            if(t < first)
                                first = t;
                        }

                        timestamp = first;
                    }

                    //Default spawning behaviour
                    if (timestamp != null) {
                        if (notify) {
                            p.sendMessage(plugin.textColor + "Loot hasn't respawned yet ❤");
                            Double until = (((timestamp + milliTime)-currentTime) * 0.001 / 60);
                            p.sendMessage(plugin.textColor + "It will respawn in: " + String.format("%.2f", until) + " minutes...");
                        }

                        return;
                    } else {
                        //Player isn't in the loot times so add
                        playerTimes.put(pid, currentTime);

                        if (notify)
                            p.sendMessage(plugin.textColor + "You found a lootable ❤\nIt will respawn in: " + time + " minutes...");
                    }

                    LootUtils.SavePlayerTimes(tState, playerTimes);

                    if (items != null && !items.isEmpty()) {
                        inv.clear(); //Clear the existing items

                        int count = Math.min(items.size(), amount); //How many items to choose
                        ArrayList<Integer> added = new ArrayList<>();

                        for (int i = 0; i < count; i++) {
                            int index = i;

                            if (amount < items.size()) {
                                index = (int) Math.round(Math.random() * (items.size() - 1));

                                if (type != lootType.mystery && added.contains(index)) {
                                    //Retry the random loot choice for individual & everyone boxes if the item has already been added
                                    index = (int) Math.round(Math.random() * (items.size() - 1));

                                    //If the retry didn't work just skip adding this item
                                    if (added.contains(index))
                                        continue;
                                }
                            }

                            ItemStack choice = items.get(index);
                            int attempt = (int) Math.round(Math.random() * 26);

                            if (inv.getItem(attempt) == null) {
                                inv.setItem(attempt, choice);
                            } else {
                                inv.addItem(choice);
                            }

                            added.add(index);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals(plugin.lootTitle)) {
            //Ready variables
            Player p = (Player) e.getPlayer();
            Inventory inv = e.getInventory();
            Block b = p.getTargetBlock(null, 10);
            BlockState bState = b.getState();

            if (bState instanceof TileState tState) {
                //Targeted block has a valid tile state to store data
                PersistentDataContainer c = ((TileState) bState).getPersistentDataContainer();
                NamespacedKey invKey = new NamespacedKey(plugin, "inventoryLoot");

                //If the event's block is a lootable container save the contents
                ArrayList<ItemStack> prunedItems = new ArrayList<>();

                Arrays.stream(inv.getContents()).filter(itemStack -> itemStack != null).forEach(itemStack -> prunedItems.add(itemStack));

                LootUtils.SaveLoot(tState, prunedItems);
                p.sendMessage(plugin.textColor + "Loot contents saved ❤");
            }
        }
    }

    @EventHandler
    public void onHeadBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();
        boolean operator = e.getPlayer().hasPermission("persistentlootables.admin");

        if(type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD) {
            Chunk chunk = block.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            NamespacedKey keyHeads = new NamespacedKey(plugin, "lootHeads");
            boolean found = false;

            if(pdc.has(keyHeads, PersistentDataType.STRING)) {
                ArrayList<LootUtils.HeadData> heads = DecodeHeads(pdc.get(keyHeads, PersistentDataType.STRING));

                if(heads != null) {
                    for(LootUtils.HeadData h : heads) {
                        if(h.location.equals(block.getLocation())) {
                            //Head data found! (Player is breaking a lootable head)
                            found = true;
                            break;
                        }
                    }
                }
            }

            if(!operator && !found)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFrameDamage(EntityDamageByEntityEvent e) {
        Entity target = e.getEntity();
        boolean operator = e.getDamager().hasPermission("persistentlootables.admin");

        if(target instanceof ItemFrame) {
            Chunk chunk = target.getLocation().getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            NamespacedKey keyHeads = new NamespacedKey(plugin, "lootFrames");
            boolean found = false;

            if(pdc.has(keyHeads, PersistentDataType.STRING)) {
                ArrayList<LootUtils.FrameData> frames = DecodeFrames(pdc.get(keyHeads, PersistentDataType.STRING));

                if(frames != null) {
                    for(LootUtils.FrameData f : frames) {
                        if(f.location.equals(target.getLocation())) {
                            //Frame data found! (Player is breaking a lootable frame)
                            found = true;
                            break;
                        }
                    }
                }
            }

            if(!operator && !found)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFrameInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity target = e.getRightClicked();
        boolean operator = p.hasPermission("persistentlootables.admin");

        if(target instanceof ItemFrame) {
            Chunk chunk = target.getLocation().getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            NamespacedKey keyHeads = new NamespacedKey(plugin, "lootFrames");
            boolean found = false;

            if(pdc.has(keyHeads, PersistentDataType.STRING)) {
                ArrayList<LootUtils.FrameData> frames = DecodeFrames(pdc.get(keyHeads, PersistentDataType.STRING));

                if(frames != null) {
                    for(LootUtils.FrameData f : frames) {
                        if(f.location.equals(target.getLocation())) {
                            //Frame data found! (Player is breaking a lootable frame)
                            found = true;
                            break;
                        }
                    }
                }
            }

            if(!operator && !found)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFrameBreak(HangingBreakByEntityEvent e) {
        HangingBreakEvent.RemoveCause cause = e.getCause();

        if(cause != HangingBreakEvent.RemoveCause.ENTITY) {
            e.setCancelled(true);
            return;
        }

        Entity remover = e.getRemover();
        Entity target = e.getEntity();
        boolean operator = remover.hasPermission("persistentlootables.admin");

        if(target instanceof ItemFrame) {
            if(!operator)
                e.setCancelled(true);
        }
    }
}
