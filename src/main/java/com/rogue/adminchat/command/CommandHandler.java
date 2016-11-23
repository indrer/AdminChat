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
import com.rogue.adminchat.channel.ChannelManager;
import com.rogue.adminchat.channel.ChannelNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @since 1.3.0
 * @author 1Rogue
 * @author MD678685
 * @version 1.4.5
 */
public class CommandHandler implements CommandExecutor {

    private final AdminChat plugin;
    private final Map<String, String> toggled = new ConcurrentHashMap();
    private final MainCommand mainCommand;

    public CommandHandler(AdminChat plugin) {
        this.plugin = plugin;
        this.mainCommand = new MainCommand(plugin);
    }

    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("adminchat")) {
            return this.mainCommand.onCommand(sender, args);
        }
        final ChannelManager manager = this.plugin.getChannelManager();
        final Map<String, Channel> channels;
        synchronized (channels = manager.getChannels()) {
            ACCommand command = this.getCommand(commandLabel, args);
            if (command == null) {
                this.plugin.communicate(sender.getName(), "Unknown command: &c" + commandLabel);
            }
            String chanName;
            try {
                chanName = manager.getChannel(command.getCommand()).getName();
            } catch (ChannelNotFoundException ex) {
                this.plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                this.plugin.communicate(sender.getName(), ex.getMessage());
                return false;
            }
            Player target;
            final String channel = command.getCommand();
            switch (command.getType()) {
                case NORMAL:
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
                        manager.sendMessage(commandLabel, name, msg.toString().trim());
                    }
                    break;
                case TOGGLE:
                    if (sender instanceof Player) {
                        synchronized (this.toggled) {
                            String chan = this.toggled.get(sender.getName());
                            if (chan != null && commandLabel.equalsIgnoreCase(chan)) {
                                this.toggled.remove(sender.getName());
                                this.plugin.communicate((Player) sender, "Automatic chat disabled!");
                            } else {
                                this.toggled.put(sender.getName(), commandLabel);
                                this.plugin.communicate((Player) sender, "Now chatting in channel: '" + channel + "'!");
                            }
                        }
                    }
                    break;
                case MUTE:
                    if (args.length < 2) {
                        this.plugin.communicate(sender.getName(), "Invalid arguments.");
                        this.plugin.communicate(sender.getName(), "Usage: &c/<chan>mute <Player> <time> [channel]");
                    }
                    target = this.plugin.getServer().getPlayer(args[0]);
                    if (target == null) {
                        this.plugin.communicate(sender.getName(), "Unknown player: &c" + args[0]);
                        return true;
                    }
                    long time = Long.parseLong(args[1]);
                    final String name = target.getName();
                    try {
                        manager.mute(channel, target.getName());
                        this.plugin.getExecutiveManager().runAsyncTask(new Runnable() {
                            public void run() {
                                try {
                                    manager.unmute(channel, name);
                                } catch (ChannelNotFoundException ex) {
                                    plugin.communicate(sender.getName(), ex.getMessage());
                                }
                            }
                        }, time);
                    } catch (ChannelNotFoundException ex) {
                        this.plugin.communicate(sender.getName(), ex.getMessage());
                    }
                    break;
                case UNMUTE:
                    if (args.length == 1) {
                        target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            this.plugin.communicate(sender.getName(), "Unknown Player: &c" + args[0]);
                            return true;
                        }
                        try {
                            manager.unmute(channel, target.getName());
                        } catch (ChannelNotFoundException ex) {
                            Logger.getLogger(CommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        this.plugin.communicate(sender.getName(), "Invalid arguments.");
                        this.plugin.communicate(sender.getName(), "Usage: &c/<chan>mute <Player> <time> [channel]");
                    }
                    break;
                default:
                    this.plugin.communicate(sender.getName(), "Unknown Command!");
                    break;
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

    /**
     * Returns AdminChat's custom command class for a provided command name
     * 
     * @param cmd The command to try against
     * @return The {@link ACCommand} version of the command
     */
    private ACCommand getCommand(String cmd, String[] args) {
        String command;
        CommandType type = null;
        if (cmd.endsWith("toggle") || cmd.endsWith("unmute")) {
            command = cmd.substring(0, cmd.length() - 6);
        } else if (cmd.endsWith("leave")) {
            command = cmd.substring(0, cmd.length() - 5);
        } else if (cmd.endsWith("mute") || cmd.endsWith("join")) {
            command = cmd.substring(0, cmd.length() - 4);
        } else {
            command = cmd;
            if (args.length < 1) type = CommandType.TOGGLE;
        }
        if (type != null) type = CommandType.getCommand(cmd.substring(command.length()));
        return new ACCommand(command, type);
    }
}