package me.architetto.fwupdown;

import me.architetto.fwupdown.command.UpDownCommands;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Fwupdown extends JavaPlugin {

    private static Plugin plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        getConfig ().options ().copyDefaults ();
        saveDefaultConfig ();


        // commands
        getCommand ( "fwupdown" ).setExecutor ( new UpDownCommands () );

        // tabCompleter

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        plugin = null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }
}
