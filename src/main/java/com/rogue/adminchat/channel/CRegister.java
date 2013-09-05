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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Template command, used for registering a command via reflection
 *
 * @since 1.3.0
 * @author 1Rogue
 * @version 1.3.0
 */
public class CRegister extends Command {

    private CommandExecutor exe = null;

    public CRegister(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (exe != null) {
            exe.onCommand(sender, this, commandLabel, args);
        }
        return false;
    }

    public void setExecutor(CommandExecutor exe) {
        this.exe = exe;
    }
}