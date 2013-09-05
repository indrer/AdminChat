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

/**
 *
 * @since 1.3.0
 * @author 1Rogue
 * @version 1.3.0
 */
public class Channel {
    
    private final String name;
    private final String command;
    private final String format;
    
    public Channel(String cmdname, String cmdtag, String cmdformat) {
        name = cmdname;
        command = cmdtag;
        format = cmdformat;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCommand() {
        return command;
    }
    
    public String getFormat() {
        return format;
    }

}
