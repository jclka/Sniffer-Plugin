package me.jc.snifferRide;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

public class SnifferRide extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new RideEvent(this), this);
    }
    public void sendMiniMessage(CommandSender sender, String message) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("sniffer")) {

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

                reloadConfig();
                long start = System.currentTimeMillis();

                reloadConfig();

                long end = System.currentTimeMillis();
                long time = end - start;
                sendMiniMessage(sender, "<green>SnifferRide config reloaded in " + time + "ms.");


                return true;
            }
        }

        sendMiniMessage(sender, "<red> Usage: /Sniffer Reload");
        return true;

    }
}