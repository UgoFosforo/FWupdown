package me.architetto.fwupdown;

import me.architetto.fwupdown.command.UpDownCommands;
import me.architetto.fwupdown.command.UpDownTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Fwupdown extends JavaPlugin {

    private static Plugin plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        getConfig ().options ().copyDefaults ();
        saveDefaultConfig ();

        // commands
        this.getCommand("fwupdown").setExecutor(new UpDownCommands());

        // tabCompleter
        this.getCommand("fwupdown").setTabCompleter(new UpDownTabCompleter());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        plugin = null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static void log(String msg){

        Bukkit.getLogger().log(Level.INFO,"[FWupdowm] " + msg);
    }

}
