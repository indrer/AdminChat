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
package com.rogue.adminchat.command.channel;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import com.rogue.adminchat.command.CommandType;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

/**
 *
 * @since 1.3.2
 * @author 1Rogue
 * @version 1.3.2
 */
public abstract class BaseCommand extends Command implements PluginIdentifiableCommand {
    
    protected final String command;
    protected final CommandType type;
    protected final AdminChat plugin;
    protected final Channel channel;
    
    public BaseCommand(String command, CommandType type, Channel channel, AdminChat plugin) {
        super(command);
        this.command = command;
        this.type = type;
        this.plugin = plugin;
        this.channel = channel;
    }
    
    public String getCommand() {
        return this.command;
    }

    public CommandType getType() {
        return this.type;
    }

    /**
     * Gets the owner of this PluginIdentifiableCommand.
     *
     * @return Plugin that owns this PluginIdentifiableCommand.
     */
    public Plugin getPlugin() {
        return plugin;
    }
}
