package com.github.tejaslamba2006.gamedecides;

import com.github.tejaslamba2006.gamedecides.commands.GameDecidesCommand;
import com.github.tejaslamba2006.gamedecides.listeners.TwistListener;
import com.github.tejaslamba2006.gamedecides.managers.TwistManager;
import com.github.tejaslamba2006.gamedecides.utils.PluginAdvertiser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public final class GameDecides extends JavaPlugin {

    private static GameDecides instance;
    private TwistManager twistManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        twistManager = new TwistManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new TwistListener(this), this);
        
        // Register commands
        GameDecidesCommand commandExecutor = new GameDecidesCommand(this);
        getCommand("gamedecides").setExecutor(commandExecutor);
        getCommand("gamedecides").setTabCompleter(commandExecutor);
        
        // Advertise SMP Core
        PluginAdvertiser.advertiseOnEnable(this);
        
        getLogger().info("GameDecides has been enabled!");
        getLogger().info("Use /gamedecides start to begin the twists!");
    }

    @Override
    public void onDisable() {
        if (twistManager != null && twistManager.isRunning()) {
            twistManager.stop();
        }
        getLogger().info("GameDecides has been disabled!");
    }

    public static GameDecides getInstance() {
        return instance;
    }

    public TwistManager getTwistManager() {
        return twistManager;
    }

    public void reload() {
        boolean wasRunning = twistManager.isRunning();
        if (wasRunning) {
            twistManager.stop();
        }
        reloadConfig();
        twistManager = new TwistManager(this);
        if (wasRunning) {
            twistManager.start();
        }
    }

    public Component colorize(String message) {
        if (message.contains("&")) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        }
        return miniMessage.deserialize(message);
    }

    public String getPrefix() {
        return getConfig().getString("messages.prefix", "&6[GameDecides] &r");
    }
}
