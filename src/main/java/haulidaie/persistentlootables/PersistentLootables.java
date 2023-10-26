package haulidaie.persistentlootables;

import haulidaie.persistentlootables.commands.LootCommand;
import haulidaie.persistentlootables.commands.LootTabCompletion;
import haulidaie.persistentlootables.listeners.LootListener;
import haulidaie.persistentlootables.utilities.LootUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class PersistentLootables extends JavaPlugin {
    private static PersistentLootables instance;
    public ChatColor textColor = ChatColor.WHITE;
    public String lootTitle = ChatColor.BOLD + "Loot Table";
    public int randomPercent = 0;

    public static PersistentLootables GetInstance() {
        return instance;
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
        config.options().copyDefaults(true);
        saveConfig();

        //Update the default text color from the config.yml
        String s = config.getString("textcolor");
        if(s != null && s.contains("#") && s.length() == 7)
            textColor = ChatColor.of(s);

        //Update the loot check time from config.yml
        int t = config.getInt("updatetime");
        if(t < 600)
            t = 1200; //Default to one minute

        int r = config.getInt("randompercent");
        if(r > 0)
            randomPercent = r;

        //Register commands
        getCommand("ploot").setExecutor(new LootCommand());

        //Register tab completions
        getCommand("ploot").setTabCompleter(new LootTabCompletion());

        //Register listeners
        getServer().getPluginManager().registerEvents(new LootListener(), this);

        //Start repeatedly updating loaded chunks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            //Update all the lootable chunks for all the worlds on the server
            for(World w : getServer().getWorlds())
                UpdateLootChunks(w);
        }, 20, t);
    }
}
