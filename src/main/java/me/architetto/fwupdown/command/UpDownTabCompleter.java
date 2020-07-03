package me.architetto.fwupdown.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpDownTabCompleter implements TabCompleter {

    public List<String> arguments = new ArrayList<String> ();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        final List<String> completions = new ArrayList<>();

        if(arguments.isEmpty ()){
            if(sender.hasPermission ( "fwupdown.allow" )) {
                arguments.add ( "up" );
                arguments.add ( "down" );
                arguments.add ( "back" );
                arguments.add ( "help" );
            }

            if(sender.hasPermission ( "fwupdown.op" ))
            arguments.add("reload");
        }

        StringUtil.copyPartialMatches(args[0], arguments, completions);
        Collections.sort(completions);
        return completions;
    }
}
