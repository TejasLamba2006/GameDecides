package com.github.tejaslamba2006.gamedecides.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PluginAdvertiser implements Listener {

    private static final String SMP_CORE_PLUGIN = "smp-core";
    private static final String DISCORD_URL = "https://discord.gg/7fQPG4Grwt";
    private static final String MODRINTH_URL = "https://modrinth.com/plugin/smpcore";

    private final JavaPlugin plugin;
    private final Set<UUID> advertisedPlayers = new HashSet<>();
    private final boolean smpCoreInstalled;

    public PluginAdvertiser(JavaPlugin plugin) {
        this.plugin = plugin;
        this.smpCoreInstalled = Bukkit.getPluginManager().getPlugin(SMP_CORE_PLUGIN) != null;
    }

    public static void advertiseOnEnable(JavaPlugin plugin) {
        boolean smpCoreInstalled = Bukkit.getPluginManager().getPlugin(SMP_CORE_PLUGIN) != null;

        if (!smpCoreInstalled) {
            plugin.getLogger().info("");
            plugin.getLogger()
                    .info("\u001B[33m╔════════════════════════════════════════════════════════════╗\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m  \u001B[36m★ Looking for more SMP features?\u001B[0m                           \u001B[33m║\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m                                                              \u001B[33m║\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m  \u001B[32mCheck out SMP Core - All-in-one SMP plugin!\u001B[0m                \u001B[33m║\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m  \u001B[37m• PvP Balance  • Gear Control  • Dimension Locks\u001B[0m          \u001B[33m║\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m  \u001B[37m• Mob Control  • Auto Restart  • QoL Features\u001B[0m             \u001B[33m║\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m                                                              \u001B[33m║\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m  \u001B[34mDownload: https://modrinth.com/plugin/smpcore\u001B[0m              \u001B[33m║\u001B[0m");
            plugin.getLogger().info(
                    "\u001B[33m║\u001B[0m  \u001B[35mDiscord:  https://discord.gg/7fQPG4Grwt\u001B[0m                   \u001B[33m║\u001B[0m");
            plugin.getLogger()
                    .info("\u001B[33m╚════════════════════════════════════════════════════════════╝\u001B[0m");
            plugin.getLogger().info("");
        }

        Bukkit.getPluginManager().registerEvents(new PluginAdvertiser(plugin), plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp() || smpCoreInstalled || advertisedPlayers.contains(player.getUniqueId())) {
            return;
        }

        advertisedPlayers.add(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("═".repeat(50)).color(NamedTextColor.GOLD));
            player.sendMessage(Component.empty());

            player.sendMessage(Component.text("  ★ ")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text("Hey! You're using ")
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(plugin.getDescription().getName())
                            .color(NamedTextColor.GREEN)
                            .decorate(TextDecoration.BOLD)));

            player.sendMessage(Component.empty());

            player.sendMessage(Component.text("  Check out ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("SMP Core")
                            .color(NamedTextColor.AQUA)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" for even more features!")
                            .color(NamedTextColor.GRAY)));

            player.sendMessage(Component.text("  All-in-one SMP plugin: PvP balance, gear control,")
                    .color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("  dimension locks, mob control & more!")
                    .color(NamedTextColor.GRAY));

            player.sendMessage(Component.empty());

            Component downloadLink = Component.text("  [Download]")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.openUrl(MODRINTH_URL))
                    .hoverEvent(
                            HoverEvent.showText(Component.text("Click to open Modrinth").color(NamedTextColor.GREEN)));

            Component discordLink = Component.text(" [Discord Support]")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.openUrl(DISCORD_URL))
                    .hoverEvent(HoverEvent
                            .showText(Component.text("Click to join Discord").color(NamedTextColor.LIGHT_PURPLE)));

            player.sendMessage(downloadLink.append(discordLink));

            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("═".repeat(50)).color(NamedTextColor.GOLD));
            player.sendMessage(Component.empty());
        }, 40L);
    }
}
