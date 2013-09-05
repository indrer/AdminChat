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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @since 1.2.2
 * @author 1Rogue
 * @version 1.2.2
 */
public class ChannelManager {

    private final Map<String, Channel> channels = new HashMap();
    private final AdminChat plugin;

    public ChannelManager(AdminChat p) {
        plugin = p;
        try {
            setup();
        } catch (IOException ex) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, "Error grabbing channels!", ex);
        }
    }

    private void setup() throws IOException {
        if (plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        File chan = new File(plugin.getDataFolder() + File.separator + "channels.yml");
        if (!chan.exists()) {
            plugin.saveResource("channels.yml", true);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(chan);
        ConfigurationSection sect = yaml.getConfigurationSection("channels");
        if (sect == null) {
            plugin.getLogger().severe("No channels found, disabling!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        for (String s : sect.getKeys(false)) {
            String format = yaml.getString("channels." + s + ".format");
            String cmd = yaml.getString("channels." + s + ".command");
            if (format != null && cmd != null) {
                plugin.getLogger().log(Level.INFO, "Adding command {0}!", cmd);
                channels.put(cmd, new Channel(s, cmd, format));
                Permission perm = new Permission("adminchat.channel." + cmd);
                perm.setDefault(PermissionDefault.OP);
                try {
                    plugin.getLogger().config("Registering " + perm.getName());
                    Bukkit.getPluginManager().addPermission(perm);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("The permission " + perm.getName() + " already existed!");
                }
            }
        }
        register();
    }

    private void register() {
        SimpleCommandMap scm;
        try {
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);
            scm = (SimpleCommandMap) f.get(Bukkit.getPluginManager());
            if (!channels.keySet().isEmpty()) {
                for (String s : channels.keySet()) {
                    PluginCommand pc = this.getCommand(s, plugin);
                    PluginCommand pctoggle = this.getCommand(s + "toggle", plugin);
                    if (pc != null && pctoggle != null) {
                        scm.register(".", pc);
                        scm.register(".", pctoggle);
                    }
                }
            }
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private PluginCommand getCommand(String name, Plugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            command = c.newInstance(name, plugin);
        } catch (SecurityException e) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalArgumentException e) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalAccessException e) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, e);
        } catch (InstantiationException e) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, e);
        } catch (InvocationTargetException e) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, e);
        } catch (NoSuchMethodException e) {
            Logger.getLogger(ChannelManager.class.getName()).log(Level.SEVERE, null, e);
        }

        return command;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }
}
