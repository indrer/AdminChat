package com.rogue.adminchat.command.channel;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import com.rogue.adminchat.channel.ChannelNotFoundException;
import com.rogue.adminchat.command.CommandType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * @author MD678685
 * @version 1.5.0
 * @since 1.5.0
 */
public class MuteCommand extends BaseCommand {

    public MuteCommand(String command, Channel channel, AdminChat plugin) {
        super(command, CommandType.MUTE, channel, plugin);
    }

    /**
     * Executes the command, returning its success
     *
     * @param sender       Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args         All arguments passed to the command, split via ' '
     * @return true if the command was successful, otherwise false
     */
    public boolean execute(final CommandSender sender, String commandLabel, String[] args) {
        final Player target;
        switch (args.length) {
            case 1:
                target = this.plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    this.plugin.communicate(sender, "Unknown player: &c" + args[0]);
                    return false;
                }
                channel.muteSender(target);
                plugin.communicate(target, "You have been muted in " + channel + ".");
                plugin.communicate(sender, target.getName() + " was muted in " + channel + ".");
                return true;
            case 2:
                target = this.plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    this.plugin.communicate(sender, "Unknown player: &c" + args[0]);
                    return false;
                }
                channel.muteSender(target);
                plugin.communicate(target, "You have been muted in " + channel + ".");
                plugin.communicate(sender, target.getName() + " was muted in " + channel + ".");
                long time = Long.parseLong(args[1]);
                if (time > 0) {
                    this.plugin.getExecutiveManager().runAsyncTask(new Runnable() {
                        public void run() {
                            channel.unmuteSender(target);
                            plugin.communicate(target, "You have been unmuted in " + channel + ".");
                            plugin.communicate(sender, target.getName() + " was unmuted in " + channel + ".");
                        }
                    }, time);
                }
            default:
                this.plugin.communicate(sender, "Invalid arguments.");
                this.plugin.communicate(sender, "Usage: &c/<chan>mute <player> [seconds]");
                break;
        }
        return false;
    }
}
