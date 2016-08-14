/*
 * Copyright (C) 2013 AE97
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Adopted from TotalPermissions
 *
 * @since 1.2.0
 * @author Lord_Ralex
 * @version 1.2.1
 */
public class UpdateRunnable implements Runnable {

    private final String VERSION_URL = "https://raw.github.com/md678685/AdminChat/master/VERSION";
    private Boolean isLatest = null;
    private String latest;
    private String version;
    private final AdminChat plugin;

    public UpdateRunnable(AdminChat plugin) {
        super();
        this.plugin = plugin;
        this.version = plugin.getDescription().getVersion();
    }

    public void run() {
        if (this.version.endsWith("SNAPSHOT") || version.endsWith("DEV")) {
            this.plugin.getLogger().warning("Dev build detected, update checking disabled");
            this.isLatest = true;
            return;
        }
        try {
            URL call = new URL(this.VERSION_URL);
            InputStream stream = call.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            this.latest = reader.readLine();
            reader.close();
            this.plugin.setUpdateStatus(!this.latest.equalsIgnoreCase(this.version));
        } catch (MalformedURLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Error checking for update", ex);
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Error checking for update", ex);
        }
    }
}