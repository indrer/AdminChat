package com.rogue.adminchat.command.channel;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import com.rogue.adminchat.command.CommandType;
import org.bukkit.command.CommandSender;

/**
 * @author MD678685
 * @version 1.5.0
 * @since 1.5.0
 */
public class LeaveCommand extends BaseCommand {

    public LeaveCommand(String command, Channel channel, AdminChat plugin) {
        super(command, CommandType.LEAVE, channel, plugin);
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
        channel.removeMember(sender);
        return true;
    }
}
