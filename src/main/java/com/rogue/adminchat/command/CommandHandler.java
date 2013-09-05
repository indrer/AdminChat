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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @since 1.2.2
 * @author 1Rogue
 * @version 1.2.2
 */
public class CommandHandler implements CommandExecutor {

    private final AdminChat plugin;
    private Map<String, String> toggled = new HashMap();

    public CommandHandler(AdminChat p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        boolean toggle = false;
        if (commandLabel.toLowerCase().endsWith("toggle")) {
            toggle = true;
            commandLabel = commandLabel.substring(0, commandLabel.length() - 6);
        }
        plugin.getLogger().log(Level.INFO, "onCommand called! commandLabel = {0}", commandLabel);
        if (plugin.getChannelManager().getChannels().containsKey(commandLabel) && sender.hasPermission("adminchat.channel." + plugin.getChannelManager().getChannels().get(commandLabel).getName())) {
            if (toggle) {
                plugin.getLogger().log(Level.INFO, "toggle command called");
                if (sender instanceof Player) {
                    String chan = toggled.get(sender.getName());
                    if (chan != null && commandLabel.equalsIgnoreCase(chan)) {
                        toggled.remove(sender.getName());
                        plugin.communicate((Player) sender, "Automatic chat disabled!");
                    } else {
                        toggled.put(sender.getName(), commandLabel);
                        plugin.communicate((Player) sender, "Now chatting in channel: " + commandLabel + "!");
                    }
                }
            } else {
                plugin.getLogger().log(Level.INFO, "Normal command called");
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
                    plugin.adminBroadcast(commandLabel, name, msg.toString().trim());
                }
            }
        }
        return false;
    }
    
    /**
     * Returns a list of players that are toggled for admin chat
     *
     * @since 1.2.0
     * @version 1.2.2
     *
     * @return List of toggled players
     */
    public Map<String, String> getToggled() {
        return toggled;
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
                plugin.getCommand(cmd).setExecutor(this);
                plugin.getCommand(cmd + "toggle").setExecutor(this);
            }
        }
    }
}