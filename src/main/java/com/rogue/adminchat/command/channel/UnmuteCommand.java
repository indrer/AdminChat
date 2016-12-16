package com.rogue.adminchat.command.channel;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import com.rogue.adminchat.command.CommandType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author MD678685
 * @version 1.5.0
 * @since 1.5.0
 */
public class UnmuteCommand extends BaseCommand {

    public UnmuteCommand(String command, Channel channel, AdminChat plugin) {
        super(command, CommandType.UNMUTE, channel, plugin);
    }

    /**
     * Executes the command, returning its success
     *
     * @param sender       Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args         All arguments passed to the command, split via ' '
     * @return true if the command was successful, otherwise false
     */
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                this.plugin.communicate(sender, "Unknown player: &c" + args[0]);
                return true;
            }
            channel.unmuteSender(target);
            plugin.communicate(target, "You have been unmuted in " + channel + ".");
            plugin.communicate(sender, target.getName() + " was unmuted in " + channel + ".");

        } else {
            this.plugin.communicate(sender, "Invalid arguments.");
            this.plugin.communicate(sender, "Usage: &c/<chan>unmute <player>");
        }
        return true;
    }
}
