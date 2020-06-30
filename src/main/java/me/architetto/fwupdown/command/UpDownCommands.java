package me.architetto.fwupdown.command;

import me.architetto.fwupdown.Fwupdown;
import me.architetto.fwupdown.utility.ChatMessages;
import me.architetto.fwupdown.utility.Messages;
import org.bukkit.*;

import org.bukkit.block.data.Openable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;


public class UpDownCommands implements CommandExecutor {

    public static HashMap<UUID,Location> LastLocation = new HashMap<UUID, Location> ();

    private final List<Material> gateList= Arrays.asList ( Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE
            , Material.JUNGLE_FENCE_GATE, Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE );

    private double clearspace = Fwupdown.getPlugin ().getConfig ().getDouble ( "Spazio libero minimo" );
    private boolean waterAsAir = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Permetti TP in acqua" );
    private boolean lavaProtection = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Lava protection" );




    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {


        if(!(sender instanceof Player)){
            sender.sendMessage ( ChatMessages.ONLYPLAYER ( Messages.NO_CONSOLE ) );
            return true;
        }
        Player player = (Player) sender;
        Location backlocation = player.getLocation ();


        if(args[0].equalsIgnoreCase ("up"))
        {
            if(!sender.hasPermission("fwupdown.up")){
                sender.sendMessage(ChatMessages.NOPERM ( Messages.NO_PERM ));
                return true;
            }

            Location playerLoc = player.getEyeLocation ();
            //Fix della posizione di partenza per centrare il blocco
            playerLoc.setX (playerLoc.getBlockX ());
            playerLoc.setZ (playerLoc.getBlockZ ());
            playerLoc.setY (playerLoc.getBlockY ());
            playerLoc.add (0.5,1,0.5);

            //Salvo l'altezza massima del  mondo in cui mi trovo
            int MaxHeight = Objects.requireNonNull (playerLoc.getWorld ()).getMaxHeight () + 1;

            //Controllo i blocchi sopra il player
            while(playerLoc.getY() < MaxHeight-clearspace && playerLoc.getBlock ().getType ()!=Material.BEDROCK){
                if(!playerLoc.getBlock ().isPassable ()){
                    while(playerLoc.getY() < MaxHeight-clearspace && playerLoc.getBlock ().getType ()!=Material.BEDROCK){
                        if(playerLoc.getBlock ().isPassable () || playerLoc.getBlock ().getType () == Material.LADDER
                                || Tag.DOORS.getValues ().contains (playerLoc.getBlock().getType ())
                                || checkTrapDoorIsOpen (playerLoc)){

                            if(lavaProtection && playerLoc.getBlock ().getType () == Material.LAVA){
                                preventFallInLava (playerLoc,args[0]);
                                continue;
                            }

                            double TargetPlace = playerLoc.getY ();
                            while(playerLoc.getY ()<TargetPlace+clearspace && playerLoc.getBlock ().getType ()!=Material.BEDROCK){

                                if(!playerLoc.getBlock ().isPassable () && playerLoc.getBlock ().getType () != Material.LADDER
                                        && !Tag.DOORS.getValues ().contains (playerLoc.getBlock().getType()) && !checkTrapDoorIsOpen (playerLoc)){
                                    if(playerLoc.getY ()==TargetPlace && Tag.SLABS.getValues ().contains (playerLoc.getBlock ().getType ())){
                                        playerLoc.add(0,1,0);
                                        continue;
                                    }
                                    break;
                                }

                                if(!waterAsAir && playerLoc.getBlock ().getType()==Material.WATER){
                                    playerLoc.add(0,1,0);
                                    continue;
                                }

                                if(playerLoc.getY ()==TargetPlace+clearspace-1){

                                    playerLoc.setY ( TargetPlace );
                                    Location lastCheck = playerLoc.clone ();

                                    if(Tag.SLABS.getValues ().contains (playerLoc.getBlock().getType())
                                            || Tag.CARPETS.getValues ().contains (playerLoc.getBlock().getType()))
                                        playerLoc.add (0,0.5,0);

                                    if(checkGateANDFance (lastCheck) &&  (playerLoc.getBlock ().isPassable ()
                                            || Tag.CARPETS.getValues ().contains (playerLoc.getBlock().getType())))
                                        playerLoc.add (0, 0.5, 0);

                                    teleportSuccess (playerLoc,player,sender,backlocation);
                                    return true;

                                }else
                                    playerLoc.add (0,1,0);
                            }
                        }else
                            playerLoc.add (0,1,0);
                    }
                }else
                    playerLoc.add (0,1,0);
            }
            sender.sendMessage(ChatMessages.NOSPACE ( Messages.NO_SPACE ));
            return true;
        }

        if(args[0].equalsIgnoreCase ("down") && player.hasPermission ("fwupdown.down")) {

            if(!sender.hasPermission("fwupdown.down")){
                sender.sendMessage(ChatMessages.NOPERM ( Messages.NO_PERM ));
                return true;
            }

            Location playerLoc = player.getEyeLocation ();

            //Fix della posizione di partenza per centrare il blocco
            playerLoc.setX ( playerLoc.getBlockX () );
            playerLoc.setZ ( playerLoc.getBlockZ () );
            playerLoc.setY ( playerLoc.getBlockY () );
            playerLoc.add ( 0.5, -2, 0.5 );


            while(playerLoc.getBlock ().getType ()!=Material.BEDROCK && playerLoc.getY ()>clearspace) {

                //Può usare il comando mentre è in volo
                if(playerLoc.getBlock ().isPassable ()) {
                    while (playerLoc.getBlock ().getType () != Material.BEDROCK) {
                        if (!waterAsAir && playerLoc.getBlock ().getType () == Material.WATER) {
                            playerLoc.add (0, 1, 0);
                            teleportSuccess (playerLoc, player, sender, backlocation);
                            player.setFallDistance (1);
                            return true;
                        }

                        if (!playerLoc.getBlock ().isPassable ()) {
                            playerLoc.add (0, 1, 0);
                            teleportSuccess (playerLoc, player, sender, backlocation);
                            player.setFallDistance ( 1 );
                            return true;
                        } else
                            playerLoc.add (0, -1, 0);

                        if (playerLoc.getBlock ().getType () == Material.VOID_AIR) {
                            sender.sendMessage(ChatMessages.NOVOID ( Messages.NO_VOID ));
                            return true;
                        }

                        if (playerLoc.getBlock ().getType () == Material.BEDROCK) {
                            playerLoc.add (0, 1, 0);
                            teleportSuccess (playerLoc, player, sender, backlocation);
                            player.setFallDistance (1);
                            return true;
                        }
                    }
                }

                if (!playerLoc.getBlock ().isPassable ()) {
                    while(playerLoc.getBlock ().getType ()!=Material.BEDROCK && playerLoc.getY ()>clearspace){
                        //ricerca del primo blocco di "aria"
                        if(playerLoc.getBlock ().isPassable () || playerLoc.getBlock ().getType () == Material.LADDER
                                || Tag.DOORS.getValues ().contains (playerLoc.getBlock().getType())
                                || checkTrapDoorIsOpen (playerLoc)){
                            int checked = 1;
                            while(playerLoc.getBlock ().isPassable () || playerLoc.getBlock ().getType () == Material.LADDER
                                    || Tag.DOORS.getValues ().contains (playerLoc.getBlock().getType())
                                    || checkTrapDoorIsOpen (playerLoc)){
                                playerLoc.add (0,-1,0);

                                if(playerLoc.getBlock ().getType ()==Material.WATER && !waterAsAir){
                                    if(checked >=clearspace ) {
                                        playerLoc.add (0, 1, 0);
                                        teleportSuccess (playerLoc, player, sender, backlocation);
                                        return true;
                                    }else {
                                        playerLoc.add ( 0, -1, 0 );
                                        continue;
                                    }
                                }

                                if(!playerLoc.getBlock ().isPassable ()){
                                    if(playerLoc.getBlock ().getType () == Material.LADDER
                                            || Tag.DOORS.getValues ().contains (playerLoc.getBlock().getType())
                                            || checkTrapDoorIsOpen (playerLoc)){
                                        checked++;
                                        continue;
                                    }

                                    if(checked >=clearspace ) {
                                        if(lavaProtection && touchLava (playerLoc)){
                                            break; //Evita il tp nella lava se il lavaprotection è attivo (config)
                                        }
                                        playerLoc.add ( 0, 1, 0 );
                                        teleportSuccess (playerLoc, player, sender, backlocation);
                                        return true;
                                    }
                                    break;
                                }
                                checked++;
                            }
                        }else
                            playerLoc.add(0,-1,0);
                    }
                    sender.sendMessage(ChatMessages.NOSPACE ( Messages.NO_SPACE ));
                    return true;
                }
                return true;
            }
            if(playerLoc.getBlock ().getType ()==Material.BEDROCK && playerLoc.getY ()>clearspace) {
                sender.sendMessage(ChatMessages.NOSPACE ( Messages.NO_SPACE ));
                return true;
            }

            return true;
        }

        if(args[0].equalsIgnoreCase ( "back" )){
            if(!sender.hasPermission("fwupdown.back")){
                sender.sendMessage(ChatMessages.NOPERM ( Messages.NO_PERM ));
                return true;
            }
            if(LastLocation.containsKey ( player.getUniqueId () )){
                teleportSuccess (LastLocation.get ( player.getUniqueId () ), player, sender);
                return true;
            }else
                sender.sendMessage(ChatMessages.NOBACK ( Messages.NO_BACK ));
            return true;

        }

        if(args[0].equalsIgnoreCase ( "help" )){
            sender.sendMessage (ChatMessages.HELP ());
            return true;
        }

        if(args[0].equalsIgnoreCase ( "reload" )){
            if(!sender.hasPermission ( "fwupdown.reload" )){
                sender.sendMessage( ChatMessages.NOPERM ( Messages.NO_PERM ));
                return true;
            }
            //Potrebbe non essere il metodo migliore...
            Fwupdown.getPlugin ().reloadConfig ();
            clearspace = Fwupdown.getPlugin ().getConfig ().getDouble ( "Spazio libero minimo" );
            waterAsAir = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Permetti TP in acqua" );
            lavaProtection = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Lava protection" );
            sender.sendMessage ( ChatMessages.RELOAD ( Messages.CONFIG_RELOAD ) );
            return true;
        }



        return false;
    }



    //-------------------------------------------------------------

    private boolean checkGateANDFance(Location checkBlock){
        boolean flag=false;
        checkBlock.setY ( checkBlock.getY ()-1);
        if(gateList.contains (checkBlock.getBlock ().getType ())
                || Tag.FENCES.getValues ().contains ( checkBlock.getBlock ().getType () )) {
            flag = true;
        }
        return flag;
    }

    private boolean checkTrapDoorIsOpen(Location checkBlock){
        boolean flag=false;
        if(Tag.TRAPDOORS.getValues ().contains (checkBlock.getBlock ().getType ())){
            Openable trapdoor = (Openable) checkBlock.getBlock ().getBlockData ();
            if(trapdoor.isOpen ())
                flag = true;
        }
        return flag;
    }

    private void preventFallInLava(Location checkBlock,String direction){
        while(checkBlock.getBlock ().isPassable ()){

            if(direction.equalsIgnoreCase ( "up" ))
                checkBlock.add ( 0,1,0 );
            if(direction.equalsIgnoreCase ( "down" ))
                checkBlock.add ( 0,-1,0 );

            if(checkBlock.getY ()==Objects.requireNonNull ( checkBlock.getWorld () ).getMaxHeight ()){
                checkBlock.setY ( Objects.requireNonNull ( checkBlock.getWorld () ).getMaxHeight () );
                break;
            }
            if(checkBlock.getBlock ().getType ()==Material.BEDROCK){
                break;
            }
        }
    }

    private boolean touchLava(Location checkBlock){
        Location clone = checkBlock.clone ();
        boolean lava=false;
        if(clone.getBlock ().getType ()==Material.LAVA)
            lava=true;
        clone.add(0,1,0);
        if(clone.getBlock ().getType ()==Material.LAVA)
            lava=true;
        return lava;
    }

    private void saveLastLocation(Player player, Location location){
        LastLocation.put ( player.getUniqueId (),location );

    }

    public void teleportSuccess (Location playerLoc, Player player, CommandSender sender, Location backlocation){

        player.playSound (player.getLocation (), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 1);
        player.teleport (playerLoc, PlayerTeleportEvent.TeleportCause.PLUGIN );
        sender.sendMessage(ChatMessages.SUCCESS ( Messages.TPSUCCESS ));
        if (player.isFlying ()) player.setFlying (false);
        saveLastLocation (player,backlocation);
    }

    //Viene usato se utilizzato il comando /back -> ha il compito di eliminare l'ultima posizione dall'hashmap
    public void teleportSuccess (Location playerLoc, Player player, CommandSender sender){

        player.playSound (player.getLocation (), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 1);
        player.teleport (playerLoc, PlayerTeleportEvent.TeleportCause.PLUGIN );
        sender.sendMessage(ChatMessages.SUCCESS ( Messages.TPSUCCESS ));
        if (player.isFlying ()) player.setFlying (false);
        LastLocation.remove ( player.getUniqueId () );
    }


}
