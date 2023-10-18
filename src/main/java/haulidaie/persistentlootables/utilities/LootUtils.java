package haulidaie.persistentlootables.utilities;

import haulidaie.persistentlootables.PersistentLootables;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class LootUtils {
    private static final PersistentLootables plugin = PersistentLootables.GetInstance();

    public static String EncodeItems(List<ItemStack> items) {
        if(items.isEmpty())
            return "";
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeInt(items.size());

            for (int i = 0; i < items.size(); i++) {
                os.writeObject(items.get(i));
            }

            os.flush();

            byte[] itemsSerialized = io.toByteArray();
            String data = Base64.getEncoder().encodeToString(itemsSerialized);

            os.close();

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<ItemStack> DecodeItems(String data) {
        if(data.isEmpty())
            return null;

        ArrayList<ItemStack> items = new ArrayList<>();
        byte [] dataDecoded = Base64.getDecoder().decode(data);

        try {
            ByteArrayInputStream io = new ByteArrayInputStream(dataDecoded);
            BukkitObjectInputStream is = new BukkitObjectInputStream(io);

            int itemsCount = is.readInt();

            for (int i = 0; i < itemsCount; i++) {
                items.add((ItemStack) is.readObject());
            }

            is.close();

            return items;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void SaveLoot(TileState tState, ArrayList<ItemStack> items) {
        //Converts a list of ItemStacks to a string and saves it in the block's persistent data container
        String itemsEncoded = EncodeItems(items);
        PersistentDataContainer c = tState.getPersistentDataContainer();
        NamespacedKey invKey = new NamespacedKey(plugin, "inventoryLoot");

        c.set(invKey, PersistentDataType.STRING, itemsEncoded);

        tState.update();
    }

    public static void LoadLoot(TileState tState, Inventory inv) {
        //Converts a list of ItemStacks to a string and saves it in the block's persistent data container
        PersistentDataContainer c = tState.getPersistentDataContainer();
        NamespacedKey invKey = new NamespacedKey(plugin, "inventoryLoot");

        if (c.has(invKey, PersistentDataType.STRING)) {
            String s = c.get(invKey, PersistentDataType.STRING);
            ArrayList<ItemStack> items = DecodeItems(s);

            if (items != null) {
                for (ItemStack item : items)
                    inv.addItem(item);
            }
        }
    }

    public static String EncodePlayerTimes(HashMap<UUID, Long> playerTimes) {
        if(playerTimes.isEmpty())
            return "";
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeObject(playerTimes);

            os.flush();

            byte[] playerTimesSerialized = io.toByteArray();
            String data = Base64.getEncoder().encodeToString(playerTimesSerialized);

            os.close();

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<UUID, Long> DecodePlayerTimes(String data) {
        if(data.isEmpty())
            return null;

        HashMap<UUID, Long> playerTimes = new HashMap<>();
        byte [] dataDecoded = Base64.getDecoder().decode(data);

        try {
            ByteArrayInputStream io = new ByteArrayInputStream(dataDecoded);
            BukkitObjectInputStream is = new BukkitObjectInputStream(io);

            playerTimes = (HashMap<UUID, Long>) is.readObject();

            is.close();

            return playerTimes;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void SavePlayerTimes(TileState tState, HashMap<UUID, Long> playerTimes) {
        //Converts a list of ItemStacks to a string and saves it in the block's persistent data container
        String playerTimesEncoded = EncodePlayerTimes(playerTimes);
        PersistentDataContainer c = tState.getPersistentDataContainer();
        NamespacedKey invKey = new NamespacedKey(plugin, "playerTimes");

        c.set(invKey, PersistentDataType.STRING, playerTimesEncoded);

        tState.update();
    }

    public static HashMap<UUID, Long> LoadPlayerTimes(TileState tState) {
        //Converts a list of ItemStacks to a string and saves it in the block's persistent data container
        PersistentDataContainer c = tState.getPersistentDataContainer();
        NamespacedKey playerTimesKey = new NamespacedKey(plugin, "playerTimes");

        if (c.has(playerTimesKey, PersistentDataType.STRING)) {
            String s = c.get(playerTimesKey, PersistentDataType.STRING);

            return DecodePlayerTimes(s);
        }

        return null;
    }

    public static HashMap<UUID, Long> RemoveOverduePlayers(HashMap<UUID, Long> playerTimes, Long milliTime) {
        Long currentTime = System.currentTimeMillis();

        for (UUID p : playerTimes.keySet()) {
            Long timestamp = playerTimes.get(p);

            if (timestamp + milliTime <= currentTime)
                playerTimes.remove(p);
        }

        return playerTimes;
    }
}
