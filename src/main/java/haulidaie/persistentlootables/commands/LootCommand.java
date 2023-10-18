package haulidaie.persistentlootables.commands;

import haulidaie.persistentlootables.PersistentLootables;
import haulidaie.persistentlootables.utilities.LootUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class LootCommand implements CommandExecutor {
    private final PersistentLootables plugin = PersistentLootables.GetInstance();

    enum lootType {
        none,
        mystery,
        individual,
        everyone,
    }

    public TileState GetTargetLoot(Player p) {
        Block b = p.getTargetBlock(null, 10);
        BlockState bState = b.getState();

        if (bState instanceof TileState tState)
            return tState;

        return null;
    }

    public boolean CreateLootContainer(Player p, lootType type) {
        //Create a loot container at the player's target block
        Block b = p.getTargetBlock(null, 10);
        BlockState bState = b.getState();

        if (bState instanceof TileState tState) {
            //Targeted block has a valid tile state to store data
            PersistentDataContainer c = tState.getPersistentDataContainer();
            NamespacedKey invKey = new NamespacedKey(plugin, "inventoryLoot");

            if(c.has(invKey, PersistentDataType.STRING)) {
                p.sendMessage(plugin.textColor + "This block already contains loot data ❤\nYou can delete it with /loot destroy");
                return false;
            }

            if(tState instanceof InventoryHolder invHolder) {
                Inventory invOriginal = invHolder.getInventory();
                Inventory invLoot = Bukkit.createInventory(invHolder, 27, plugin.lootTitle);

                //Value defaults determined by loot container type
                int dAmount = 1;
                double dTime = 1.0;
                boolean dNotify = false;
                String dEffect = "";
                String dSound = "";

                //Set defaults
                switch (type) {
                    case mystery -> {
                        dAmount = 1;
                        dTime = 30.0;
                        dNotify = true;
                    }
                    case individual -> {
                        dAmount = 27;
                        dTime = 60.0;
                        dNotify = true;
                    }
                    case everyone -> {
                        dAmount = 27;
                        dTime = 20.0;
                        dNotify = false;
                    }
                }

                //Reset or create the saved loot data
                c.set(invKey, PersistentDataType.STRING, "");
                c.set(new NamespacedKey(plugin, "lootType"), PersistentDataType.STRING, type.toString());
                c.set(new NamespacedKey(plugin, "lootAmount"), PersistentDataType.INTEGER, dAmount);
                c.set(new NamespacedKey(plugin, "lootTime"), PersistentDataType.DOUBLE, dTime);
                c.set(new NamespacedKey(plugin, "playerTimes"), PersistentDataType.STRING, "");
                c.set(new NamespacedKey(plugin, "lootNotify"), PersistentDataType.BOOLEAN, dNotify);
                c.set(new NamespacedKey(plugin, "lootEffect"), PersistentDataType.STRING, dEffect);
                c.set(new NamespacedKey(plugin, "lootNoise"), PersistentDataType.STRING, dSound);
                tState.update();

                //Open the loot inventory to add lootable items
                p.openInventory(invLoot);
                return true;
            }
        }

        return false;
    }

    public boolean DestroyLootContainer(Player p) {
        TileState tState = GetTargetLoot(p);

        if (tState != null) {
            PersistentDataContainer c = tState.getPersistentDataContainer();

            if(c.has(new NamespacedKey(plugin, "inventoryLoot"), PersistentDataType.STRING)) {
                c.remove(new NamespacedKey(plugin, "inventoryLoot"));
                c.remove(new NamespacedKey(plugin, "lootType"));
                c.remove(new NamespacedKey(plugin, "lootAmount"));
                c.remove(new NamespacedKey(plugin, "lootTime"));
                c.remove(new NamespacedKey(plugin, "playerTimes"));
                c.remove(new NamespacedKey(plugin, "lootNotify"));
                c.remove(new NamespacedKey(plugin, "lootEffect"));
                c.remove(new NamespacedKey(plugin, "lootNoise"));

                tState.update();
                return true;
            }
        }

        return false;
    }

    public boolean SetLootType(Player p, String s) {
        lootType type = lootType.none;
        switch (s) {
            case("mystery"): type = lootType.mystery;
            case("individual"): type = lootType.individual;
            case("everyone"): type = lootType.everyone;
        }

        TileState tState = GetTargetLoot(p);

        if (tState != null && type != lootType.none) {
            PersistentDataContainer c = tState.getPersistentDataContainer();
            c.set(new NamespacedKey(plugin, "lootType"), PersistentDataType.STRING, type.toString());
            tState.update();

            return true;
        }

        return false;
    }

    public boolean SetLootAmount(Player p, String s) {
        TileState tState = GetTargetLoot(p);
        int a = Integer.parseInt(s);

        if (tState != null && a >= 0 && a <= 27) {
            PersistentDataContainer c = tState.getPersistentDataContainer();
            c.set(new NamespacedKey(plugin, "lootAmount"), PersistentDataType.INTEGER, a);
            tState.update();

            return true;
        }

        return false;
    }

    public boolean SetLootTime(Player p, String s) {
        TileState tState = GetTargetLoot(p);
        Double t = Double.parseDouble(s);

        if (tState != null && t >= 0.0) {
            PersistentDataContainer c = tState.getPersistentDataContainer();
            c.set(new NamespacedKey(plugin, "lootTime"), PersistentDataType.DOUBLE, t);
            tState.update();

            return true;
        }

        return false;
    }

    public boolean SetLootNotify(Player p, boolean b) {
        TileState tState = GetTargetLoot(p);

        if (tState != null) {
            PersistentDataContainer c = tState.getPersistentDataContainer();
            c.set(new NamespacedKey(plugin, "lootNotify"), PersistentDataType.BOOLEAN, b);
            tState.update();

            return true;
        }

        return false;
    }

    public boolean OpenLoot(Player p) {
        TileState tState = GetTargetLoot(p);

        if (tState != null) {
            if(tState instanceof InventoryHolder invHolder) {
                Inventory inv = Bukkit.createInventory(invHolder, 27, plugin.lootTitle);
                LootUtils.LoadLoot(tState, inv);

                //Open the loot inventory to edit lootable items
                p.openInventory(inv);
                return true;
            }
        }

        return false;
    }

    public boolean DisplayLootInfo(Player p) {
        TileState tState = GetTargetLoot(p);

        if (tState != null) {
            PersistentDataContainer c = tState.getPersistentDataContainer();
            NamespacedKey invKey = new NamespacedKey(plugin, "inventoryLoot");
            NamespacedKey typeKey = new NamespacedKey(plugin, "lootType");
            NamespacedKey amountKey = new NamespacedKey(plugin, "lootAmount");
            NamespacedKey timeKey = new NamespacedKey(plugin, "lootTime");
            NamespacedKey notifyKey = new NamespacedKey(plugin, "lootNotify");

            if (c.has(invKey, PersistentDataType.STRING)) {
                Block b = tState.getLocation().getBlock();

                p.sendMessage(plugin.textColor + "Lootable Check ==================" + "\n"
                        + "Coords:    " + b.getX() + " " + b.getY() + " " + b.getZ() + "\n"
                        + "Type:       " + c.get(typeKey, PersistentDataType.STRING) + "\n"
                        + "Amount:    " + c.get(amountKey, PersistentDataType.INTEGER) + " items\n"
                        + "Time:       " + c.get(timeKey, PersistentDataType.DOUBLE) + " minutes\n"
                        + "Notify:     " + c.get(notifyKey, PersistentDataType.BOOLEAN) + "\n");

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            switch (args[0]) {
                case ("create") -> {
                    if(args.length > 1 && !args[1].isEmpty()) {
                        lootType type = lootType.none;
                        switch (args[1]) {
                            case("mystery"): type = lootType.mystery; break;
                            case("individual"): type = lootType.individual; break;
                            case("everyone"): type = lootType.everyone; break;
                        }

                        if(type == lootType.none) {
                            p.sendMessage(plugin.textColor + "Loot container failed to be created ❤\nAvailable loot types: mystery, individual, everyone");
                            return true;
                        }

                        if (CreateLootContainer(p, type)) {
                            p.sendMessage(plugin.textColor + "Loot container successfully created ❤");
                        } else {
                            p.sendMessage(plugin.textColor + "Loot container failed to be created ❤\n/loot create type");
                        }

                        return true;
                    }
                }
                case ("destroy") -> {
                    if (DestroyLootContainer(p)) {
                        p.sendMessage(plugin.textColor + "Loot container successfully destroyed ❤");
                    } else {
                        p.sendMessage(plugin.textColor + "Loot container could not be destroyed ❤");
                    }

                    return true;
                }
                case ("check") -> {
                    if(!DisplayLootInfo(p))
                        p.sendMessage(plugin.textColor + "Loot info could not be displayed ❤");

                    return true;
                }
                case("set") -> {
                    if(args.length > 1 && !args[1].isEmpty()) {
                        //All alterable data of a lootable container
                        switch (args[1]) {
                            case ("type") -> {
                                if (args.length > 2 && !args[2].isEmpty()) {
                                    //Change the type of loot container
                                    if (SetLootType(p, args[2])) {
                                        p.sendMessage(plugin.textColor + "Loot type successfully updated ❤");
                                    } else {
                                        p.sendMessage(plugin.textColor + "Loot type failed to update ❤\nAvailable loot types: mystery, individual, everyone");
                                    }

                                    return true;
                                }
                            }
                            case ("amount") -> {
                                if (args.length > 2 && !args[2].isEmpty()) {
                                    if (SetLootAmount(p, args[2])) {
                                        p.sendMessage(plugin.textColor + "Loot amount successfully updated ❤");
                                    } else {
                                        p.sendMessage(plugin.textColor + "Loot amount failed to update ❤\nMust be an integer within 0 & 27");
                                    }

                                    return true;
                                }
                            }
                            case ("items") -> {
                                if (OpenLoot(p)) {
                                    p.sendMessage(plugin.textColor + "Loot loaded for editing successfully ❤");
                                } else {
                                    p.sendMessage(plugin.textColor + "Loot failed to load for editing ❤");
                                }

                                return true;
                            }
                            case ("time") -> {
                                if (args.length > 2 && !args[2].isEmpty()) {
                                    if (SetLootTime(p, args[2])) {
                                        p.sendMessage(plugin.textColor + "Loot timer successfully updated ❤");
                                    } else {
                                        p.sendMessage(plugin.textColor + "Loot timer failed to update ❤\nMust be a positive amount of minutes (decimals allowed)");
                                    }

                                    return true;
                                }
                            }
                            case ("notify") -> {
                                if (args.length > 2 && !args[2].isEmpty()) {
                                    boolean b = Boolean.parseBoolean(args[2]);
                                    if (SetLootNotify(p, b)) {
                                        if(b) {
                                            p.sendMessage(plugin.textColor + "Loot notification set to TRUE ❤");
                                        } else {
                                            p.sendMessage(plugin.textColor + "Loot notification set to FALSE ❤");
                                        }
                                    } else {
                                        p.sendMessage(plugin.textColor + "Loot notification failed to update ❤\nMust be a boolean (true or false)");
                                    }

                                    return true;
                                }
                            }
                        }
                    }
                }
            }

            p.sendMessage(plugin.textColor + "Unknown loot command ❤\nSub-commands: /loot (create, set, check, destroy)");
            return true;
        }

        return false;
    }
}
