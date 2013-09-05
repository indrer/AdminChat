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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

/**
 *
 * @since 1.3.0
 * @author 1Rogue
 * @version 1.3.0
 */
public class ChannelManager {

    private final Map<String, Channel> channels = new ConcurrentHashMap();
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
     * new one if it does not exist
     * 
     * @since 1.3.0
     * @version 1.3.0
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
            this.plugin.getLogger().severe("No channels found, disabling!");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
            return;
        }
        for (String s : sect.getKeys(false)) {
            String format = yaml.getString("channels." + s + ".format");
            String cmd = yaml.getString("channels." + s + ".command");
            if (format != null && cmd != null) {
                this.plugin.getLogger().log(Level.CONFIG, "Adding command {0}!", cmd);
                this.channels.put(cmd, new Channel(s, cmd, format));
                Permission perm = new Permission("adminchat.channel." + cmd);
                perm.setDefault(PermissionDefault.OP);
                try {
                    this.plugin.getLogger().log(Level.CONFIG, "Registering {0}", perm.getName());
                    Bukkit.getPluginManager().addPermission(perm);
                } catch (IllegalArgumentException e) {
                    this.plugin.getLogger().log(Level.WARNING, "The permission {0} already existed!", perm.getName());
                }
            }
        }
        register();
    }

    /**
     * Registers the channel commands with bukkit's command map dynamically. If
     * a command already exists, it will be prefixed with a period.
     * 
     * @since 1.3.0
     * @version 1.3.0
     */
    private void register() {
        SimpleCommandMap scm;
        try {
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);
            scm = (SimpleCommandMap) f.get(Bukkit.getPluginManager());
            if (!this.channels.keySet().isEmpty()) {
                for (String s : this.channels.keySet()) {
                    PluginCommand pc = this.getCommand(s, this.plugin);
                    PluginCommand pctoggle = this.getCommand(s + "toggle", this.plugin);
                    if (pc != null && pctoggle != null) {
                        scm.register(".", pc);
                        scm.register(".", pctoggle);
                    }
                }
            }
        } catch (NoSuchFieldException ex) {
            this.plugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            this.plugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            this.plugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.SEVERE, null, ex);
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
     * @return New PluginCommand object, or null upon an exception
     */
    private PluginCommand getCommand(String name, Plugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            command = c.newInstance(name, plugin);
        } catch (SecurityException e) {
            this.plugin.getLogger().log(Level.SEVERE, null, e);
        } catch (IllegalArgumentException e) {
            this.plugin.getLogger().log(Level.SEVERE, null, e);
        } catch (IllegalAccessException e) {
            this.plugin.getLogger().log(Level.SEVERE, null, e);
        } catch (InstantiationException e) {
            this.plugin.getLogger().log(Level.SEVERE, null, e);
        } catch (InvocationTargetException e) {
            this.plugin.getLogger().log(Level.SEVERE, null, e);
        } catch (NoSuchMethodException e) {
            this.plugin.getLogger().log(Level.SEVERE, null, e);
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
     * @param name The channel command
     * @return The channel object, null if channel does not exist
     */
    public Channel getChannel(String name) {
        final Map<String, Channel> chans;
        synchronized (chans = this.channels) {
            return chans.get(name);
        }
    }
}
