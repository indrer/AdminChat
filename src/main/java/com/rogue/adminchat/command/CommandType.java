/*
 * Copyright (C) 2013 Spencer
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

/**
 * @since 1.3.2
 * @author 1Rogue
 * @version 1.3.2
 */
public enum CommandType {
    
    NORMAL("normal"),
    TOGGLE("toggle"),
    MUTE("mute"),
    UNMUTE("unmute");
    private final String name;
    
    private CommandType(String name) {
        this.name = name;
    }
    
    public CommandType getCommand(String type) {
        for (CommandType enu : CommandType.values()) {
            if (enu.name().equals(type.toLowerCase())) {
                return enu;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
