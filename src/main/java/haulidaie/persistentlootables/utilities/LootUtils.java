package haulidaie.persistentlootables.utilities;

import haulidaie.persistentlootables.PersistentLootables;
import org.bukkit.*;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
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

    public static class HeadData {
        public Location location;
        public BlockData block;

        public void Set(Location _location, BlockData _block) {
            location = _location;
            block = _block;
        }
    }

    public static class FrameData {
        public Location location;
        public ItemStack item;
        public Rotation rotation;

        public void Set(Location _location, ItemStack _item, Rotation _rotation) {
            location = _location;
            item = _item;
            rotation = _rotation;
        }
    }

    public static class ChunkData {
        public Chunk chunk;
        public ArrayList<HeadData> heads = new ArrayList<>();
        public ArrayList<FrameData> frames = new ArrayList<>();
        public Double time;
        public Double timeLast;
        public boolean notify;
        public String noise;
        public String effect;

        public void Set(Chunk _chunk, ArrayList<HeadData> _heads, ArrayList<FrameData> _frames, Double _time, boolean _notify, String _noise, String _effect) {
            chunk = _chunk;
            heads = _heads;
            frames = _frames;
            time = _time;
            timeLast = _time;
            notify = _notify;
            noise = _noise;
            effect = _effect;
        }
    }

    public static ChunkData CreateChunkData(Chunk chunk) {
        ChunkData data = new ChunkData();
        data.Set(chunk, new ArrayList<HeadData>(), new ArrayList<FrameData>(), 72000.0, false, "", "");
        SaveChunkData(chunk, data);

        return data;
    }

    public static void SaveChunkData(Chunk chunk, ChunkData data) {
        //Overwrites anything in the PDC of the chunk specified in the ChunkData
        PersistentDataContainer c = data.chunk.getPersistentDataContainer();
        NamespacedKey keyHeads = new NamespacedKey(plugin, "lootHeads");
        NamespacedKey keyFrames = new NamespacedKey(plugin, "lootFrames");
        NamespacedKey keyTime = new NamespacedKey(plugin, "lootTime");
        NamespacedKey keyTimeLast = new NamespacedKey(plugin, "lootTimeLast");
        NamespacedKey keyNotify = new NamespacedKey(plugin, "lootNotify");
        NamespacedKey keyNoise = new NamespacedKey(plugin, "lootNoise");
        NamespacedKey keyEffect = new NamespacedKey(plugin, "lootEffect");

        c.set(keyHeads, PersistentDataType.STRING, EncodeHeads(data.heads));
        c.set(keyFrames, PersistentDataType.STRING, EncodeFrames(data.frames));
        c.set(keyTime, PersistentDataType.DOUBLE, data.time);
        c.set(keyTimeLast, PersistentDataType.DOUBLE, data.timeLast);
        c.set(keyNotify, PersistentDataType.BOOLEAN, data.notify);
        c.set(keyNoise, PersistentDataType.STRING, data.noise);
        c.set(keyEffect, PersistentDataType.STRING, data.effect);
    }

    public static ChunkData LoadChunkData(Chunk chunk) {
        PersistentDataContainer c = chunk.getPersistentDataContainer();
        NamespacedKey keyHeads = new NamespacedKey(plugin, "lootHeads");
        NamespacedKey keyFrames = new NamespacedKey(plugin, "lootFrames");
        NamespacedKey keyTime = new NamespacedKey(plugin, "lootTime");
        NamespacedKey keyTimeLast = new NamespacedKey(plugin, "lootTimeLast");
        NamespacedKey keyNotify = new NamespacedKey(plugin, "lootNotify");
        NamespacedKey keyNoise = new NamespacedKey(plugin, "lootNoise");
        NamespacedKey keyEffect = new NamespacedKey(plugin, "lootEffect");

        if(c.has(keyTime, PersistentDataType.DOUBLE)) {
            ChunkData data = new ChunkData();
            data.chunk = chunk;
            data.heads = DecodeHeads(c.get(keyHeads, PersistentDataType.STRING));
            data.frames = DecodeFrames(c.get(keyFrames, PersistentDataType.STRING));
            data.time = c.get(keyTime, PersistentDataType.DOUBLE);
            data.timeLast = c.get(keyTimeLast, PersistentDataType.DOUBLE);
            data.notify = c.get(keyNotify, PersistentDataType.BOOLEAN);
            data.noise = c.get(keyNoise, PersistentDataType.STRING);
            data.effect = c.get(keyEffect, PersistentDataType.STRING);

            return data;
        }

        return null;
    }

    public static void ApplyChunkData(Chunk chunk, ChunkData data) {
        if(chunk == data.chunk) {
            //Create heads
            if(data.heads != null) {
                for (HeadData h : data.heads) {
                    h.location.getBlock().setBlockData(h.block);
                }
            }

            //Create frames
            if(data.frames != null) {
                Entity[] entities = chunk.getEntities();
                ItemFrame e = null;

                for(FrameData f : data.frames) {
                    //Look for an existing item frame
                    for(Entity existing : entities) {
                        if(existing instanceof ItemFrame existingFrame) {
                            if(existing.getLocation() == f.location) {
                                e = existingFrame;
                            }
                        }
                    }

                    //Create a default blank item frame otherwise
                    if(e == null)
                        e = chunk.getWorld().spawn(f.location, ItemFrame.class);

                    //Set the item frame's values
                    e.setItem(f.item);
                    e.setRotation(f.rotation);
                }
            }
        }
    }

    public static String EncodeItems(List<ItemStack> items) {
        if(items.isEmpty())
            return "";
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeInt(items.size());

            for(int i = 0; i < items.size(); i++) {
                os.writeObject(items.get(i));
            }

            os.flush();

            byte[] itemsSerialized = io.toByteArray();
            String data = Base64.getEncoder().encodeToString(itemsSerialized);

            os.close();

            return data;
        } catch (IOException ignored) {}

        return "";
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

            for(int i = 0; i < itemsCount; i++) {
                items.add((ItemStack) is.readObject());
            }

            is.close();

            return items;
        } catch (IOException | ClassNotFoundException ignored) {}

        return null;
    }

    public static String EncodeHeads(List<HeadData> heads) {
        if(heads.isEmpty())
            return "";
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeInt(heads.size());

            for(int i = 0; i < heads.size(); i++) {
                os.writeObject(heads.get(i).location);
                os.writeUTF(heads.get(i).block.getAsString());
            }

            os.flush();

            byte[] itemsSerialized = io.toByteArray();
            String data = Base64.getEncoder().encodeToString(itemsSerialized);

            os.close();

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //return "";
    }

    public static ArrayList<HeadData> DecodeHeads(String data) {
        ArrayList<HeadData> heads = new ArrayList<>();

        if(data.isEmpty())
            return heads;

        byte [] dataDecoded = Base64.getDecoder().decode(data);

        try {
            ByteArrayInputStream io = new ByteArrayInputStream(dataDecoded);
            BukkitObjectInputStream is = new BukkitObjectInputStream(io);

            int itemsCount = is.readInt();

            for(int i = 0; i < itemsCount; i++) {
                HeadData hd = new HeadData();
                hd.location = (Location) is.readObject();
                hd.block = Bukkit.createBlockData(is.readUTF());
                heads.add(hd);
            }

            is.close();

            return heads;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        //return null;
    }

    public static String EncodeFrames(List<FrameData> frames) {
        if(frames.isEmpty())
            return "";
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeInt(frames.size());

            for(int i = 0; i < frames.size(); i++) {
                os.writeObject(frames.get(i).location);
                os.writeObject(frames.get(i).item);
                os.writeObject(frames.get(i).rotation);
            }

            os.flush();

            byte[] itemsSerialized = io.toByteArray();
            String data = Base64.getEncoder().encodeToString(itemsSerialized);

            os.close();

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //return "";
    }

    public static ArrayList<FrameData> DecodeFrames(String data) {
        ArrayList<FrameData> frames = new ArrayList<>();

        if(data.isEmpty())
            return frames;

        byte [] dataDecoded = Base64.getDecoder().decode(data);

        try {
            ByteArrayInputStream io = new ByteArrayInputStream(dataDecoded);
            BukkitObjectInputStream is = new BukkitObjectInputStream(io);

            int itemsCount = is.readInt();

            for(int i = 0; i < itemsCount; i++) {
                FrameData fd = new FrameData();
                fd.location = (Location) is.readObject();
                fd.item = (ItemStack) is.readObject();
                fd.rotation = (Rotation) is.readObject();
                frames.add(fd);
            }

            is.close();

            return frames;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        //return null;
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
