package haulidaie.persistentlootables;

import haulidaie.persistentlootables.commands.LootCommand;
import haulidaie.persistentlootables.commands.LootTabCompletion;
import haulidaie.persistentlootables.listeners.LootListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PersistentLootables extends JavaPlugin {
    private static PersistentLootables instance;
    public ChatColor textColor = ChatColor.WHITE;
    public String lootTitle = ChatColor.BOLD + "Loot Table";

    public static PersistentLootables GetInstance() {
        return instance;
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
        if(s != null && s.contains("#") && s.length() == 7) {
            textColor = ChatColor.of(s);
        } else {
            System.out.println("Your default textcolor is invalid ❤ (Check config.yml)");
        }

        //Register commands
        getCommand("loot").setExecutor(new LootCommand());

        //Register tab completions
        getCommand("loot").setTabCompleter(new LootTabCompletion());

        //Register listeners
        getServer().getPluginManager().registerEvents(new LootListener(), this);

        System.out.println("PersistentLootables is now active ❤");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
