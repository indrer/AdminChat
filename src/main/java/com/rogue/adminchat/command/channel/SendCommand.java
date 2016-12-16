package com.rogue.adminchat.command.channel;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import com.rogue.adminchat.channel.SenderMutedException;
import com.rogue.adminchat.command.CommandType;
import org.bukkit.command.CommandSender;

/**
 * Handles the execution of the send command
 *
 * @since 1.5.0
 * @author MD678685
 * @version 1.5.0
 */
public class SendCommand extends BaseCommand {

    public SendCommand(String command, Channel channel, AdminChat plugin) {
        super(command, CommandType.NORMAL, channel, plugin);
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
        StringBuilder msg = new StringBuilder();
        if (args.length > 0) {
            for (String s : args) {
                msg.append(s).append(" ");
            }
            try {
                channel.sendMessage(sender, msg.toString().trim());
            } catch (SenderMutedException e) {
                this.plugin.communicate(sender, "You are muted in this channel.");
                return false;
            }
        }
        return true;
    }
}
