package me.davidjawhar.dailylaws;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DailyLawsPlugin extends JavaPlugin {

    private LawManager lawManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.lawManager = new LawManager(this);
        this.lawManager.loadState();
        this.lawManager.registerLaws();
        this.lawManager.startSchedulers();

        getLogger().info("DailyLaws enabled.");
    }

    @Override
    public void onDisable() {
        if (lawManager != null) {
            lawManager.shutdown();
            lawManager.saveState();
        }
        getLogger().info("DailyLaws disabled.");
    }

    public LawManager getLawManager() {
        return lawManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("laws")) return false;

        if (args.length == 0) {
            if (!sender.hasPermission("dailylaws.use")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD + "📜 Active Laws: " + ChatColor.YELLOW + lawManager.getActiveLawSummary());
            sender.sendMessage(ChatColor.GRAY + "Next roll in: " + ChatColor.AQUA + lawManager.timeUntilNextRollString());
            sender.sendMessage(ChatColor.DARK_GRAY + "Use /laws list");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "list" -> {
                if (!sender.hasPermission("dailylaws.use")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "Available Laws:");
                lawManager.getAllLaws().forEach(l ->
                        sender.sendMessage(ChatColor.YELLOW + "- " + l.id() + ChatColor.GRAY + " : " + l.description())
                );
                return true;
            }
            case "reroll" -> {
                if (!sender.hasPermission("dailylaws.admin")) {
                    sender.sendMessage(ChatColor.RED + "Admin only.");
                    return true;
                }
                lawManager.rollNewDay(true);
                sender.sendMessage(ChatColor.GREEN + "Rerolled laws.");
                return true;
            }
            case "pause" -> {
                if (!sender.hasPermission("dailylaws.admin")) return deny(sender);
                lawManager.setPaused(true);
                sender.sendMessage(ChatColor.YELLOW + "Daily rolling paused.");
                return true;
            }
            case "resume" -> {
                if (!sender.hasPermission("dailylaws.admin")) return deny(sender);
                lawManager.setPaused(false);
                sender.sendMessage(ChatColor.YELLOW + "Daily rolling resumed.");
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("dailylaws.admin")) return deny(sender);
                reloadConfig();
                lawManager.onConfigReload();
                sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
                return true;
            }
            case "enable", "disable" -> {
                if (!sender.hasPermission("dailylaws.admin")) return deny(sender);
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /laws " + sub + " <LAW_ID>");
                    return true;
                }
                String id = args[1].toUpperCase();
                boolean ok = sub.equals("enable") ? lawManager.enableLaw(id) : lawManager.disableLaw(id);
                sender.sendMessage(ok ? ChatColor.GREEN + "Updated pool." : ChatColor.RED + "Unknown law id: " + id);
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
            }
        }
    }

    private boolean deny(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Admin only.");
        return true;
    }
}