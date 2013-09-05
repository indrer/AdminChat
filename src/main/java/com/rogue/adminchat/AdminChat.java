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
 * @version 1.3.0
 */
public final class AdminChat extends JavaPlugin {

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
        } else {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            if (!yaml.isSet("update-check")) {
                yaml.set("update-check", true);
            }
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

        this.getLogger().info("Enabling Command Handler...");
        this.chandle = new CommandHandler(this);

        this.getLogger().info("Enabling Channel Manager...");
        this.cmanager = new ChannelManager(this);

        this.getLogger().info("Enabling Listener...");
        this.listener = new AdminListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, this);

        this.chandle.setExecs();
    }
    
    /**
     * Reloads AdminChat
     * 
     * @since 1.3.0
     * @version 1.3.0
     */
    public void reload(String... names) {
        this.onDisable();
        try {
            Thread.sleep(250L);
        } catch (InterruptedException ex) {
            this.getLogger().log(Level.SEVERE, null, ex);
        }
        listener = null;
        chandle = null;
        cmanager = null;
        isUpdate = false;
        this.onLoad();
        this.onEnable();
        for (String s : names) {
            this.communicate(s, "Reloaded!");
        }
    }

    /**
     * Parses the format string and sends it to players
     *
     * @since 1.2.0
     * @version 1.3.0
     *
     * @param channel The channel to send to, based on command
     * @param name The user sending the message
     * @param message The message to send to others in the channel
     */
    public void adminBroadcast(String channel, String name, String message) {
        Channel chan = this.getChannelManager().getChannel(channel);
        String send = chan.getFormat();
        send = send.replace("{NAME}", name);
        send = send.replace("{MESSAGE}", message);
        Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', send), "adminchat.channel." + chan.getName());
    }

    /**
     * Sends a message to a player through AdminChat
     *
     * @since 1.2.0
     * @version 1.3.0
     *
     * @param player The player to send to
     * @param message The message to send
     */
    public void communicate(Player player, String message) {
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&cAdminChat&a] " + message));
        }
    }

    /**
     * Sends a message to a player through AdminChat
     *
     * @since 1.2.0
     * @version 1.3.0
     *
     * @param player The player to send to
     * @param message The message to send
     */
    public void communicate(String player, String message) {
        this.communicate(Bukkit.getPlayer(player), message);
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
        this.isUpdate = status;
        return this.isUpdate;
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
        return this.isUpdate;
    }

    /**
     * Returns AdminChat's channel manager
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return Channel Manager class for AdminChat
     */
    public ChannelManager getChannelManager() {
        return this.cmanager;
    }

    /**
     * Returns AdminChat's Command Handler
     *
     * @version 1.3.0
     * @since 1.3.0
     *
     * @return Command Handler class for AdminChat
     */
    public CommandHandler getCommandHandler() {
        return this.chandle;
    }
}
