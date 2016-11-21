package com.rogue.adminchat.util;

import com.rogue.adminchat.AdminChat;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * A formatting helper class.
 *
 * @author MD678685
 * @since 1.5.0
 * @version 1.5.0
 */

// TODO: REWORK THIS COMPLETELY
public class FormatHelper {

    private IFormatter formatter;
    private AdminChat plugin;

    public FormatHelper(AdminChat plugin) {
        this.plugin = plugin;

        if (this.plugin.getServer().getServicesManager().isProvidedFor(Chat.class)) {
            this.setFormatter(new VaultFormatter(this.plugin));
        } else {
            this.setFormatter(new DefaultFormatter());
        }
    }

    public void setFormatter(IFormatter formatter1) {
        this.formatter = formatter1;
    }

    /**
     * Format a message to be sent from a chat channel
     *
     * @param format Message format
     * @param sender Message sender
     * @param message Message to be formatted
     * @return Formatted string
     */
    public String formatMessage(String format, CommandSender sender, String message) {

        return formatMessage(format, sender, message, this.formatter);

    }

    /**
     * Format a message to be sent from a chat channel using the specified formatter
     *
     * @param format Message format
     * @param sender Message sender
     * @param message Message to be formatted
     * @return Formatted string
     */
    public String formatMessage(String format, CommandSender sender, String message, IFormatter formatter) {

        return formatter.formatMessage(format, sender, message);

    }

    public interface IFormatter {
        /**
         * Format a message.
         *
         * @param format Message format
         * @param sender Message sender
         * @param message Message to be formatted
         * @return Formatter string
         */
        String formatMessage(String format, CommandSender sender, String message);
    }

    private class DefaultFormatter implements IFormatter {

        /**
         * Format a message using the default formatter
         *
         * @param format Message format
         * @param sender Message sender
         * @param message Message to be formatted
         * @return Formatter string
         */
        public String formatMessage(String format, CommandSender sender, String message) {
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
    }

    private class VaultFormatter implements IFormatter {

        private Chat chatService;
        private Permission permService;
        private DefaultFormatter defaultFormatter;

        public VaultFormatter(AdminChat plugin) {
            this.chatService = plugin.getServer().getServicesManager().getRegistration(Chat.class).getProvider();
            this.permService = plugin.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
            this.defaultFormatter = new DefaultFormatter();
        }

        /**
         * Format a message.
         *
         * @param format  Message format
         * @param sender  Message sender
         * @param message Message to be formatted
         * @return Formatter string
         */
        public String formatMessage(String format, CommandSender sender, String message) {
            String prefix = "";
            String suffix = "";
            String group = "";
            String allGroups = "";

            if (sender instanceof Player) {
                Player player = (Player) sender;
                prefix = this.chatService.getPlayerPrefix(player);
                suffix = this.chatService.getPlayerSuffix(player);
                group = this.permService.getPrimaryGroup(player);

                for (String group1 : this.permService.getPlayerGroups(player)) {
                    allGroups += group1 + "|";
                }

                if (allGroups.length() > 0) allGroups = allGroups.substring(0, allGroups.length() - 1);
            }

            String formattedMessage = defaultFormatter.formatMessage(format, sender, message)
                    .replace("{PREFIX}", prefix)
                    .replace("{SUFFIX}", suffix)
                    .replace("{GROUP}", group)
                    .replace("{ALLGROUPS}", allGroups);

            return formattedMessage;
        }
    }

}
