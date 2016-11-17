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
import org.bukkit.command.CommandSender;

import java.util.HashSet;

/**
 * A chat channel
 *
 * @since 1.3.0
 * @author 1Rogue
 * @author MD678685
 * @version 1.5.0
 */
public class Channel {

    private final AdminChat plugin;
    private final String name;
    private final String command;
    private final String format;
    private final HashSet<CommandSender> mutedSenders;
    private final HashSet<CommandSender> members;

    public Channel(AdminChat _plugin, String cmdname, String cmdtag, String cmdformat) {
        plugin = _plugin;
        name = cmdname;
        command = cmdtag;
        format = cmdformat;
        mutedSenders = new HashSet<CommandSender>();
        members = new HashSet<CommandSender>();
    }

    /**
     * Returns the channel's name, as it is in the configuration
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return The channel's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the command used for the channel
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return The channel command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the string format for the channel
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return The channel format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sends a message to players which have permission to read a channel
     *
     * @param sender The message sender
     * @param message The message to send
     */
    public void sendMessage(CommandSender sender, String message) throws SenderMutedException {

        if (this.isMuted(sender)) {
            throw new SenderMutedException("You are muted in this channel!");
        }

        String formattedMessage = this.plugin.getFormatHelper().formatMessage(format, sender, message);

        for (CommandSender member : members) {
            member.sendMessage(formattedMessage);
        }

        this.plugin.getLogger().info(formattedMessage);

    }

    /**
     * Returns whether or not a player is muted in a channel
     *
     * @param sender The sender to check
     * @since 1.4.0
     * @version 1.5.0
     * @return True if muted in the channel, false otherwise
     */
    public synchronized boolean isMuted(CommandSender sender) {
        return mutedSenders.contains(sender);
    }

    /**
     * Add a CommandSender to the channel.
     * @param sender The sender to add
     * @since 1.5.0
     * @version 1.5.0
     */
    public synchronized void addMember(CommandSender sender) {
        if (!members.contains(sender)) {
            members.add(sender);
        }
    }

    /**
     * Remove a CommandSender from the channel.
     * @param sender The sender to remove
     * @since 1.5.0
     * @version 1.5.0
     */
    public synchronized void removeMember(CommandSender sender) {
        if (members.contains(sender)) {
            members.remove(sender);
        }
    }

    /**
     * Gets members of the channel.
     * @return A copy of the HashSet of members
     * @since 1.5.0
     * @version 1.5.0
     */
    public synchronized HashSet<CommandSender> getMembers() {
        return (HashSet<CommandSender>) members.clone();
    }

    /**
     * Mute a sender in the channel.
     * @param sender The sender to mute
     * @since 1.5.0
     * @version 1.5.0
     */
    public synchronized void muteSender(CommandSender sender) {
        if (!mutedSenders.contains(sender)) {
            mutedSenders.add(sender);
        }
    }

    /**
     * Unmute a sender in the channel.
     * @param sender The sender to unmute
     * @since 1.5.0
     * @version 1.5.0
     */
    public synchronized void unmuteSender(CommandSender sender) {
        if (mutedSenders.contains(sender)) {
            mutedSenders.remove(sender);
        }
    }

}
