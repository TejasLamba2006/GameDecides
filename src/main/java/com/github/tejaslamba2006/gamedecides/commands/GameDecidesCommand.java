package com.github.tejaslamba2006.gamedecides.commands;

import com.github.tejaslamba2006.gamedecides.GameDecides;
import com.github.tejaslamba2006.gamedecides.twists.Twist;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GameDecidesCommand implements CommandExecutor, TabCompleter {

    private final GameDecides plugin;

    public GameDecidesCommand(GameDecides plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gamedecides.admin")) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                    plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (plugin.getTwistManager().isRunning()) {
                    sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                            plugin.getConfig().getString("messages.already-running")));
                } else {
                    plugin.getTwistManager().start();
                    sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                            plugin.getConfig().getString("messages.started")));
                }
            }
            case "stop" -> {
                if (!plugin.getTwistManager().isRunning()) {
                    sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                            plugin.getConfig().getString("messages.not-running")));
                } else {
                    plugin.getTwistManager().stop();
                    sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                            plugin.getConfig().getString("messages.stopped")));
                }
            }
            case "skip" -> {
                if (!plugin.getTwistManager().isRunning()) {
                    sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                            plugin.getConfig().getString("messages.not-running")));
                } else {
                    plugin.getTwistManager().skip();
                    sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                            plugin.getConfig().getString("messages.skipped")));
                }
            }
            case "list" -> {
                sender.sendMessage(plugin.colorize("&6=== Enabled Twists ==="));
                for (Twist twist : plugin.getTwistManager().getEnabledTwists()) {
                    String name = plugin.getTwistManager().getTwistName(twist);
                    String desc = plugin.getTwistManager().getTwistDescription(twist);
                    sender.sendMessage(plugin.colorize("&e" + name + " &7- " + desc));
                }
                sender.sendMessage(plugin.colorize("&7Total: &e" + 
                        plugin.getTwistManager().getEnabledTwists().size() + " twists"));
            }
            case "info" -> {
                sender.sendMessage(plugin.colorize("&6=== GameDecides Info ==="));
                sender.sendMessage(plugin.colorize("&7Status: " + 
                        (plugin.getTwistManager().isRunning() ? "&aRunning" : "&cStopped")));
                if (plugin.getTwistManager().getCurrentTwist() != null) {
                    String currentName = plugin.getTwistManager().getTwistName(
                            plugin.getTwistManager().getCurrentTwist());
                    sender.sendMessage(plugin.colorize("&7Current Twist: &e" + currentName));
                }
                sender.sendMessage(plugin.colorize("&7Enabled Twists: &e" + 
                        plugin.getTwistManager().getEnabledTwists().size()));
                sender.sendMessage(plugin.colorize("&7Interval: &e" + 
                        plugin.getConfig().getInt("twist-interval") + " seconds"));
            }
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(plugin.colorize(plugin.getPrefix() + 
                        plugin.getConfig().getString("messages.reload")));
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&6=== GameDecides Help ==="));
        sender.sendMessage(plugin.colorize("&e/gd start &7- Start the game twists"));
        sender.sendMessage(plugin.colorize("&e/gd stop &7- Stop the game twists"));
        sender.sendMessage(plugin.colorize("&e/gd skip &7- Skip to the next twist"));
        sender.sendMessage(plugin.colorize("&e/gd list &7- List all enabled twists"));
        sender.sendMessage(plugin.colorize("&e/gd info &7- Show current status"));
        sender.sendMessage(plugin.colorize("&e/gd reload &7- Reload configuration"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = List.of("start", "stop", "skip", "list", "info", "reload");
            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }
        
        return completions;
    }
}
