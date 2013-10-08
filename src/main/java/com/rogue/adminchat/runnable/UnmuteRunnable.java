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
package com.rogue.adminchat.runnable;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.ChannelNotFoundException;
import java.util.logging.Level;

/**
 *
 * @since 1.3.2
 * @author 1Rogue
 * @version 1.3.2
 */
public class UnmuteRunnable implements Runnable {

    private final AdminChat plugin;
    private final String[] names;
    private final String channel;

    public UnmuteRunnable(AdminChat plugin, String channel, String... names) {
        this.plugin = plugin;
        this.names = names;
        this.channel = channel;
    }

    public void run() {
        try {
            this.plugin.getChannelManager().unmute(this.channel, names);
        } catch (ChannelNotFoundException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unknown channel called for unmute()", ex);
        }
    }
}
