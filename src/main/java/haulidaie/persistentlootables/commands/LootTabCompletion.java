package haulidaie.persistentlootables.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;

public class LootTabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("set");
            suggestions.add("check");
            suggestions.add("destroy");
        } else if (args.length == 2) {
            switch(args[0]) {
                case("create") -> {
                    suggestions.add("mystery");
                    suggestions.add("individual");
                    suggestions.add("everyone");
                    suggestions.add("head");
                    suggestions.add("frame");
                }
                case("set") -> {
                    suggestions.add("time");
                    suggestions.add("items");
                    suggestions.add("amount");
                    suggestions.add("notify");
                    suggestions.add("type");
                    suggestions.add("chunk");
                }
                case("check") -> {
                    suggestions.add("target");
                    suggestions.add("chunk");
                }
                case("destroy") -> {
                    suggestions.add("target");
                    suggestions.add("chunk");
                }
            }
        } else if (args.length == 3) {
            if (args[0].equals("set")) {
                switch(args[1]) {
                    case("notify") -> {
                        suggestions.add("true");
                        suggestions.add("false");
                    }
                    case("type") -> {
                        suggestions.add("mystery");
                        suggestions.add("individual");
                        suggestions.add("everyone");
                    }
                    case("chunk") -> {
                        suggestions.add("time");
                        suggestions.add("notify");
                    }
                }
            }
        } else if(args.length == 4) {
            if (args[0].equals("set") && args[1].equals("chunk")) {
                if (args[2].equals("notify")) {
                    suggestions.add("true");
                    suggestions.add("false");
                }
            }
        }

        return suggestions;
    }
}
