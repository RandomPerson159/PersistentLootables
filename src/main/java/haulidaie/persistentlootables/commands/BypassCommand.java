package haulidaie.persistentlootables.commands;

import haulidaie.persistentlootables.PersistentLootables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BypassCommand implements CommandExecutor {
    private final PersistentLootables plugin = PersistentLootables.GetInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player p) {
            //Toggle the players bypass if they have permissions

            if(p.hasPermission("persistentlootables.admin")) {
                if(plugin.IsBypasser(p)) {
                    plugin.RemoveBypasser(p);
                    p.sendMessage(plugin.textColor+"World protection bypass set to FALSE ❤");
                } else {
                    plugin.AddBypasser(p);
                    p.sendMessage(plugin.textColor+"World protection bypass set to TRUE ❤");
                }
            }
        }

        return true;
    }
}
