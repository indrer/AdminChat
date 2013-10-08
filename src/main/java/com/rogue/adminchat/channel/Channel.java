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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @since 1.3.0
 * @author 1Rogue
 * @version 1.3.2
 */
public class Channel {

    private final String name;
    private final String command;
    private final String format;
    private final List<String> muted;

    public Channel(String cmdname, String cmdtag, String cmdformat) {
        name = cmdname;
        command = cmdtag;
        format = cmdformat;
        this.muted = new ArrayList();
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
     * Returns a list of the muted players in this channel
     * 
     * @since 1.3.2
     * @version 1.3.2
     * 
     * @return All muted players for the channel
     */
    public List<String> getMuted() {
        return this.muted;
    }
    
    public boolean isMuted(String name) {
        return this.muted.contains(name);
    }
}
