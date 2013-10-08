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
package com.rogue.adminchat.command;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @since 1.3.0
 * @author 1Rogue
 * @version 1.3.1
 */
public class CommandHandler implements CommandExecutor {

    private final AdminChat plugin;
    private final Map<String, String> toggled = new ConcurrentHashMap();

    public CommandHandler(AdminChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("adminchat")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("adminchat.reload")) {
                if (sender instanceof Player) {
                    this.plugin.reload(sender.getName());
                } else {
                    this.plugin.reload();
                }
                return false;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("muteall") && sender.hasPermission("adminchat.muteall")) {
                
            }
        }
        
        // This needs to be rewritten using getCommand(command)
        boolean toggle = false;
        if (commandLabel.toLowerCase().endsWith("toggle")) {
            toggle = true;
            commandLabel = commandLabel.substring(0, commandLabel.length() - 6);
        }
        if (this.plugin.getChannelManager().getChannels().containsKey(commandLabel)
                && sender.hasPermission("adminchat.channel."
                + this.plugin.getChannelManager().getChannels().get(commandLabel).getName()
                + ".send")) {
            if (toggle) {
                if (sender instanceof Player) {
                    synchronized (this.toggled) {
                        String chan = this.toggled.get(sender.getName());
                        if (chan != null && commandLabel.equalsIgnoreCase(chan)) {
                            this.toggled.remove(sender.getName());
                            this.plugin.communicate((Player) sender, "Automatic chat disabled!");
                        } else {
                            this.toggled.put(sender.getName(), commandLabel);
                            this.plugin.communicate((Player) sender, "Now chatting in channel: '" + commandLabel + "'!");
                        }
                    }
                }
            } else {
                StringBuilder msg = new StringBuilder();
                if (args.length > 0) {
                    for (String s : args) {
                        msg.append(s).append(" ");
                    }
                    String name;
                    if (sender instanceof Player) {
                        name = sender.getName();
                    } else {
                        name = "CONSOLE";
                    }
                    this.plugin.adminBroadcast(commandLabel, name, msg.toString().trim());
                }
            }
        }
        return false;
    }

    /**
     * Returns a list of players that are toggled for admin chat
     *
     * @since 1.2.0
     * @version 1.3.0
     *
     * @return List of toggled players
     */
    public Map<String, String> getToggled() {
        return this.toggled;
    }

    /**
     * Sets the command handler as the executor for channel commands
     *
     * @since 1.3.0
     * @version 1.3.0
     */
    public void setExecs() {
        final Map<String, Channel> channels;
        synchronized (channels = plugin.getChannelManager().getChannels()) {
            for (String cmd : channels.keySet()) {
                this.plugin.getCommand(cmd).setExecutor(this);
                this.plugin.getCommand(cmd + "toggle").setExecutor(this);
            }
            this.plugin.getCommand("adminchat").setExecutor(this);
        }
    }
    
    private ACCommand getCommand(String cmd) {
        return null;
    }
}