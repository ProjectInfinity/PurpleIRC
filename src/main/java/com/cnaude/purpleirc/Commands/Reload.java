/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.purpleirc.Commands;

import com.cnaude.purpleirc.PurpleIRC;
import org.bukkit.command.CommandSender;

/**
 *
 * @author cnaude
 */
public class Reload implements IRCCommandInterface {

    private final PurpleIRC plugin;
    private final String usage = "";
    private final String desc = "Reload the plugin.";
    private final String name = "reload";

    /**
     *
     * @param plugin
     */
    public Reload(PurpleIRC plugin) {
        this.plugin = plugin;
    }

    /**
     *
     * @param sender
     * @param args
     */
    @Override
    public void dispatch(CommandSender sender, String[] args) {
        sender.sendMessage("Disabling PurpleIRC...");
        plugin.getServer().getPluginManager().disablePlugin(plugin);
        sender.sendMessage("Enabling PurpleIRC...");
        plugin.getServer().getPluginManager().enablePlugin(plugin);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String desc() {
        return desc;
    }

    @Override
    public String usage() {
        return usage;
    }
}
