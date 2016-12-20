package com.rogue.adminchat.command;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.ChannelNotFoundException;
import com.rogue.adminchat.runnable.UnmuteRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @since 1.4.5
 * @author MD678685
 * @version 1.5.0
 */

public class MainCommand implements CommandExecutor {

    private final Map<String, String> toggled = new ConcurrentHashMap();

    public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String[] args) {
        AdminChat plugin = AdminChat.getPlugin();
        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("adminchat.reload")) { // Reload command
            if (sender instanceof Player) {
                plugin.reload(sender.getName());
            } else {
                plugin.reload();
            }
            return true;
        } else if (args[0] == "help" && sender.hasPermission("adminchat.help")) {
            plugin.communicate(sender, "/adminchat help - displays help");
            plugin.communicate(sender, "/adminchat reload - reloads AdminChat");
        } else {
            plugin.communicate(sender, "Command " + args[0] + " not recognised!");
            plugin.communicate(sender, "For a list of valid subcommands, type /adminchat help!");
            return true;
        }
        return false;
    }
}
