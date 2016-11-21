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
package com.rogue.adminchat.channel;

import com.rogue.adminchat.AdminChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Class for managing chat channels
 *
 * @since 1.3.0
 * @author 1Rogue
 * @author MD678685
 * @version 1.5.0
 */
public class ChannelManager {

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private final AdminChat plugin;

    public ChannelManager(AdminChat plugin) {
        this.plugin = plugin;
        try {
            setup();
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Error grabbing channels!", ex);
        }
    }

    /**
     * Gets the channel configurations from the channels.yml file, or loads a
     * new one if it does not exist. Also registers appropriate permissions
     *
     * @since 1.3.0
     * @version 1.3.1
     *
     * @throws IOException When file is not readable
     */
    private void setup() throws IOException {
        if (this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }
        File chan = new File(this.plugin.getDataFolder() + File.separator + "channels.yml");
        if (!chan.exists()) {
            this.plugin.saveResource("channels.yml", true);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(chan);
        ConfigurationSection sect = yaml.getConfigurationSection("channels");
        if (sect == null) {
            this.plugin.getLogger().severe("No channels found! AdminChat will not work until you add channels to plugins/AdminChat/channels.yml");
            return;
        }
        for (String s : sect.getKeys(false)) {
            String format = yaml.getString("channels." + s + ".format");
            String cmd = yaml.getString("channels." + s + ".command");
            String permDefault = yaml.getString("channels." + s + ".permDefault");
            permDefault = (permDefault != null ? permDefault : "op");
            if (format != null && cmd != null && !cmd.equalsIgnoreCase("adminchat")) {
                this.plugin.getLogger().log(Level.CONFIG, "Adding command {0}!", cmd);
                this.channels.put(s, new Channel(plugin, s, cmd, format));
                Permission perm = new Permission("adminchat.channel." + s);
                Permission read = new Permission("adminchat.channel." + s + ".read");
                Permission send = new Permission("adminchat.channel." + s + ".send");
                Permission mute = new Permission("adminchat.channel." + s + ".mute");
                Permission join = new Permission("adminchat.channel." + s + ".join");
                Permission leave = new Permission("adminchat.channel." + s + ".leave");
                perm.setDefault(PermissionDefault.getByName(permDefault));
                perm.addParent("adminchat.channel.*", true);
                read.addParent(perm, true);
                send.addParent(perm, true);
                mute.addParent(perm, true);
                mute.addParent("adminchat.muteall", true);
                join.addParent(perm, true);
                leave.addParent(perm, true);
                try {
                    this.plugin.getLogger().log(Level.CONFIG, "Registering {0}", perm.getName());
                    Bukkit.getPluginManager().addPermission(perm);
                    Bukkit.getPluginManager().addPermission(read);
                    Bukkit.getPluginManager().addPermission(send);
                    Bukkit.getPluginManager().addPermission(mute);
                    Bukkit.getPluginManager().addPermission(join);
                    Bukkit.getPluginManager().addPermission(leave);
                } catch (IllegalArgumentException e) {
                    this.plugin.getLogger().log(Level.WARNING, "The permission {0} is already registered!", perm.getName());
                }
                registerCommand(cmd);
            }
        }
    }

    /**
     * Registers the channel commands with bukkit's command map dynamically. If
     * a command already exists, it will be prefixed with a "adminchat:".
     *
     * @since 1.3.0
     * @version 1.3.0
     */
    private void registerCommand(String baseCommand) {
        SimpleCommandMap map = (SimpleCommandMap) this.plugin.getServer().getPluginManager();

        PluginCommand pc = this.getCommand(baseCommand, this.plugin);
        PluginCommand toggle = this.getCommand(baseCommand + "toggle", this.plugin);
        PluginCommand mute = this.getCommand(baseCommand + "mute", this.plugin);
        PluginCommand unmute = this.getCommand(baseCommand + "unmute", this.plugin);
        if (pc != null && toggle != null && mute != null && unmute != null) {
            map.register("adminchat", pc);
            map.register("adminchat", toggle);
            map.register("adminchat", mute);
            map.register("adminchat", unmute);
        }
    }

    /**
     * Gets a PluginCommand object from bukkit
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @param name Command Label
     * @param plugin Plugin instance
     * 
     * @return New PluginCommand object, or null upon an exception
     */
    private PluginCommand getCommand(String name, Plugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            command = c.newInstance(name, plugin);
        } catch (SecurityException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create " + name + " command", e);
        } catch (IllegalArgumentException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create " + name + " command", e);
        } catch (IllegalAccessException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create " + name + " command", e);
        } catch (InstantiationException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create " + name + " command", e);
        } catch (InvocationTargetException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create " + name + " command", e);
        } catch (NoSuchMethodException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create " + name + " command", e);
        }

        return command;
    }

    /**
     * Returns a map of the current channels, with the command as their key
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return A map of the channels
     */
    public Map<String, Channel> getChannels() {
        return this.channels;
    }

    /**
     * Returns a Channel by a requested key. This method is thread-safe.
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @param name The channel name
     * 
     * @throws ChannelNotFoundException If no channel is found by the provided name
     * 
     * @return The channel object, null if channel does not exist
     */
    public synchronized Channel getChannel(String name) throws ChannelNotFoundException {
        Channel chan = this.channels.get(name);
        if (chan != null) {
            return chan;
        } else {
            throw new ChannelNotFoundException("Unknown Channel: &c" + name);
        }
    }
    
    /**
     * Checks if there is a channel by the command name
     * 
     * @since 1.3.2
     * @version 1.3.2
     * 
     * @param name The command used to call the channel
     * @return True if exists, false otherwise
     */
    public synchronized boolean isChannel(String name) {
        return this.channels.containsKey(name);
    }

    /**
     * Deprecated - use Channel#isMuted instead.
     *
     * Parses the format string and sends it to players
     *
     * @since 1.2.0
     * @version 1.4.3
     *
     * @param channel The channel to send to, based on command
     * @param name The user sending the message
     * @param message The message to send to others in the channel
     */
    @Deprecated
    public void sendMessage(String channel, String name, String message) {
        CommandSender sender;
        if (name == "CONSOLE") {
            sender = this.plugin.getServer().getConsoleSender();
        } else {
            sender = this.plugin.getServer().getPlayer(name);
        }
        try {
            this.getChannel(channel).sendMessage(sender, message);
        } catch (ChannelNotFoundException e) {
            this.plugin.communicate(sender, e.getMessage());
            this.plugin.getLogger().log(Level.SEVERE, "Could not find channel: " + channel, e);
        } catch (SenderMutedException e) {
            this.plugin.communicate(sender, e.getMessage());
        }
    }

    /**
     * Adds passed player names to a mute list. Does not verify the names are
     * players.
     *
     * Deprecated; use Channel#muteSender
     *
     * @since 1.3.2
     * @version 1.5.0
     *
     * @param channel Channel to mute in
     * @param names Names to mute
     * 
     * @throws ChannelNotFoundException If no channel is found by the provided name
     * 
     * @return True if all names were successfully added.
     */
    @Deprecated
    public void mute(String channel, String... names) throws ChannelNotFoundException {
        if (!this.isChannel(channel)) throw new ChannelNotFoundException("Unknown channel while muting", channel);
        Channel pluginChannel = this.getChannel(channel);
        for (String name : names) {
            Player player = this.plugin.getServer().getPlayer(name);
            if (!(player == null)) {
                pluginChannel.muteSender(player);
            }
        }
    }

    /**
     * Unmutes a player within a channel, or globally
     *
     * Deprecated; use Channel#unmuteSender
     * 
     * @since 1.3.2
     * @version 1.5.0
     * 
     * @param channel The channel to mute in, null if global
     * @param names Players to mute by name
     * 
     * @throws ChannelNotFoundException If no channel is found by the provided name
     */
    @Deprecated
    public void unmute(String channel, String... names) throws ChannelNotFoundException {
        if (!this.isChannel(channel)) throw new ChannelNotFoundException("Unknown channel", channel);
        Channel pluginChannel = this.getChannel(channel);
        for (String name : names) {
            Player player = this.plugin.getServer().getPlayer(name);
            if (!(player == null)) {
                pluginChannel.unmuteSender(player);
            }
        }
    }

    /**
     * Deprecated - use Channel#isMuted instead.
     *
     * Returns whether or not a player is muted in a channel
     *
     * @vesion 1.5.0
     *
     * @param name The name to check
     * @param channel The channel to check against
     * 
     * @return True if muted in the channel, false otherwise
     */
    @Deprecated
    public synchronized boolean isMuted(String name, String channel) {
        boolean isMuted;
        try {
            isMuted = getChannel(channel).isMuted(this.plugin.getServer().getPlayer(name));
        } catch (ChannelNotFoundException e) {
            isMuted = false;
        }
        return isMuted;
    }
}
