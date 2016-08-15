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
        } else if (args[0].equalsIgnoreCase("muteall") && sender.hasPermission("adminchat.muteall")) { // Muteall command
            if (args.length >= 2) {
                long time = Long.parseLong(args[1]);
                if (args.length < 3) {
                    this.plugin.setGlobalMute(true);
                    this.plugin.getExecutiveManager().runAsyncTask(new Runnable() {
                        public void run() {
                            plugin.setGlobalMute(false);
                        }
                    }, time);
                } else {
                    StringBuilder invalidTargets = new StringBuilder();
                    int invalidTargetCount = 0;
                    List<String> targets = new ArrayList();
                    for (int i = 2; i < args.length; i++) {
                        Player target = this.plugin.getServer().getPlayer(args[i]);
                        if (target == null) {
                            invalidTargets.append("&c").append(args[2]).append("&a, ");
                            invalidTargetCount++;
                        } else {
                            targets.add(target.getName());
                        }
                        try {
                            this.plugin.getChannelManager().mute(null, targets.toArray(new String[targets.size()]));
                            this.plugin.getExecutiveManager().runAsyncTask(new UnmuteRunnable(
                                    this.plugin,
                                    null,
                                    targets.toArray(new String[targets.size()])), time);
                        } catch (ChannelNotFoundException ex) {
                            this.plugin.communicate(sender, ex.getMessage());
                        }
                    }
                    if (invalidTargets.length() != 0) {
                        this.plugin.communicate(sender, "Player" + ((invalidTargetCount == 1) ? "" : "s") + " " + invalidTargets.substring(0, invalidTargets.length() - 2) + " not found!");
                        return true;
                    }
                }
            } else {
                this.plugin.communicate(sender, "Invalid usage!");
                this.plugin.communicate(sender, "/adminchat muteall <time> [players...]");
                return true;
            }
        } else if (args[0] == "help" && sender.hasPermission("adminchat.help")) {
            this.plugin.communicate(sender, "/adminchat help - displays help");
            this.plugin.communicate(sender, "/adminchat muteall <time> [players...] - mutes [players...] or everyone for <time> seconds");
            this.plugin.communicate(sender, "/adminchat reload - reloads AdminChat");
        } else {
            this.plugin.communicate(sender, "Command " + args[0] + " not recognised!");
            this.plugin.communicate(sender, "For a list of valid subcommands, type /adminchat help!");
            return true;
        }
        return false;
    }
}
