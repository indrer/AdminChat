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

import com.rogue.adminchat.channel.ChannelManager;
import com.rogue.adminchat.command.CommandHandler;
import com.rogue.adminchat.executables.ExecutiveManager;
import com.rogue.adminchat.metrics.Metrics;
import com.rogue.adminchat.runnable.UpdateRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminChat's main class
 *
 * @since 1.0
 * @author 1Rogue
 * @version 1.4.1
 */
public final class AdminChat extends JavaPlugin {

    private AdminListener listener;
    private ChannelManager cmanager;
    private CommandHandler chandle;
    private ExecutiveManager emanager;
    private boolean isUpdate = false;
    private boolean globalMute = false;
    private String prefix;

    @Override
    public void onEnable() {

        try {
            Metrics metrics = new Metrics(this);
            getLogger().info("Enabling metrics...");
            metrics.start();
        } catch (IOException ex) {
            Logger.getLogger(AdminChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.getLogger().info("Enabling executive manager...");
        this.emanager = new ExecutiveManager(this);


        if (!this.getConfig().isSet("update-check") || !this.getConfig().isSet("prefix"))  {
            this.getConfig().options().copyDefaults(true);
            this.saveConfig();
            this.reloadConfig();
        }

        if (this.getConfig().getBoolean("update-check")) {
            Bukkit.getScheduler().runTaskLater(this, new UpdateRunnable(this), 10L);
        } else {
            this.getLogger().info("Update checking disabled!");
        }

        this.prefix = this.getConfig().getString("prefix");

        this.getLogger().info("Enabling Command Handler...");
        this.chandle = new CommandHandler(this);

        this.getLogger().info("Enabling Channel Manager...");
        this.cmanager = new ChannelManager(this);

        this.getLogger().info("Enabling Listener...");
        this.listener = new AdminListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, this);

        this.chandle.setExecs();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
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
     * Sends a message to a player through AdminChat
     *
     * @since 1.2.0
     * @version 1.4.0
     *
     * @param player The player to send to
     * @param message The message to send
     */
    public void communicate(Player player, String message) {
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.prefix + message));
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
    
    /**
     * Gets the scheduler for plugin tasks
     * 
     * @since 1.3.2
     * @version 1.3.2
     * 
     * @return The scheduler for AdminChat
     */
    public ExecutiveManager getExecutiveManager() {
        return this.emanager;
    }
    
    /**
     * Sets whether or not to globally mute everyone without an override perm.
     * 
     * @since 1.3.2
     * @version 1.3.2
     * 
     * @param mute TRue to mute all, false otherwise
     */
    public void setGlobalMute(boolean mute) {
        this.globalMute = mute;
    }
}
