package com.github.tejaslamba2006.gamedecides.managers;

import com.github.tejaslamba2006.gamedecides.GameDecides;
import com.github.tejaslamba2006.gamedecides.twists.Twist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TwistManager {

    private final GameDecides plugin;
    private final List<Twist> enabledTwists = new ArrayList<>();
    private final Set<String> worldBlacklist = new HashSet<>();
    private final Random random = new Random();
    
    private Twist currentTwist = null;
    private BukkitTask mainTask = null;
    private BukkitTask effectTask = null;
    private boolean running = false;

    public TwistManager(GameDecides plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    private void loadConfiguration() {
        enabledTwists.clear();
        for (Twist twist : Twist.values()) {
            String path = "twists." + twist.name() + ".enabled";
            if (plugin.getConfig().getBoolean(path, true)) {
                enabledTwists.add(twist);
            }
        }
        
        worldBlacklist.clear();
        worldBlacklist.addAll(plugin.getConfig().getStringList("world-blacklist"));
        
        plugin.getLogger().info("Loaded " + enabledTwists.size() + " enabled twists");
    }

    public void start() {
        if (running) return;
        running = true;
        
        int interval = plugin.getConfig().getInt("twist-interval", 300) * 20; // Convert to ticks
        
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                nextTwist();
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    public void stop() {
        running = false;
        
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
        
        // Clean up current twist effects
        if (currentTwist != null) {
            cleanupTwist(currentTwist);
        }
        currentTwist = null;
    }

    public void skip() {
        if (!running) return;
        nextTwist();
    }

    private void nextTwist() {
        // Cleanup previous twist
        if (currentTwist != null) {
            cleanupTwist(currentTwist);
        }
        
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
        
        // Select new random twist
        if (enabledTwists.isEmpty()) {
            plugin.getLogger().warning("No twists enabled!");
            return;
        }
        
        currentTwist = enabledTwists.get(random.nextInt(enabledTwists.size()));
        
        // Broadcast the twist
        broadcastTwist();
        
        // Apply the twist
        applyTwist(currentTwist);
    }

    private void broadcastTwist() {
        if (!plugin.getConfig().getBoolean("broadcast.enabled", true)) return;
        
        String twistName = getTwistName(currentTwist);
        String twistDesc = getTwistDescription(currentTwist);
        
        String message = plugin.getConfig().getString("broadcast.message", "&6&l⚡ TWIST CHANGE! &e%twist%")
                .replace("%twist%", twistName);
        String descMessage = plugin.getConfig().getString("broadcast.description-message", "&7%description%")
                .replace("%description%", twistDesc);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (worldBlacklist.contains(player.getWorld().getName())) continue;
            
            player.sendMessage(plugin.colorize(message));
            player.sendMessage(plugin.colorize(descMessage));
            
            // Play sound
            String soundName = plugin.getConfig().getString("broadcast.sound", "ENTITY_ENDER_DRAGON_GROWL");
            try {
                Sound sound = Sound.valueOf(soundName);
                float volume = (float) plugin.getConfig().getDouble("broadcast.sound-volume", 1.0);
                float pitch = (float) plugin.getConfig().getDouble("broadcast.sound-pitch", 1.0);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException ignored) {}
            
            // Show title
            if (plugin.getConfig().getBoolean("title.enabled", true)) {
                int fadeIn = plugin.getConfig().getInt("title.fade-in", 10) * 50;
                int stay = plugin.getConfig().getInt("title.stay", 60) * 50;
                int fadeOut = plugin.getConfig().getInt("title.fade-out", 20) * 50;
                
                Title title = Title.title(
                        plugin.colorize("&6&l⚡ " + twistName),
                        plugin.colorize("&7" + twistDesc),
                        Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))
                );
                player.showTitle(title);
            }
        }
    }

    private void applyTwist(Twist twist) {
        Collection<? extends Player> players = getAffectedPlayers();
        
        switch (twist) {
            case DREAM_LUCK -> {} // Handled in listener
            
            case CHAD_GEAR -> {
                for (Player player : players) {
                    // Save and clear armor
                    applyLeatherArmor(player);
                }
            }
            
            case STAY_AWAY -> {} // Handled in listener
            
            case GHOST_MODE -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                    // Hide all players from each other
                    for (Player other : players) {
                        if (!player.equals(other)) {
                            player.hidePlayer(plugin, other);
                        }
                    }
                }
            }
            
            case SQUID_GAME -> {} // Handled in listener
            
            case FAITH_LEAP -> {} // Handled in listener
            
            case BIRD_MODE -> {
                for (Player player : players) {
                    ItemStack elytra = new ItemStack(Material.ELYTRA);
                    ItemMeta meta = elytra.getItemMeta();
                    meta.setUnbreakable(true);
                    meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
                    elytra.setItemMeta(meta);
                    player.getInventory().setChestplate(elytra);
                }
            }
            
            case DWARF_MODE -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 2, false, false));
                }
            }
            
            case PIGLIN_LIFE -> {} // Handled in listener
            
            case HEALTH_PACK -> {
                for (Player player : players) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
                }
            }
            
            case REVERSE_KARMA -> {} // Handled in listener
            
            case SURPRISE -> startTNTRain();
            
            case LOW_GRAVITY -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 3, false, false));
                }
            }
            
            case SPEED_DEMON -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
                }
            }
            
            case TURTLE_MODE -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, false, false));
                }
            }
            
            case BOUNCY_WORLD -> {} // Handled in listener
            
            case DOLPHIN_GRACE -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 1, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false));
                }
            }
            
            case GLASS_CANNON -> {
                for (Player player : players) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(4.0);
                    player.setHealth(Math.min(player.getHealth(), 4.0));
                    player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(30.0); // 3x damage
                }
            }
            
            case THORNS_EVERYWHERE -> {} // Handled in listener
            
            case VAMPIRIC -> {} // Handled in listener
            
            case ONE_TAP -> {} // Handled in listener
            
            case PACIFIST -> {} // Handled in listener
            
            case EXPLOSIVE_ARROWS -> {} // Handled in listener
            
            case FIRE_TOUCH -> {} // Handled in listener
            
            case BREEZE_ATTACK -> startBreezeAttacks();
            
            case VAULT_HUNTER -> {} // Handled in listener
            
            case MACE_MAYHEM -> {} // Handled in listener
            
            case OMINOUS_AURA -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, Integer.MAX_VALUE, 0, false, false));
                }
            }
            
            case INVENTORY_SWAP -> swapInventories();
            
            case POSITION_SHUFFLE -> shufflePositions();
            
            case RANDOM_TELEPORT -> startRandomTeleports();
            
            case BACKWARDS_DAY -> {} // Client-side, would need packets - skip for now
            
            case NO_JUMP -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false)); // -7 jump
                }
            }
            
            case BLINDFOLDED -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
                }
            }
            
            case ETERNAL_NIGHT -> {
                for (World world : Bukkit.getWorlds()) {
                    if (!worldBlacklist.contains(world.getName())) {
                        world.setTime(18000);
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                    }
                }
            }
            
            case RAIN_OF_FIRE -> startFireRain();
            
            case LIGHTNING_STORM -> startLightningStorm();
            
            case MOB_MAYHEM -> startMobSpawning();
            
            case SHARED_DAMAGE -> {} // Handled in listener
            
            case LUCKY_BLOCKS -> {} // Handled in listener
            
            case NO_TOOLS -> {} // Handled in listener
            
            case MAGNETIC -> startMagneticEffect();
            
            case STARTER_KIT -> giveStarterKits();
            
            case GIANT_MODE -> {
                for (Player player : players) {
                    player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(2.0);
                }
            }
            
            case BABY_MODE -> {
                for (Player player : players) {
                    player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(0.5);
                }
            }
            
            case LEVITATION -> {
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, 0, false, false));
                }
            }
            
            case FREEZE -> {
                for (Player player : players) {
                    player.setFreezeTicks(200); // 10 seconds
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 255, false, false));
                }
            }
            
            case HOT_POTATO -> startHotPotato();
        }
    }

    private void cleanupTwist(Twist twist) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        
        switch (twist) {
            case CHAD_GEAR, BIRD_MODE -> {
                for (Player player : players) {
                    player.getInventory().setHelmet(null);
                    player.getInventory().setChestplate(null);
                    player.getInventory().setLeggings(null);
                    player.getInventory().setBoots(null);
                }
            }
            
            case GHOST_MODE -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    for (Player other : players) {
                        player.showPlayer(plugin, other);
                    }
                }
            }
            
            case DWARF_MODE -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.HASTE);
                }
            }
            
            case HEALTH_PACK, GLASS_CANNON -> {
                for (Player player : players) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                    player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0);
                }
            }
            
            case LOW_GRAVITY -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                }
            }
            
            case SPEED_DEMON -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
            }
            
            case TURTLE_MODE -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.SLOWNESS);
                }
            }
            
            case DOLPHIN_GRACE -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
                    player.removePotionEffect(PotionEffectType.WATER_BREATHING);
                }
            }
            
            case OMINOUS_AURA -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.BAD_OMEN);
                }
            }
            
            case NO_JUMP -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                }
            }
            
            case BLINDFOLDED -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
            }
            
            case ETERNAL_NIGHT -> {
                for (World world : Bukkit.getWorlds()) {
                    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                }
            }
            
            case GIANT_MODE, BABY_MODE -> {
                for (Player player : players) {
                    player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);
                }
            }
            
            case LEVITATION -> {
                for (Player player : players) {
                    player.removePotionEffect(PotionEffectType.LEVITATION);
                }
            }
        }
    }

    // === Helper Methods ===

    private Collection<? extends Player> getAffectedPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !worldBlacklist.contains(p.getWorld().getName()))
                .filter(p -> !p.hasPermission("gamedecides.bypass"))
                .toList();
    }

    private void applyLeatherArmor(Player player) {
        ItemStack helmet = createCursedLeather(Material.LEATHER_HELMET);
        ItemStack chestplate = createCursedLeather(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = createCursedLeather(Material.LEATHER_LEGGINGS);
        ItemStack boots = createCursedLeather(Material.LEATHER_BOOTS);
        
        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    private ItemStack createCursedLeather(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        item.setItemMeta(meta);
        return item;
    }

    private void startTNTRain() {
        effectTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 9) {
                    cancel();
                    return;
                }
                for (Player player : getAffectedPlayers()) {
                    for (int i = 0; i < 4; i++) {
                        Location loc = player.getLocation().add(
                                random.nextDouble() * 10 - 5,
                                15 + i * 3,
                                random.nextDouble() * 10 - 5
                        );
                        TNTPrimed tnt = player.getWorld().spawn(loc, TNTPrimed.class);
                        tnt.setFuseTicks(80 + i * 15);
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 600L);
    }

    private void startBreezeAttacks() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getAffectedPlayers()) {
                    if (random.nextInt(100) < 20) { // 20% chance per cycle
                        // Launch player with wind
                        player.setVelocity(player.getVelocity().add(
                                new org.bukkit.util.Vector(
                                        random.nextDouble() * 2 - 1,
                                        0.5 + random.nextDouble(),
                                        random.nextDouble() * 2 - 1
                                )
                        ));
                        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.0f);
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);
    }

    private void swapInventories() {
        List<Player> players = new ArrayList<>(getAffectedPlayers());
        if (players.size() < 2) return;
        
        Collections.shuffle(players);
        
        // Store all inventories
        Map<Player, ItemStack[]> inventories = new HashMap<>();
        for (Player player : players) {
            inventories.put(player, player.getInventory().getContents().clone());
        }
        
        // Swap inventories in circular fashion
        for (int i = 0; i < players.size(); i++) {
            Player current = players.get(i);
            Player next = players.get((i + 1) % players.size());
            current.getInventory().setContents(inventories.get(next));
            current.sendMessage(plugin.colorize(plugin.getPrefix() + "&eYour inventory was swapped!"));
        }
    }

    private void shufflePositions() {
        List<Player> players = new ArrayList<>(getAffectedPlayers());
        if (players.size() < 2) return;
        
        List<Location> locations = new ArrayList<>();
        for (Player player : players) {
            locations.add(player.getLocation().clone());
        }
        
        Collections.shuffle(locations);
        
        for (int i = 0; i < players.size(); i++) {
            players.get(i).teleport(locations.get(i));
            players.get(i).sendMessage(plugin.colorize(plugin.getPrefix() + "&eYou were teleported!"));
        }
    }

    private void startRandomTeleports() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getAffectedPlayers()) {
                    if (random.nextInt(100) < 10) { // 10% chance
                        Location loc = player.getLocation().add(
                                random.nextInt(20) - 10,
                                0,
                                random.nextInt(20) - 10
                        );
                        loc.setY(player.getWorld().getHighestBlockYAt(loc) + 1);
                        player.teleport(loc);
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    private void startFireRain() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getAffectedPlayers()) {
                    Location loc = player.getLocation().add(
                            random.nextInt(10) - 5,
                            0,
                            random.nextInt(10) - 5
                    );
                    loc.setY(player.getWorld().getHighestBlockYAt(loc));
                    loc.getBlock().setType(Material.FIRE);
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    private void startLightningStorm() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getAffectedPlayers()) {
                    if (random.nextInt(100) < 30) {
                        Location loc = player.getLocation().add(
                                random.nextInt(16) - 8,
                                0,
                                random.nextInt(16) - 8
                        );
                        player.getWorld().strikeLightning(loc);
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    private void startMobSpawning() {
        EntityType[] mobs = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER};
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getAffectedPlayers()) {
                    Location loc = player.getLocation().add(
                            random.nextInt(10) - 5,
                            0,
                            random.nextInt(10) - 5
                    );
                    loc.setY(player.getWorld().getHighestBlockYAt(loc) + 1);
                    player.getWorld().spawnEntity(loc, mobs[random.nextInt(mobs.length)]);
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    private void startMagneticEffect() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getAffectedPlayers()) {
                    for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                        if (entity instanceof Item item) {
                            org.bukkit.util.Vector direction = player.getLocation().toVector()
                                    .subtract(item.getLocation().toVector()).normalize().multiply(0.5);
                            item.setVelocity(direction);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    private void giveStarterKits() {
        ItemStack[][] kits = {
                {new ItemStack(Material.IRON_SWORD), new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.COOKED_BEEF, 16)},
                {new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 32), new ItemStack(Material.LEATHER_CHESTPLATE)},
                {new ItemStack(Material.DIAMOND, 5), new ItemStack(Material.IRON_INGOT, 16), new ItemStack(Material.GOLDEN_APPLE, 3)},
                {new ItemStack(Material.ENDER_PEARL, 4), new ItemStack(Material.GOLDEN_APPLE, 2), new ItemStack(Material.SHIELD)}
        };
        
        for (Player player : getAffectedPlayers()) {
            ItemStack[] kit = kits[random.nextInt(kits.length)];
            for (ItemStack item : kit) {
                player.getInventory().addItem(item.clone());
            }
            player.sendMessage(plugin.colorize(plugin.getPrefix() + "&aYou received a starter kit!"));
        }
    }

    private void startHotPotato() {
        List<Player> players = new ArrayList<>(getAffectedPlayers());
        if (players.isEmpty()) return;
        
        Player holder = players.get(random.nextInt(players.size()));
        ItemStack potato = new ItemStack(Material.BAKED_POTATO);
        ItemMeta meta = potato.getItemMeta();
        meta.displayName(plugin.colorize("&c&l🔥 HOT POTATO 🔥"));
        meta.setEnchantmentGlintOverride(true);
        potato.setItemMeta(meta);
        
        holder.getInventory().addItem(potato);
        Bukkit.broadcast(plugin.colorize(plugin.getPrefix() + "&c" + holder.getName() + " &ehas the hot potato!"));
        
        effectTask = new BukkitRunnable() {
            int countdown = 30;
            @Override
            public void run() {
                countdown--;
                if (countdown <= 0) {
                    // Find who has the potato
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        for (ItemStack item : p.getInventory().getContents()) {
                            if (item != null && item.getType() == Material.BAKED_POTATO && 
                                    item.getItemMeta() != null && item.getItemMeta().hasEnchantmentGlintOverride()) {
                                p.getWorld().createExplosion(p.getLocation(), 3f, false, false);
                                p.getInventory().remove(item);
                                Bukkit.broadcast(plugin.colorize(plugin.getPrefix() + "&c" + p.getName() + " &eexploded with the hot potato!"));
                                cancel();
                                return;
                            }
                        }
                    }
                    cancel();
                } else if (countdown <= 10) {
                    Bukkit.broadcast(plugin.colorize(plugin.getPrefix() + "&c" + countdown + " seconds until explosion!"));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // === Getters ===

    public Twist getCurrentTwist() {
        return currentTwist;
    }

    public boolean isRunning() {
        return running;
    }

    public String getTwistName(Twist twist) {
        return plugin.getConfig().getString("twists." + twist.name() + ".name", twist.name());
    }

    public String getTwistDescription(Twist twist) {
        return plugin.getConfig().getString("twists." + twist.name() + ".description", "No description");
    }

    public List<Twist> getEnabledTwists() {
        return Collections.unmodifiableList(enabledTwists);
    }

    public Set<String> getWorldBlacklist() {
        return worldBlacklist;
    }
}
