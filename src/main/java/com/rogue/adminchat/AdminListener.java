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
package com.rogue.adminchat;

import java.util.Map;
import java.util.logging.Level;

import com.rogue.adminchat.channel.ChannelNotFoundException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @since 1.2.0
 * @author 1Rogue
 * @version 1.3.0
 */
public class AdminListener implements Listener {

    private final AdminChat plugin;

    public AdminListener(AdminChat plugin) {
        this.plugin = plugin;
    }

    /**
     * Makes players who have toggled adminchat send chat to the appropriate
     * channels
     *
     * @since 1.2.0
     * @version 1.3.0
     *
     * @param event AsyncPlayerChatEvent instance
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        final Map<String, String> toggled;
        final String name = event.getPlayer().getName();
        synchronized (toggled = this.plugin.getCommandHandler().getToggled()) {
            String chan = toggled.get(name);
            try {
                event.setCancelled(true);
                this.plugin.getChannelManager().getChannel(chan).sendMessage(toggled.get(name), name, event.getMessage());
            } catch (ChannelNotFoundException e) {
                this.plugin.communicate(event.getPlayer(), "Could not find the channel you were toggled in! This should not happen!");
                this.plugin.getLogger().log(Level.SEVERE, "Could not find the channel " + name + " is toggled in! This should not happen!", e);
            }
        }
    }

    /**
     * Sends a notification to ops/players with all of the plugin's permissions
     *
     * @since 1.2.0
     * @version 1.2.1
     *
     * @param e The join event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("adminchat.updatenotice")) {
            if (plugin.isOutOfDate()) {
                plugin.communicate(e.getPlayer(), "An update is available for Adminchat!");
            }
        }
    }
}