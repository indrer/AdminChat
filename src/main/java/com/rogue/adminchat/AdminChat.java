package com.rogue.adminchat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AdminChat's main class
 *
 * @since 1.0
 * @author 1Rogue
 * @version 1.2.0
 */
public class AdminChat extends JavaPlugin {

    private String SEND = "&aADMIN: {NAME}: &c{MESSAGE}";
    private List<String> toggled = new ArrayList();
    private AdminListener listener;

    @Override
    public void onLoad() {
        File file = new File(this.getDataFolder(), "config.yml");
        if (this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        if (!file.exists()) {
            this.saveDefaultConfig();
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            SEND = yaml.getString("format");
        } else {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            if (!yaml.isSet("format")) {
                yaml.set("format", "&aADMIN: {0}: &c{1}");
            }

            SEND = yaml.getString("format");
        }
    }

    @Override
    public void onEnable() {
        listener = new AdminListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ac") && sender.hasPermission("adminchat.use")) {
            StringBuilder msg = new StringBuilder();
            if (args.length > 0) {
                for (String s : args) {
                    msg.append(s);
                }
                String name;
                if (sender instanceof Player) {
                    name = sender.getName();
                } else {
                    name = "CONSOLE";
                }
                this.adminBroadcast(name, msg.toString());
            }
        } else if (cmd.getName().equalsIgnoreCase("actoggle")) {
            if (sender instanceof Player) {
                if (toggled.contains(sender.getName())) {
                    toggled.remove(sender.getName());
                    this.communicate((Player)sender, "Automatic admin chat disabled!");
                } else {
                    this.communicate((Player)sender, "Automatic admin chat enabled!");
                }
            }
        }
        return true;
    }

    /**
     * Parses the format string and sends it to players
     *
     * @since 1.2.0
     * @version 1.2.0
     *
     * @param name The name to replace in the string
     * @param message The message to send to admins
     */
    public void adminBroadcast(String name, String message) {
        String send = SEND;
        send = send.replace("{NAME}", name);
        send = send.replace("{MESSAGE}", message);
        Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', send), "adminchat.read");
    }

    /**
     * Returns a list of players that are toggled for admin chat
     *
     * @since 1.2.0
     * @version 1.2.0
     *
     * @return List of toggled players
     */
    public List<String> getToggled() {
        return toggled;
    }
    
    /**
     * Sends a message to a player through AdminChat
     * 
     * @since 1.2.0
     * @version 1.2.0
     * 
     * @param p The player to send to
     * @param message The message to send
     */
    public void communicate(Player player, String message) {
        player.sendMessage(ChatColor.GREEN + "[" + ChatColor.RED + "AdminChat" + ChatColor.GREEN + "] " + message);
    }
    
    /**
     * Sends a message to a player through AdminChat
     * 
     * @since 1.2.0
     * @version 1.2.0
     * 
     * @param p The player to send to
     * @param message The message to send
     */
    public void communicate(String player, String message) {
        Bukkit.getPlayer(player).sendMessage(ChatColor.GREEN + "[" + ChatColor.RED + "AdminChat" + ChatColor.GREEN + "] " + message);
    }
}
