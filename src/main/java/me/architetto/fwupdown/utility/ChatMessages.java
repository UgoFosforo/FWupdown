package me.architetto.fwupdown.utility;

import org.bukkit.ChatColor;

public class ChatMessages {

    public static String HELP() {
        String message = pluginGoldPrefix ();
        message = message.concat(ChatColor.BOLD + "Lista comandi: " + ChatColor.RESET +
                "\n" + ChatColor.GREEN + "  up " + ChatColor.GRAY + "     [ TP sul primo blocco sopra di lui che ha abbastanza spazio libero. ]" +
                "\n" + ChatColor.GREEN + "  down " + ChatColor.GRAY + "   [ TP sul primo blocco sotto di lui che ha abbastanza spazio libero. ]" +
                "\n" + ChatColor.GREEN + "  back " + ChatColor.GRAY + "   [ TP alla posizione precedente. ]" +
                "\n" + ChatColor.GREEN + "  reload " + ChatColor.GRAY + " [ Ricarica il config. ]" +
                "\n" + ChatColor.GREEN + "  help " + ChatColor.GRAY + "   [ Mostra l'elenco dei comandi ]"
        );
        return message;
    }

    public static String pluginGreenPrefix() {
        return  ChatColor.DARK_GREEN + "[FWupdown] " +
                ChatColor.RESET;
    }

    public static String pluginRedPrefix() {
        return  ChatColor.RED + "[FWupdown] " +
                ChatColor.RESET;
    }

    public static String pluginMagicPrefix() {
        return  ChatColor.AQUA + "[FWupdown] " +
                ChatColor.RESET;
    }

    public static String pluginGoldPrefix() {
        return  ChatColor.GOLD + "[FWupdown] " +
                ChatColor.RESET;
    }

    public static String pluginGrayPrefix() {
        return  ChatColor.GRAY + "[FWupdown] " +
                ChatColor.RESET;
    }

    public static String SUCCESS(String message) {
        message = pluginGreenPrefix () + message;
        return message;
    }

    public static String NOSPACE(String message) {
        message = pluginGrayPrefix () + message;
        return message;
    }

    public static String ONLYPLAYER(String message) {
        message = pluginRedPrefix () + message;
        return message;
    }

    public static String NOPERM(String message) {
        message = pluginRedPrefix () + message;
        return message;
    }

    public static String NOVOID(String message) {
        message = pluginMagicPrefix () + message;
        return message;
    }

    public static String NOBACK(String message) {
        message = pluginRedPrefix () + message;
        return message;
    }

    public static String RELOAD(String message) {
        message = pluginGreenPrefix () + message;
        return message;
    }



}
