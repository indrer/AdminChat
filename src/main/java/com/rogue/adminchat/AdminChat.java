/*
 * Copyright (C) 2013 Spencer Alderman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.rogue.adminchat;

import com.rogue.adminchat.channel.Channel;
import com.rogue.adminchat.channel.ChannelManager;
import com.rogue.adminchat.command.CommandHandler;
import com.rogue.adminchat.metrics.Metrics;
import com.rogue.adminchat.runnable.UpdateRunnable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AdminChat's main class
 *
 * @since 1.0
 * @author 1Rogue
 * @version 1.2.2
 */
public class AdminChat extends JavaPlugin {

    private String SEND = "&aADMIN: {NAME}: &c{MESSAGE}";
    private AdminListener listener;
    private ChannelManager cmanager;
    private CommandHandler chandle;
    private boolean isUpdate = false;

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
            if (!yaml.isSet("format")) { yaml.set("format", "&aADMIN: {NAME}: &c{MESSAGE}"); }
            if (!yaml.isSet("update-check")) { yaml.set("update-check", true); }

            SEND = yaml.getString("format");
        }
    }

    @Override
    public void onEnable() {
        
        try {
            Metrics metrics = new Metrics(this);
            getLogger().info("Enabling metrics...");
            metrics.start();
        } catch (IOException ex) {
            Logger.getLogger(AdminChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (this.getConfig().getBoolean("update-check")) {
            Bukkit.getScheduler().runTaskLater(this, new UpdateRunnable(this), 10L);
        } else {
            this.getLogger().info("Update checking disabled!");
        }
        
        this.getLogger().info("Enabling Channel Manager...");
        cmanager = new ChannelManager(this);
        
        this.getLogger().info("Enabling Command Handler...");
        chandle = new CommandHandler(this);
        
        this.getLogger().info("Enabling Listener...");
        listener = new AdminListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    /**
     * Parses the format string and sends it to players
     *
     * @since 1.2.0
     * @version 1.2.2
     *
     * @param name The name to replace in the string
     * @param message The message to send to admins
     */
    public void adminBroadcast(String channel, String name, String message) {
        Channel chan = this.getChannelManager().getChannels().get(channel);
        String send = chan.getFormat();
        send = send.replace("{NAME}", name);
        send = send.replace("{MESSAGE}", message);
        Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', send), "adminchat.channel." + chan.getName());
    }
    
    /**
     * Sends a message to a player through AdminChat
     * 
     * @since 1.2.0
     * @version 1.2.2
     * 
     * @param p The player to send to
     * @param message The message to send
     */
    public void communicate(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&cAdminChat&a] " + message));
    }
    
    /**
     * Sends a message to a player through AdminChat
     * 
     * @since 1.2.0
     * @version 1.2.2
     * 
     * @param p The player to send to
     * @param message The message to send
     */
    public void communicate(String player, String message) {
        Bukkit.getPlayer(player).sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&cAdminChat&a] " + message));
    }
    
    /**
     * Sets the update status for the plugin
     * 
     * @since 1.2.0
     * @version 1.2.0
     * 
     * @param status The status to set
     * @return The newly set status
     */
    public boolean setUpdateStatus(boolean status) {
        isUpdate = status;
        return isUpdate;
    }
    
    /**
     * Whether or not the plugin is outdated
     * 
     * @since 1.2.0
     * @version 1.2.0
     * 
     * @return True if outdated, false otherwise
     */
    public boolean isOutOfDate() {
        return isUpdate;
    }
    
    /**
     * Returns AdminChat's channel manager
     * 
     * @since 1.2.2
     * @version 1.2.2
     * 
     * @return Channel Manager class for AdminChat
     */
    public ChannelManager getChannelManager() {
        return cmanager;
    }
    
    /**
     * Returns AdminChat's Command Handler
     * 
     * @version 1.2.2
     * @since 1.2.2
     * 
     * @return Command Handler class for AdminChat
     */
    public CommandHandler getCommandHandler() {
        return chandle;
    }
}
