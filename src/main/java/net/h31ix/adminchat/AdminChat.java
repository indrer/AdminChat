package net.h31ix.adminchat;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminChat extends JavaPlugin {
    
    private String SEND = "&aADMIN: {0}: &c{1}";
    
    @Override
    public void onLoad() {
        File file = new File(this.getDataFolder(), "config.yml");
        if (this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        if (!file.exists()) {
            this.saveDefaultConfig();
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            
            SEND = yaml.getString("format");
        } else {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            if (!yaml.isSet("format")) { yaml.set("format", "&aADMIN: {0}: &c{1}"); }
            
            SEND = yaml.getString("format");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ac") && sender.hasPermission("adminchat.use")) {
            StringBuilder msg = new StringBuilder();
            if (args.length > 0) {
                for (String s : args) {
                    msg.append(s);
                }
                String name = "";
                if (sender instanceof Player) {
                    name = sender.getName();
                } else {
                    name = "CONSOLE";
                }
                Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', this.getMessage(name, msg.toString())), "adminchat.read");
            }
        }
        return true;
    }
    
    private String getMessage(String... vars) {
        String send = SEND;
        for (int i = 0; i < vars.length; i++) {
            send = send.replace("{" + i + "}", vars[i]);
        }
        return send;
    }
}
