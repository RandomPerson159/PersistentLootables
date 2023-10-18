package haulidaie.persistentlootables.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;

public class LootTabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        ArrayList<String> suggestions = new ArrayList<>();

        if (strings.length == 1) {
            suggestions.add("create");
            suggestions.add("set");
            suggestions.add("check");
            suggestions.add("destroy");
        } else if (strings.length == 2) {
            switch(strings[0]) {
                case("create") -> {
                    suggestions.add("mystery");
                    suggestions.add("individual");
                    suggestions.add("everyone");
                }
                case("set") -> {
                    suggestions.add("time");
                    suggestions.add("items");
                    suggestions.add("amount");
                    suggestions.add("notify");
                    suggestions.add("type");
                }
            }
        } else if (strings.length == 3) {
            if (strings[0].equals("set")) {
                switch(strings[1]) {
                    case("notify") -> {
                        suggestions.add("true");
                        suggestions.add("false");
                    }
                    case("type") -> {
                        suggestions.add("mystery");
                        suggestions.add("individual");
                        suggestions.add("everyone");
                    }
                }
            }
        }

        return suggestions;
    }
}
