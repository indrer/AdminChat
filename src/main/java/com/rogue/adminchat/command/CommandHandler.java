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

import static com.rogue.adminchat.command.CommandType.*;
import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import com.rogue.adminchat.channel.ChannelManager;
import com.rogue.adminchat.channel.ChannelNotFoundException;
import com.rogue.adminchat.runnable.UnmuteRunnable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
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
    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("adminchat")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("adminchat.reload")) {
                if (sender instanceof Player) {
                    this.plugin.reload(sender.getName());
                } else {
                    this.plugin.reload();
                }
                return true;
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("muteall") && sender.hasPermission("adminchat.muteall")) {
                long time = Long.parseLong(args[1]);
                if (args.length < 3) {
                    this.plugin.setGlobalMute(true);
                    this.plugin.getExecutiveManager().runAsyncTask(new Runnable() {
                        @Override
                        public void run() {
                            plugin.setGlobalMute(false);
                        }
                    }, time);
                } else {
                    StringBuilder badtargets = new StringBuilder();
                    int badtar = 0;
                    List<String> targets = new ArrayList();
                    for (int i = 2; i < args.length; i++) {
                        Player target = this.plugin.getServer().getPlayer(args[i]);
                        if (target == null) {
                            badtargets.append("&c").append(args[2]).append("&a, ");
                            badtar++;
                        } else {
                            targets.add(target.getName());
                        }
                        try {
                            this.plugin.getChannelManager().mute(null, targets.toArray(new String[targets.size()]));
                            this.plugin.getExecutiveManager().runAsyncTask(new UnmuteRunnable(
                                    this.plugin,
                                    null,
                                    targets.toArray(new String[targets.size()])), time);
                        } catch (ChannelNotFoundException ex) {
                            this.plugin.communicate(sender.getName(), ex.getMessage());
                        }
                    }
                    if (badtargets.length() != 0) {
                        this.plugin.communicate(sender.getName(), "Player" + ((badtar == 1) ? "" : "s") + " " + badtargets.substring(0, badtargets.length() - 2) + " not found!");
                        return true;
                    }
                }
            }
        }
        final ChannelManager manager = this.plugin.getChannelManager();
        final Map<String, Channel> channels;
        synchronized (channels = manager.getChannels()) {
            ACCommand command = this.getCommand(commandLabel);
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
                            @Override
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
    private ACCommand getCommand(String cmd) {
        if (cmd.endsWith("toggle")) {
            String command = cmd.substring(0, cmd.length() - 6);
            if (this.plugin.getChannelManager().isChannel(command)) {
                return new ACCommand(command, CommandType.TOGGLE);
            }
        } else if (cmd.endsWith("unmute")) {
            String command = cmd.substring(0, cmd.length() - 6);
            if (this.plugin.getChannelManager().isChannel(command)) {
                return new ACCommand(command, CommandType.UNMUTE);
            }
        } if (cmd.endsWith("mute")) {
            String command = cmd.substring(0, cmd.length() - 4);
            if (this.plugin.getChannelManager().isChannel(command)) {
                return new ACCommand(command, CommandType.MUTE);
            }
        }
        if (this.plugin.getChannelManager().isChannel(cmd)) {
            return new ACCommand(cmd, CommandType.NORMAL);
        }
        return null;
    }
}