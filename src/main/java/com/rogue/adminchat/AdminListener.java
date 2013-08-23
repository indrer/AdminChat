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

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @since 1.2.0
 * @author 1Rogue
 * @version 1.2.0
 */
public class AdminListener implements Listener {
    
    private AdminChat plugin;
    
    public AdminListener (AdminChat p) {
        plugin = p;
    }
    
    /**
     * Makes players who have toggled adminchat send chat to the appropriate
     * channels
     * 
     * @since 1.2.0
     * @version 1.2.0
     * 
     * @param event AsyncPlayerchatEvent instance
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (plugin.getToggled().contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            plugin.adminBroadcast(event.getPlayer().getName(), event.getMessage());
        }
    }
    
    /**
     * Sends a notification to ops/players with all of the plugin's permissions
     *
     * @since 1.2.0
     * @versino 1.2.0
     *
     * @param e The join event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("adminchat.updatenotice")) {
            if (plugin.isOutOfDate()) {
                e.getPlayer().sendMessage("An update for playtime is available!");
            }
        }
    }

}
