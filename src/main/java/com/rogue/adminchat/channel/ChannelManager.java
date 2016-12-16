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
import com.rogue.adminchat.command.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Class for managing chat channels
 *
 * @author 1Rogue
 * @author MD678685
 * @version 1.5.0
 * @since 1.3.0
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
     * @throws IOException When file is not readable
     * @version 1.3.1
     * @since 1.3.0
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
            registerChannel(s, cmd, format, permDefault);
        }
    }

    protected void registerChannel(String name, String cmd, String format, String permDefault) {
        if (format != null && cmd != null && !cmd.equalsIgnoreCase("adminchat")) {
            this.plugin.getLogger().log(Level.CONFIG, "Adding command {0}!", cmd);
            Channel channel = new Channel(plugin, name, cmd, format);
            this.channels.put(name, channel);
            Permission perm = new Permission("adminchat.channel." + name);
            Permission read = new Permission("adminchat.channel." + name + ".read");
            Permission send = new Permission("adminchat.channel." + name + ".send");
            Permission mute = new Permission("adminchat.channel." + name + ".mute");
            Permission join = new Permission("adminchat.channel." + name + ".join");
            Permission leave = new Permission("adminchat.channel." + name + ".leave");
            Permission autojoin = new Permission("adminchat.channel." + name + ".autojoin");
            perm.setDefault(PermissionDefault.getByName(permDefault));
            perm.addParent("adminchat.channel.*", true);
            read.addParent(perm, true);
            send.addParent(perm, true);
            mute.addParent(perm, true);
            mute.addParent("adminchat.muteall", true);
            join.addParent(perm, true);
            leave.addParent(perm, true);
            autojoin.addParent(perm, true);
            try {
                this.plugin.getLogger().log(Level.CONFIG, "Registering {0}", perm.getName());
                PluginManager m = Bukkit.getPluginManager();
                m.addPermission(perm);
                m.addPermission(read);
                m.addPermission(send);
                m.addPermission(mute);
                m.addPermission(join);
                m.addPermission(leave);
                m.addPermission(autojoin);
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().log(Level.WARNING, "The permission {0} is already registered!", perm.getName());
            }
            registerCommand(cmd, channel);
        }
    }

    /**
     * Registers the channel commands with bukkit's command map dynamically. If
     * a command already exists, it will be prefixed with a "adminchat:".
     *
     * @version 1.5.0
     * @since 1.3.0
     */
    private void registerCommand(String baseCommand, Channel channel) {
        SimpleCommandMap map = (SimpleCommandMap) this.plugin.getServer().getPluginManager();

        BaseCommand pc = getCommand(baseCommand, channel, this.plugin);
        BaseCommand toggle = getCommand(baseCommand + "toggle", channel, this.plugin);
        BaseCommand mute = getCommand(baseCommand + "mute", channel, this.plugin);
        BaseCommand unmute = getCommand(baseCommand + "unmute", channel, this.plugin);
        BaseCommand join = getCommand(baseCommand + "join", channel, this.plugin);
        BaseCommand leave = getCommand(baseCommand + "leave", channel, this.plugin);
        if (pc != null && toggle != null && mute != null && unmute != null) {
            map.register("adminchat", pc);
            map.register("adminchat", toggle);
            map.register("adminchat", mute);
            map.register("adminchat", unmute);
            map.register("adminchat", join);
            map.register("adminchat", leave);
        }
    }

    /**
     * Returns a map of the current channels, with the command as their key
     *
     * @return A map of the channels
     * @version 1.3.0
     * @since 1.3.0
     */
    public Map<String, Channel> getChannels() {
        return this.channels;
    }

    /**
     * Returns a Channel by a requested key. This method is thread-safe.
     *
     * @param name The channel name
     * @return The channel object, null if channel does not exist
     * @throws ChannelNotFoundException If no channel is found by the provided name
     * @version 1.3.0
     * @since 1.3.0
     */
    public synchronized Channel getChannel(String name) throws ChannelNotFoundException {
        Channel chan = this.channels.get(name);
        if (chan != null) {
            return chan;
        } else {
            throw new ChannelNotFoundException("Unknown Channel: &c" + name, name);
        }
    }

    /**
     * Checks if there is a channel by the command name
     *
     * @param name The command used to call the channel
     * @return True if exists, false otherwise
     * @version 1.3.2
     * @since 1.3.2
     */
    public synchronized boolean isChannel(String name) {
        return this.channels.containsKey(name);
    }

    /**
     * Deprecated - use Channel#isMuted instead.
     * <p>
     * Parses the format string and sends it to players
     *
     * @param channel The channel to send to, based on command
     * @param name    The user sending the message
     * @param message The message to send to others in the channel
     * @version 1.4.3
     * @since 1.2.0
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
     * <p>
     * Deprecated; use Channel#muteSender
     *
     * @param channel Channel to mute in
     * @param names   Names to mute
     * @return True if all names were successfully added.
     * @throws ChannelNotFoundException If no channel is found by the provided name
     * @version 1.5.0
     * @since 1.3.2
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
     * <p>
     * Deprecated; use Channel#unmuteSender
     *
     * @param channel The channel to mute in, null if global
     * @param names   Players to mute by name
     * @throws ChannelNotFoundException If no channel is found by the provided name
     * @version 1.5.0
     * @since 1.3.2
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
     * <p>
     * Returns whether or not a player is muted in a channel
     *
     * @param name    The name to check
     * @param channel The channel to check against
     * @return True if muted in the channel, false otherwise
     * @vesion 1.5.0
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

    /**
     * Creates a new command class for a provided command name
     *
     * @param cmd The command to try against
     * @return The {@link BaseCommand} version of the command
     */
    private static BaseCommand getCommand(String cmd, Channel channel, AdminChat plugin) {
        String commandLabel;
        BaseCommand command;
        if (cmd.endsWith("unmute")) {
            commandLabel = cmd.substring(0, cmd.length() - 6);
            command = new UnmuteCommand(cmd, channel, plugin);
        } else if (cmd.endsWith("toggle")) {
            commandLabel = cmd.substring(0, cmd.length() - 6);
            command = new ToggleCommand(cmd, channel, plugin);
        } else if (cmd.endsWith("leave")) {
            commandLabel = cmd.substring(0, cmd.length() - 5);
            command = new LeaveCommand(cmd, channel, plugin);
        } else if (cmd.endsWith("mute")) {
            commandLabel = cmd.substring(0, cmd.length() - 4);
            command = new MuteCommand(cmd, channel, plugin);
        } else if (cmd.endsWith("join")) {
            commandLabel = cmd.substring(0, cmd.length() - 4);
            command = new JoinCommand(cmd, channel, plugin);
        } else {
            commandLabel = cmd;
            command = new SendCommand(cmd, channel, plugin);
        }

        return command;
    }
}
