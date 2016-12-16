package com.rogue.adminchat.command;

import com.rogue.adminchat.AdminChat;
import com.rogue.adminchat.channel.Channel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * @author MD678685
 * @version 1.5.0
 * @since 1.5.0
 */
public class ToggleCommand extends BaseCommand {

    public ToggleCommand(String command, Channel channel, AdminChat plugin) {
        super(command, CommandType.TOGGLE, channel, plugin);
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
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String chan = player.getMetadata("adminchat-toggled").get(0).asString();
            if (chan != null && commandLabel.equalsIgnoreCase(chan)) {
                player.removeMetadata("adminchat-toggled", this.plugin);
                this.plugin.communicate(sender, "Automatic chat disabled!");
            } else {
                player.removeMetadata("adminchat-toggled", this.plugin);
                player.setMetadata("adminchat-toggled", new FixedMetadataValue(this.plugin, channel.getName()));
                this.plugin.communicate(sender, "Now chatting in channel: '" + channel.getName() + "'!");
            }

        }
        return true;
    }
}
