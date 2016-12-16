package com.rogue.adminchat.command;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.ChannelNotFoundException;
import com.rogue.adminchat.runnable.UnmuteRunnable;
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
 * @version 1.4.5
 */

public class MainCommand {

    private final AdminChat plugin;
    private final Map<String, String> toggled = new ConcurrentHashMap();

    public MainCommand(AdminChat plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("adminchat.reload")) { // Reload command
            if (sender instanceof Player) {
                this.plugin.reload(sender.getName());
            } else {
                this.plugin.reload();
            }
            return true;
        } else if (args[0] == "help" && sender.hasPermission("adminchat.help")) {
            this.plugin.communicate(sender, "/adminchat help - displays help");
            this.plugin.communicate(sender, "/adminchat reload - reloads AdminChat");
        } else {
            this.plugin.communicate(sender, "Command " + args[0] + " not recognised!");
            this.plugin.communicate(sender, "For a list of valid subcommands, type /adminchat help!");
            return true;
        }
        return false;
    }
}
