package com.rogue.adminchat.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This is placeholder documentation for the file.
 * <p>
 * Created by md678685 on 25/08/2016.
 */
public class FormatHelper {

    private final Formatter formatter;

    public FormatHelper(String formatterToUse) {
        formatter = Formatter.getFormatter(formatterToUse);
    }

    /**
     * Format a message to be sent from a chat channel
     *
     * @param format Message format
     * @param sender Message sender
     * @param message Message to be formatted
     * @return
     */
    public String formatMessage(String format, CommandSender sender, String message) {

        return formatACMessage(format, sender, message);

    }

    private String formatACMessage(String format, CommandSender sender, String message) {

        String displayName;
        if (sender instanceof Player) {
            displayName = ((Player) sender).getDisplayName();
        } else {
            displayName = sender.getName();
        }

        String senderName = sender.getName();

        format = format.replace("{NAME}", senderName)
                .replace("{MESSAGE}", message)
                .replace("{DISPLAYNAME}", displayName);

        return format;
    }

    private enum Formatter {

        BASIC,
        VAULT,
        PLACEHOLDER_API;

        public static Formatter getFormatter(String type) {
            for (Formatter enu : Formatter.values()) {
                if (enu.toString().equalsIgnoreCase(type)) {
                    return enu;
                }
            }
            return null;
        }

    }

}
