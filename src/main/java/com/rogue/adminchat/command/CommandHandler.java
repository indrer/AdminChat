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
import com.rogue.adminchat.channel.SenderMutedException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 1Rogue
 * @author MD678685
 * @version 1.4.5
 * @since 1.3.0
 */
public class CommandHandler implements CommandExecutor {

    private final AdminChat plugin;
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
        ACCommand command = this.getCommand(commandLabel, args);
        if (command == null) {
            this.plugin.communicate(sender, "Unknown command: &c" + commandLabel);
        }
        final Player target;
        final String channel = command.getCommand();
        switch (command.getType()) {
            case NORMAL:
                StringBuilder msg = new StringBuilder();
                if (args.length > 0) {
                    for (String s : args) {
                        msg.append(s).append(" ");
                    }
                    try {
                        manager.getChannel(commandLabel).sendMessage(sender, msg.toString().trim());
                    } catch (ChannelNotFoundException e) {
                        this.plugin.getLogger().log(Level.SEVERE, "Could not find channel " + channel, e);
                    } catch (SenderMutedException e) {
                        this.plugin.communicate(sender, "You are muted in this channel.");
                    }
                }
                break;
            case TOGGLE:
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String chan = player.getMetadata("adminchat-toggled").get(0).asString();
                    if (chan != null && commandLabel.equalsIgnoreCase(chan)) {
                        player.removeMetadata("adminchat-toggled", this.plugin);
                        this.plugin.communicate(sender, "Automatic chat disabled!");
                    } else {
                        player.removeMetadata("adminchat-toggled", this.plugin);
                        player.setMetadata("adminchat-toggled", new FixedMetadataValue(this.plugin, chan));
                        this.plugin.communicate(sender, "Now chatting in channel: '" + chan + "'!");
                    }

                }
                break;
            case MUTE:
                if (args.length < 1) {
                    this.plugin.communicate(sender, "Invalid arguments.");
                    this.plugin.communicate(sender, "Usage: &c/<chan>mute <player> [time]");
                }
                target = this.plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    this.plugin.communicate(sender, "Unknown player: &c" + args[0]);
                    return true;
                }
                long time = Long.parseLong(args[1]);
                try {
                    manager.getChannel(channel).muteSender(target);
                    plugin.communicate(target, "You have been muted in " + channel + ".");
                    plugin.communicate(sender, target.getName() + " was muted in " + channel + ".");
                    if (time > 0) {
                        this.plugin.getExecutiveManager().runAsyncTask(new Runnable() {
                            public void run() {
                                try {
                                    manager.getChannel(channel).unmuteSender(target);
                                    plugin.communicate(target, "You have been unmuted in " + channel + ".");
                                    plugin.communicate(sender, target.getName() + " was unmuted in " + channel + ".");
                                } catch (ChannelNotFoundException ex) {
                                    plugin.communicate(sender, ex.getMessage());
                                    plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                                }
                            }
                        }, time);
                    }
                } catch (ChannelNotFoundException ex) {
                    this.plugin.communicate(sender, ex.getMessage());
                }
                break;
            case UNMUTE:
                if (args.length == 1) {
                    target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        this.plugin.communicate(sender, "Unknown player: &c" + args[0]);
                        return true;
                    }
                    try {
                        manager.getChannel(channel).unmuteSender(target);
                        plugin.communicate(target, "You have been unmuted in " + channel + ".");
                        plugin.communicate(sender, target.getName() + " was unmuted in " + channel + ".");
                    } catch (ChannelNotFoundException ex) {
                        Logger.getLogger(CommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    this.plugin.communicate(sender, "Invalid arguments.");
                    this.plugin.communicate(sender, "Usage: &c/<chan>mute <player> <time> [channel]");
                }
                break;
            case JOIN:
                try {
                    if (sender instanceof Player) manager.getChannel(channel).addMember(sender);
                } catch (ChannelNotFoundException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Could not find channel " + channel, e);
                }
                break;
            case LEAVE:
                try {
                    if (sender instanceof Player) manager.getChannel(channel).removeMember(sender);
                } catch (ChannelNotFoundException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Could not find channel " + channel, e);
                }
                break;
            default:
                this.plugin.communicate(sender, "Unknown command!");
                break;
        }

        return false;
    }

    /**
     * Sets the command handler as the executor for channel commands
     *
     * @version 1.5.0
     * @since 1.3.0
     */
    public void setExecs() {
        final Map<String, Channel> channels;
        synchronized (channels = plugin.getChannelManager().getChannels()) {
            for (String cmd : channels.keySet()) {
                for (CommandType type : CommandType.values()) {
                    this.plugin.getCommand(cmd + type.getCommand()).setExecutor(this);
                }
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