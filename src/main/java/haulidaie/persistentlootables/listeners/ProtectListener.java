package haulidaie.persistentlootables.listeners;

import haulidaie.persistentlootables.PersistentLootables;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class ProtectListener implements Listener {
    private final PersistentLootables plugin = PersistentLootables.GetInstance();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(type != Material.PLAYER_HEAD && type != Material.PLAYER_WALL_HEAD) {
            if(!operator)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator)
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();
        boolean operator = plugin.IsBypasser(p);
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();

        if(!operator && (mainHand.getType() == Material.ENDER_PEARL || (mainHand.getType() == Material.AIR && offHand.getType() == Material.ENDER_PEARL))) {
            e.setCancelled(true);
            return;
        }


        if(!operator && block != null && !block.isEmpty()) {
            if(!plugin.allowedInteractables.contains(block.getType()))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteractAt(PlayerInteractAtEntityEvent e) {
        Entity target = e.getRightClicked();
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(target instanceof ArmorStand) {
            if(!operator)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity target = e.getRightClicked();
        boolean operator = plugin.IsBypasser(p);

        if(target instanceof ItemFrame) {
            if(!operator)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        Entity target = e.getEntity();
        boolean operator = plugin.IsBypasser(e.getDamager());

        //Only damage non-aggressive mobs
        if(!(target instanceof ItemFrame) && !(target instanceof Monster)) {
            if(!operator)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity target = e.getEntity();
        LivingEntity killer = target.getKiller();
        boolean operator = false;

        if(killer != null)
            operator = plugin.IsBypasser(killer);

        //Only damage non-aggressive mobs
        if(!(target instanceof Monster)) {
            if(!operator) {
                AttributeInstance hp = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);

                if(hp != null)
                    target.setHealth(hp.getBaseValue());
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        //Operators cannot bypass this event (use melee)
        Block hitBlock = e.getHitBlock();
        Entity hitEntity = e.getHitEntity();

        if(hitBlock != null) {
            e.setCancelled(true);
            return;
        }

        //Only damage non-aggressive mobs
        if(hitEntity != null && !(hitEntity instanceof ItemFrame) && !(hitEntity instanceof Monster))
            e.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        Entity target = e.getEntity();

        if(!(target instanceof ItemFrame)) {
            HangingBreakEvent.RemoveCause cause = e.getCause();
            Entity remover = e.getRemover();
            boolean operator = plugin.IsBypasser(remover);

            if(cause != HangingBreakEvent.RemoveCause.ENTITY) {
                e.setCancelled(true);
                return;
            }

            if(!operator)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTakeBook(PlayerTakeLecternBookEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator)
            e.setCancelled(true);
    }

    @EventHandler
    public void onOpenSign(SignChangeEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator)
            e.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator)
            e.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator)
            e.setCancelled(true);
    }

    @EventHandler
    public void onBucketCatch(PlayerBucketEntityEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator)
            e.setCancelled(true);
    }

    @EventHandler
    public void onTNTPrime(TNTPrimeEvent e) {
        boolean operator = plugin.IsBypasser(e.getPrimingEntity());

        if(!operator || !plugin.AllowExplosions())
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator || !plugin.AllowExplosions())
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if(!plugin.AllowExplosions())
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator)
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent e) {
        boolean operator = plugin.IsBypasser(e.getPlayer());

        if(!operator && e.getItem().getType() == Material.CHORUS_FRUIT)
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if(plugin.IsBypasser(p))
            plugin.RemoveBypasser(p);
    }
}
