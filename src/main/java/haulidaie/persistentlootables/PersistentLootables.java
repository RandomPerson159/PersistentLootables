package haulidaie.persistentlootables;

import haulidaie.persistentlootables.commands.BypassCommand;
import haulidaie.persistentlootables.commands.LootCommand;
import haulidaie.persistentlootables.commands.LootTabCompletion;
import haulidaie.persistentlootables.listeners.LootListener;
import haulidaie.persistentlootables.listeners.ProtectListener;
import haulidaie.persistentlootables.utilities.LootUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public final class PersistentLootables extends JavaPlugin {
    private static PersistentLootables instance;
    public ChatColor textColor = ChatColor.WHITE;
    public String lootTitle = ChatColor.BOLD + "Loot Table";
    public int randomPercent = 0;
    public ArrayList<Material> allowedInteractables = new ArrayList<>();

    private boolean protect = false;
    private boolean allowExplosions = false;
    private ArrayList<UUID> bypassers = new ArrayList<>();

    public static PersistentLootables GetInstance() {
        return instance;
    }

    public boolean Protect() {
        return protect;
    }
    public boolean AllowExplosions() {
        return allowExplosions;
    }

    public boolean IsBypasser(Entity e) {
        if(e instanceof Player p) {
            UUID id = p.getUniqueId();

            if (bypassers.contains(id))
                return true;
        }

        return false;
    }

    public void AddBypasser(Player p) {
        if(p.hasPermission("persistentlootables.admin")) {
            UUID id = p.getUniqueId();

            if(bypassers.contains(id))
                return;

            bypassers.add(id);
        }
    }

    public void RemoveBypasser(Player p) {
        bypassers.remove(p.getUniqueId());
    }

    public void UpdateLootChunks(World w) {
        //Update every active lootable chunk in the specified world
        Chunk[] chunks = w.getLoadedChunks();

        //Run through each active chunk
        for(Chunk chunk : chunks) {
            PersistentDataContainer c = chunk.getPersistentDataContainer();
            LootUtils.ChunkData data = LootUtils.LoadChunkData(chunk);

            //Check if the chunk has lootable data to load
            if(data != null) {
                Long milliTime = Math.round(data.time * 60 * 1000);
                Long currentTime = System.currentTimeMillis();
                Double last = data.timeLast;

                if(randomPercent > 0) {
                    Random r = new Random();
                    Double p = r.nextDouble(-randomPercent, randomPercent);
                    last += data.time*p;
                }

                //Check if the required time has elapsed for regeneration
                if(currentTime >= last+milliTime) {
                    LootUtils.ApplyChunkData(chunk, data);
                    data.timeLast = currentTime.doubleValue();
                    LootUtils.SaveChunkData(chunk, data);

                    //Notify any players inside the chunk if applicable
                    if(data.notify) {
                        Entity[] entities = chunk.getEntities();
                        for(Entity e : entities) {
                            if(e instanceof Player p) {
                                //Send a message to the player
                                p.sendMessage(textColor+"Loot has mysteriously reappeared around you...");
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        //Plugin startup logic
        instance = this;

        //Load the config.yml file
        FileConfiguration config = getConfig();
        saveDefaultConfig();

        //Update the default text color from the config.yml
        String s = config.getString("textcolor");
        if(s != null && s.contains("#") && s.length() == 7)
            textColor = ChatColor.of(s);

        //Update the loot check time from config.yml
        int t = config.getInt("updatetime");
        if(t < 600)
            t = 1200; //Default to one minute

        //Update random percentage variation maximum
        int r = config.getInt("randompercent");
        if(r > 0)
            randomPercent = r;

        //Update the protection options
        protect = config.getBoolean("protect");
        allowExplosions = config.getBoolean("allow_explosions");

        //Add all the interactables to allow player access to
        allowedInteractables.add(Material.CRAFTING_TABLE);
        allowedInteractables.add(Material.CHEST);
        allowedInteractables.add(Material.TRAPPED_CHEST);
        allowedInteractables.add(Material.BARREL);
        allowedInteractables.add(Material.SHULKER_BOX);
        allowedInteractables.add(Material.FURNACE);
        allowedInteractables.add(Material.BLAST_FURNACE);
        allowedInteractables.add(Material.SMOKER);
        allowedInteractables.add(Material.CAMPFIRE);
        allowedInteractables.add(Material.SOUL_CAMPFIRE);
        allowedInteractables.add(Material.SMITHING_TABLE);
        allowedInteractables.add(Material.LOOM);

        //Register commands
        getCommand("ploot").setExecutor(new LootCommand());
        getCommand("bypassprotect").setExecutor(new BypassCommand());

        //Register tab completions
        getCommand("ploot").setTabCompleter(new LootTabCompletion());

        //Register listeners
        getServer().getPluginManager().registerEvents(new LootListener(), this);

        if(protect)
            getServer().getPluginManager().registerEvents(new ProtectListener(), this);

        //Start repeatedly updating loaded chunks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            //Update all the lootable chunks for all the worlds on the server
            for(World w : getServer().getWorlds())
                UpdateLootChunks(w);
        }, 20, t);
    }
}
