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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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
            plugin.getLogger().log(Level.INFO, "s = {0}", s);
            plugin.getLogger().log(Level.INFO, "format = {0}", format);
            plugin.getLogger().log(Level.INFO, "cmd = {0}", cmd);
            if (format != null && cmd != null) {
                plugin.getLogger().log(Level.INFO, "Adding command {0}!", cmd);
                channels.put(cmd, new Channel(s, cmd, format));
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
            if (channels.keySet().isEmpty()) {
                plugin.getLogger().severe("fml BURN EVERYTHING");
            }
            for (String s : channels.keySet()) {


                //  ATTEMPTED METHOD
                PluginCommand pc = this.getCommand(s, plugin);
                if (pc != null) {
                    scm.register(".", pc);
                    plugin.getCommand(s).setExecutor(plugin.getCommandHandler());
                } else {
                    plugin.getLogger().severe("pc is NULL!");
                }



                //  ANOTHER ATTEMPTED METHOD
                 /*CRegister cmd = new CRegister(s);
                 cmd.setExecutor(plugin.getCommandHandler());
                 CRegister togglecmd = new CRegister(s + "toggle");
                 togglecmd.setExecutor(plugin.getCommandHandler());
                 plugin.getLogger().log(Level.INFO, "Registering {0}!", cmd.getName());
                 scm.register(".", cmd);
                 scm.register(".", togglecmd);*/
            }
            if (plugin.getCommand("ac") != null) {
                plugin.getLogger().info("ac is not null!");
                plugin.getCommand("ac").setExecutor(plugin.getCommandHandler());
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
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return command;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }
}
