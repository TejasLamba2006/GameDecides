package com.github.tejaslamba2006.gamedecides.listeners;

import com.github.tejaslamba2006.gamedecides.GameDecides;
import com.github.tejaslamba2006.gamedecides.twists.Twist;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

public class TwistListener implements Listener {

    private final GameDecides plugin;
    private final Random random = new Random();

    public TwistListener(GameDecides plugin) {
        this.plugin = plugin;
    }

    private boolean isCurrentTwist(Twist twist) {
        return plugin.getTwistManager().isRunning() && 
               plugin.getTwistManager().getCurrentTwist() == twist;
    }

    private boolean isAffected(Player player) {
        return !plugin.getTwistManager().getWorldBlacklist().contains(player.getWorld().getName()) &&
               !player.hasPermission("gamedecides.bypass");
    }

    // DREAM_LUCK - Triple drops
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeathDreamLuck(EntityDeathEvent event) {
        if (!isCurrentTwist(Twist.DREAM_LUCK)) return;
        
        // Triple the drops
        event.getDrops().addAll(event.getDrops().stream().map(ItemStack::clone).collect(Collectors.toList()));
        event.getDrops().addAll(event.getDrops().stream().map(ItemStack::clone).collect(Collectors.toList()));
    }

    // STAY_AWAY - Massive knockback
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageStayAway(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.STAY_AWAY)) return;
        if (!(event.getDamager() instanceof Player)) return;
        
        Vector knockback = event.getDamager().getLocation().getDirection().clone().multiply(5);
        knockback.setY(0.5);
        event.getEntity().setVelocity(event.getEntity().getVelocity().add(knockback));
    }

    // SQUID_GAME - Squids drop materials
    @EventHandler(priority = EventPriority.HIGH)
    public void onSquidDeath(EntityDeathEvent event) {
        if (!isCurrentTwist(Twist.SQUID_GAME)) return;
        if (!(event.getEntity() instanceof Squid)) return;
        
        event.getDrops().add(new ItemStack(Material.IRON_INGOT, random.nextInt(3) + 1));
        event.getDrops().add(new ItemStack(Material.STICK, random.nextInt(8) + 1));
        event.getDrops().add(new ItemStack(Material.OAK_PLANKS, random.nextInt(16) + 8));
        event.getDrops().add(new ItemStack(Material.STRING, random.nextInt(4) + 1));
    }

    // FAITH_LEAP - Fall damage heals
    @EventHandler(priority = EventPriority.HIGH)
    public void onFallDamage(EntityDamageEvent event) {
        if (!isCurrentTwist(Twist.FAITH_LEAP)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        
        double damage = event.getDamage();
        double maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        living.setHealth(Math.min(maxHealth, living.getHealth() + damage));
        event.setDamage(0);
        
        if (living instanceof Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        }
    }

    // PIGLIN_LIFE - Only gold weapons deal damage
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamagePiglinLife(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.PIGLIN_LIFE)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isAffected(player)) return;
        
        ItemStack weapon = player.getInventory().getItemInMainHand();
        String typeName = weapon.getType().toString().toLowerCase();
        
        if (!typeName.contains("gold")) {
            event.setCancelled(true);
            player.sendMessage(plugin.colorize(plugin.getPrefix() + "&cYou must use gold weapons!"));
        }
    }

    // REVERSE_KARMA - Dealing damage gives buffs
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageReverseKarma(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.REVERSE_KARMA)) return;
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isAffected(player)) return;
        
        PotionEffectType[] buffs = {
                PotionEffectType.SPEED,
                PotionEffectType.STRENGTH,
                PotionEffectType.REGENERATION,
                PotionEffectType.JUMP_BOOST,
                PotionEffectType.FIRE_RESISTANCE,
                PotionEffectType.NIGHT_VISION,
                PotionEffectType.WATER_BREATHING
        };
        
        PotionEffectType buff = buffs[random.nextInt(buffs.length)];
        player.addPotionEffect(new PotionEffect(buff, 600, 0, false, true));
    }

    // BOUNCY_WORLD - Bounce on landing
    @EventHandler(priority = EventPriority.HIGH)
    public void onFallBouncy(EntityDamageEvent event) {
        if (!isCurrentTwist(Twist.BOUNCY_WORLD)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isAffected(player)) return;
        
        event.setCancelled(true);
        Vector velocity = player.getVelocity();
        velocity.setY(Math.abs(velocity.getY()) * 0.8 + 0.5);
        player.setVelocity(velocity);
        player.playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 1.0f, 1.0f);
    }

    // THORNS_EVERYWHERE - Attacking damages you too
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageThorns(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.THORNS_EVERYWHERE)) return;
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isAffected(player)) return;
        
        double reflectDamage = event.getDamage() * 0.3;
        player.damage(reflectDamage);
    }

    // VAMPIRIC - Heal when dealing damage
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageVampiric(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.VAMPIRIC)) return;
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isAffected(player)) return;
        
        double heal = event.getDamage() * 0.5;
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
        player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.5f, 1.5f);
    }

    // ONE_TAP - Instant kill
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageOneTap(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.ONE_TAP)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        event.setDamage(1000);
    }

    // PACIFIST - No PvP damage
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamagePacifist(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.PACIFIST)) return;
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    // EXPLOSIVE_ARROWS - Arrows explode
    @EventHandler(priority = EventPriority.HIGH)
    public void onArrowHit(ProjectileHitEvent event) {
        if (!isCurrentTwist(Twist.EXPLOSIVE_ARROWS)) return;
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player)) return;
        
        Location loc = arrow.getLocation();
        arrow.getWorld().createExplosion(loc, 2.0f, false, false);
        arrow.remove();
    }

    // FIRE_TOUCH - Everything catches fire
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageFireTouch(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.FIRE_TOUCH)) return;
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;
        
        event.getEntity().setFireTicks(100);
    }

    // VAULT_HUNTER - Block breaking drops trial loot
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakVault(BlockBreakEvent event) {
        if (!isCurrentTwist(Twist.VAULT_HUNTER)) return;
        if (event.isCancelled()) return;
        if (!isAffected(event.getPlayer())) return;
        
        if (random.nextInt(100) < 5) { // 5% chance
            Material[] loot = {
                    Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
                    Material.IRON_INGOT, Material.ENDER_PEARL, Material.EXPERIENCE_BOTTLE,
                    Material.GOLDEN_APPLE
            };
            
            Material drop = loot[random.nextInt(loot.length)];
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), 
                    new ItemStack(drop, random.nextInt(3) + 1));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_VAULT_OPEN_SHUTTER, 1.0f, 1.0f);
        }
    }

    // MACE_MAYHEM - Convert fall damage to smash damage
    @EventHandler(priority = EventPriority.HIGH)
    public void onFallMaceMayhem(EntityDamageEvent event) {
        if (!isCurrentTwist(Twist.MACE_MAYHEM)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isAffected(player)) return;
        
        double fallDamage = event.getDamage();
        event.setCancelled(true);
        
        // Create explosion effect and damage nearby entities
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, loc, 1);
        
        for (Entity nearby : player.getNearbyEntities(3, 3, 3)) {
            if (nearby instanceof LivingEntity living && nearby != player) {
                living.damage(fallDamage * 2, player);
            }
        }
    }

    // SHARED_DAMAGE - Damage to one hurts everyone
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageShared(EntityDamageEvent event) {
        if (!isCurrentTwist(Twist.SHARED_DAMAGE)) return;
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player damaged)) return;
        if (!isAffected(damaged)) return;
        
        double sharedDamage = event.getDamage() * 0.3;
        
        for (Player player : damaged.getWorld().getPlayers()) {
            if (player != damaged && isAffected(player)) {
                player.damage(sharedDamage);
            }
        }
    }

    // LUCKY_BLOCKS - Random items from blocks
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakLucky(BlockBreakEvent event) {
        if (!isCurrentTwist(Twist.LUCKY_BLOCKS)) return;
        if (event.isCancelled()) return;
        if (!isAffected(event.getPlayer())) return;
        
        if (random.nextInt(100) < 10) { // 10% chance
            Material[] items = {
                    Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT,
                    Material.COAL, Material.STICK, Material.BONE, Material.STRING,
                    Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.GUNPOWDER,
                    Material.ARROW, Material.BREAD, Material.APPLE
            };
            
            Material drop = items[random.nextInt(items.length)];
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(),
                    new ItemStack(drop, random.nextInt(5) + 1));
        }
    }

    // NO_TOOLS - Tools break instantly
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakNoTools(BlockBreakEvent event) {
        if (!isCurrentTwist(Twist.NO_TOOLS)) return;
        if (event.isCancelled()) return;
        if (!isAffected(event.getPlayer())) return;
        
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (tool.getType().getMaxDurability() > 0) {
            tool.setDurability((short) (tool.getType().getMaxDurability() - 1));
        }
    }

    // HOT_POTATO - Transfer potato on hit
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerHitHotPotato(EntityDamageByEntityEvent event) {
        if (!isCurrentTwist(Twist.HOT_POTATO)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        
        // Check if attacker has hot potato
        for (int i = 0; i < attacker.getInventory().getSize(); i++) {
            ItemStack item = attacker.getInventory().getItem(i);
            if (item != null && item.getType() == Material.BAKED_POTATO &&
                    item.getItemMeta() != null && item.getItemMeta().hasEnchantmentGlintOverride()) {
                
                // Transfer potato
                attacker.getInventory().setItem(i, null);
                victim.getInventory().addItem(item);
                
                attacker.sendMessage(plugin.colorize(plugin.getPrefix() + "&aYou passed the hot potato!"));
                victim.sendMessage(plugin.colorize(plugin.getPrefix() + "&cYou received the hot potato!"));
                victim.playSound(victim.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                break;
            }
        }
    }
}
