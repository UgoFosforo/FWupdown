package me.architetto.fwupdown.command;

import me.architetto.fwupdown.Fwupdown;
import me.architetto.fwupdown.utility.ChatMessages;
import me.architetto.fwupdown.utility.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.data.Openable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class UpDownCommands implements CommandExecutor {

    public static HashMap<UUID, Location> LastLocation = new HashMap<UUID, Location> ();

    //A quanto pare non esiste un Tag.GATE
    public final List<Material> gateList= Arrays.asList ( Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE
            , Material.JUNGLE_FENCE_GATE, Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE );

    public double minClearSpace = Fwupdown.getPlugin ().getConfig ().getDouble ( "Spazio libero minimo" );
    public boolean noTPinWater = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Permetti TP in acqua" );
    public boolean lavaProtection = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Lava protection" );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)){
            sender.sendMessage ( ChatMessages.ONLYPLAYER ( Messages.NO_CONSOLE ) );
            return true;
        }

        Player player = (Player) sender;
        Location backLocation = player.getLocation ();

        if(args[0].equalsIgnoreCase ( "up" )){
            if(!sender.hasPermission ( "fwupdown.up" )){
                sender.sendMessage ( ChatMessages.ONLYPLAYER ( Messages.NO_PERM ) );
                return true;
            }

            Location playerLoc = player.getEyeLocation ();
            //Fix della posizione di partenza per centrare il blocco
            fixPosition(playerLoc,args[0]);

            while(playerLoc.getY ()<256 - minClearSpace || playerLoc.getBlock ().getType () == Material.BEDROCK){

                //Punta al primo blocco solido che trova Su o Giu (dipende dal comando)
                pointNextNOTPassableBlock ( playerLoc, args[0] );

                System.out.println ("pointNextNOTPassableBlock ---------> " + playerLoc.getY () + "//" + playerLoc.getBlock ().getType ());

                //Punta al primo blocco trapassabile che trova Su o Giu (dipende dal comando)
                pointNextPassableBlock(playerLoc,args[0]);

                System.out.println ("pointNextPassableBlock ---------> " + playerLoc.getY () + "//" + playerLoc.getBlock ().getType ());

                if(playerLoc.getY ()==400 )
                    break;

                Location targetLoc = playerLoc.clone ();

                //Check del punto targettato (Se possiede le giuste caratteristiche esegue il teleport)
                if (isGoodSpot ( playerLoc,args[0] )) {
                    //Perfezionamento delle coordinate del punto di teleport
                    fixFinalTpLocation (targetLoc);
                    teleportSuccess(targetLoc,player,sender,backLocation);
                    return true;
                }

            }

            //Se non trova nulla restituisce il messaggio NO_SPACE
            sender.sendMessage(ChatMessages.NOSPACE ( Messages.NO_SPACE ));
            return true;

        }

        if(args[0].equalsIgnoreCase ( "down" )) {
            if (!sender.hasPermission ( "fwupdown.down" )) {
                sender.sendMessage ( ChatMessages.ONLYPLAYER ( Messages.NO_PERM ) );
                return true;
            }

            Location playerLoc = player.getEyeLocation ();

            fixPosition(playerLoc,args[0]);

            while(playerLoc.getY ()<256 - minClearSpace || playerLoc.getBlock ().getType () == Material.BEDROCK){


                pointNextPassableBlock(playerLoc,args[0]);

                System.out.println ("pointNextPassableBlock VALORE TROVATO---------> " + playerLoc.getY () + "//" + playerLoc.getBlock ().getType ());

                pointNextNOTPassableBlock ( playerLoc, args[0] );

                System.out.println ("pointNextNOTPassableBlock VALORE TROVATO---------> " + playerLoc.getY () + "//" + playerLoc.getBlock ().getType ());


                pointNextPassableBlock(playerLoc,"up");

                System.out.println ("pointNextPassableBlock VALORE TROVATO---------> " + playerLoc.getY () + "//" + playerLoc.getBlock ().getType ());

                if(playerLoc.getY ()==400 || playerLoc.getY ()<0)
                    break;

                Location targetLoc = playerLoc.clone ();

                if (isGoodSpot ( playerLoc,args[0] )) {


                    if(targetLoc.getY ()==player.getLocation ().getBlockY ()){
                        sender.sendMessage(ChatMessages.NOSPACE ( Messages.NO_SPACE ));
                        return true;
                    }

                    fixFinalTpLocation (targetLoc);
                    teleportSuccess(targetLoc,player,sender,backLocation);
                    return true;

                }else {
                    playerLoc = targetLoc.clone ();
                    playerLoc.add ( 0,-1,0 );
                }

            }

            sender.sendMessage(ChatMessages.NOSPACE ( Messages.NO_SPACE ));
            return true;

        }


        if(args[0].equalsIgnoreCase ( "back" )){

            if(!sender.hasPermission("fwupdown.back")){
                sender.sendMessage(ChatMessages.NOPERM ( Messages.NO_PERM ));
                return true;
            }

            //Controlla se c'è una posizione salvata, nel caso positivo esegue il teleport
            if(LastLocation.containsKey ( player.getUniqueId () )){
                teleportBack (LastLocation.get ( player.getUniqueId () ), player, sender);
                return true;
            }else
                sender.sendMessage(ChatMessages.NOBACK ( Messages.NO_BACK ));
            return true;
        }

        //Restituisce la lista di comandi
        if(args[0].equalsIgnoreCase ( "help" )){
            sender.sendMessage (ChatMessages.HELP ());
            return true;
        }


        //Refresh delle variabili dal file config
        if(args[0].equalsIgnoreCase ( "reload" )){
            if(!sender.hasPermission ( "fwupdown.reload" )){
                sender.sendMessage( ChatMessages.NOPERM ( Messages.NO_PERM ));
                return true;
            }
            //Potrebbe non essere il metodo migliore...
            Fwupdown.getPlugin ().reloadConfig ();
            minClearSpace = Fwupdown.getPlugin ().getConfig ().getDouble ( "Spazio libero minimo" );
            noTPinWater = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Permetti TP in acqua" );
            lavaProtection = Fwupdown.getPlugin ().getConfig ().getBoolean ( "Lava protection" );
            sender.sendMessage ( ChatMessages.RELOAD ( Messages.CONFIG_RELOAD ) );
            return true;
        }

        return false;
    }

    //-----------------------------------------------------------------//

    public void fixPosition(Location playerLoc,String upORdown){

        playerLoc.setX (playerLoc.getBlockX ());
        playerLoc.setZ (playerLoc.getBlockZ ());
        playerLoc.setY (playerLoc.getBlockY ());

        //Punta al centro della superficie del blocco
        if(upORdown.equalsIgnoreCase ( "up" ))
            playerLoc.add (0.5,0,0.5);
        if(upORdown.equalsIgnoreCase ( "down" ))
            playerLoc.add ( 0.5, -2, 0.5 );

    }

    //-----------------------------------------------------------------//

    private void pointNextNOTPassableBlock(Location playerLoc, String upORdown){

        while( playerLoc.getY ()<=255 - minClearSpace+1 && playerLoc.getY ()>0 && playerLoc.getBlock ().getType () != Material.BEDROCK){
            if(playerLoc.getBlock ().isPassable () || playerLoc.getBlock ().getType () == Material.LADDER
                    || Tag.DOORS.getValues ().contains (playerLoc.getBlock().getType ())
                    || isTrapOpen (playerLoc)) {

                if(playerLoc.getY ()>255-minClearSpace || (playerLoc.getBlock ().getType ()==Material.BEDROCK  && playerLoc.getY ()<0)) {
                    playerLoc.setY ( 400 );
                    return;
                }

                if(upORdown.equalsIgnoreCase ( "up" ))
                    playerLoc.add ( 0, 1, 0 );

                if(upORdown.equalsIgnoreCase ( "down" ))
                    playerLoc.add ( 0, -1, 0 );


                System.out.println ("pointNextNOTPassableBlock -----nelWhile----> " + playerLoc.getY () +"//" + playerLoc.getBlock ().getType ());
            }
            else {

                break;
            }
        }

        if(playerLoc.getBlock ().getType ()==Material.BEDROCK && playerLoc.getY () > 10)
            playerLoc.setY ( 400 );

    }

    private void pointNextPassableBlock(Location playerLoc,String upORdown){

        while(playerLoc.getY ()<=255 && playerLoc.getY () >= 0 && playerLoc.getBlock ().getType () != Material.BEDROCK){
            if(!playerLoc.getBlock ().isPassable () && playerLoc.getBlock ().getType () != Material.LADDER
                    && !isTrapOpen (playerLoc) && !Tag.CARPETS.getValues ().contains ( playerLoc.getBlock ().getType () )
                    && !isBottomSlab ( playerLoc ) && !isBottomTrap ( playerLoc )){

                if(playerLoc.getY ()>255-minClearSpace+1 || playerLoc.getY ()<0 || playerLoc.getBlock ().getType ()==Material.BEDROCK) {
                    playerLoc.setY ( 400 );
                    return;
                }

                if(upORdown.equalsIgnoreCase ( "up" ))
                    playerLoc.add ( 0, 1, 0 );

                if(upORdown.equalsIgnoreCase ( "down" ))
                    playerLoc.add ( 0, -1, 0 );



                System.out.println ("pointNextPassableBlock ----NEL WHILE newxPASSABLE-----> " + playerLoc.getY () + "///" + playerLoc.getBlock ().getType () );

            }

            else {
                break;
            }
        }

        if(playerLoc.getBlock ().getType ()==Material.BEDROCK && playerLoc.getY () >= 0) {
            playerLoc.add ( 0, 1, 0 );
            if(!playerLoc.getBlock ().isPassable () && upORdown.equalsIgnoreCase ( "down" ))
                playerLoc.setY ( 400 );
        }


    }

    //-----------------------------------------------------------------//


    private boolean isGoodSpot(Location playerLoc,String upORdown){

        boolean flag = false;
        boolean firstIteration = true;
        double checkedBlock = 0;

        Location checkThis = playerLoc.clone ();
        checkThis.add ( 0,-1,0 );

       //Nei seguenti aggiunge 0.5 al conteggio dello spazio libero
        if(isBottomTrap ( playerLoc )
                || isBottomSlab ( playerLoc )
                || isBottomTrap ( playerLoc )
                || Tag.WALLS.getValues ().contains ( checkThis.getBlock ().getType () )
                || Tag.FENCES.getValues ().contains ( checkThis.getBlock ().getType () )
                || gateList.contains ( checkThis.getBlock ().getType () )) {
            checkedBlock = 0.5;
            firstIteration=false;
            playerLoc.add ( 0,1,0 );
        }

        while(checkedBlock<=minClearSpace+1 && playerLoc.getY ()<=255 && playerLoc.getY ()>0 && playerLoc.getBlock ().getType ()!=Material.BEDROCK){

            //Avoid loop stuck
            if(playerLoc.getBlock ().getType ()==Material.VOID_AIR || playerLoc.getBlock ().getType ()==Material.BEDROCK){
                playerLoc.setY ( 400 );
                break;
            }

            //Aumenta il conteggio dello spazio libero se è vera una delle seguenti.
            if(playerLoc.getBlock ().isPassable ()
                    || playerLoc.getBlock ().getType () == Material.LADDER
                    || Tag.DOORS.getValues ().contains (playerLoc.getBlock().getType ())
                    || isTrapOpen (playerLoc)
                    || isTopSlab ( playerLoc )
                    || Tag.CARPETS.getValues ().contains ( playerLoc.getBlock ().getType () )){

                // Difficile da spiegare.
                if(!firstIteration && Tag.CARPETS.getValues ().contains ( playerLoc.getBlock ().getType () )){
                    if(checkedBlock>=minClearSpace+1)
                        flag=true;
                    break;
                }

                // NO TP IN ACQUA
                if (!noTPinWater && playerLoc.getBlock ().getType () == Material.WATER) {
                    while (playerLoc.getBlock ().getType () == Material.WATER) {
                        playerLoc.add ( 0, 1, 0 );
                    }
                    checkedBlock=0;
                    continue;
                }

                // NO TP NELLA LAVA O SULLA LAVA
                if (lavaProtection && playerLoc.getBlock ().getType () == Material.LAVA) {
                    while (playerLoc.getBlock ().getType () == Material.LAVA) {

                        if (upORdown.equalsIgnoreCase ( "up" ))
                            playerLoc.add ( 0, 1, 0 );

                        if (upORdown.equalsIgnoreCase ( "down" ))
                            playerLoc.add ( 0, -1, 0 );

                    }
                    break;
                }

                //Se trova uno slab nello stato top aggiunge 0.5 allo spazio conteggiato e verifica se ha ragginto
                //lo spazio minimo.. altrimenti esce e si va a cercare la prossima posizione papabile
                if(isTopSlab ( playerLoc ) || isTopTrap ( playerLoc )){
                    checkedBlock=checkedBlock+0.5;
                    if(checkedBlock>=minClearSpace){
                        flag=true;
                        break;
                    }
                    playerLoc.add ( 0,1,0 );
                    break;
                }


               // if(checkedBlock == minClearSpace || checkedBlock > minClearSpace ){
                 //   flag=true;
                   // break;
                 //}

                //Il blocco ha superato le verifiche supplementari
                checkedBlock++;
                firstIteration=false;

                //Se ha raggiunto lo spazio minimo necessario si procede al tp...
                if(checkedBlock == minClearSpace || checkedBlock > minClearSpace ){
                    flag=true;
                    break;
                }
                //...altrimenti punta al prossimo blocco e ripete il ciclo.
                playerLoc.add ( 0,1,0 );
            }else{
                //se trova un blocco non trapassabile controlla direttamente se ha raggiunto lo spazio minimo necessario
                if(checkedBlock == minClearSpace || checkedBlock > minClearSpace )
                    flag=true;
                break;
            }

        }

        return flag;
    }



    //-----------------------------------------------------------------//
    //Il nome del metodo spiega quello che fa

    private boolean isTrapOpen(Location checkBlock){

        boolean flag=false;

        if(Tag.TRAPDOORS.getValues ().contains (checkBlock.getBlock ().getType ())){
            Openable trapdoor = (Openable) checkBlock.getBlock ().getBlockData ();
            if(trapdoor.isOpen ())
                flag = true;
        }

        return flag;
    }

    private boolean isBottomSlab(Location playerLoc){
        boolean flag = false;

        if( Tag.SLABS.getValues ().contains ( playerLoc.getBlock ().getType () )) {
            if (playerLoc.getBlock ().getBlockData ().getAsString ().toLowerCase ().contains ( "bottom" ))
                flag = true;
        }


        return  flag;
    }


    private boolean isBottomTrap(Location playerLoc){
        boolean flag = false;

        if ( Tag.TRAPDOORS.getValues ().contains ( playerLoc.getBlock ().getType () ) && !isTrapOpen ( playerLoc ) ){
            if (playerLoc.getBlock ().getBlockData ().getAsString ().toLowerCase ().contains ( "bottom" ))
                flag = true;
        }

        return  flag;
    }


    private boolean isTopSlab(Location playerLoc){
        boolean flag = false;

        if( Tag.SLABS.getValues ().contains ( playerLoc.getBlock ().getType () )) {
            if (playerLoc.getBlock ().getBlockData ().getAsString ().toLowerCase ().contains ( "top" ))
                flag = true;
        }

        return  flag;
    }


    private boolean isTopTrap(Location playerLoc){
        boolean flag = false;


        if ( Tag.TRAPDOORS.getValues ().contains ( playerLoc.getBlock ().getType () ) && !isTrapOpen ( playerLoc ) ){
            if (playerLoc.getBlock ().getBlockData ().getAsString ().toLowerCase ().contains ( "top" ))
                flag = true;
        }

        return  flag;
    }

    //-----------------------------------------------------------------//

    private void fixFinalTpLocation(Location playerloc){

        Location checkThis = playerloc.clone ();
        //aggiusta le coordinate del tp per poggiarlo perfettamente.

        if(isBottomSlab ( checkThis )
                || checkThis.getBlock ().getType ()==Material.DAYLIGHT_DETECTOR
                || isBottomTrap ( checkThis )) {
            playerloc.add ( 0, 0.5, 0 );
            return;
        }

        if(Tag.CARPETS.getValues ().contains ( checkThis.getBlock ().getType () ))
            playerloc.add ( 0, 0.1, 0 );

        checkThis.add ( 0,-1,0 );

        if(Tag.FENCES.getValues ().contains ( checkThis.getBlock ().getType () )
                || Tag.WALLS.getValues ().contains ( checkThis.getBlock ().getType () )
                || gateList.contains ( checkThis.getBlock ().getType ())) {
            playerloc.add ( 0, 0.4, 0 );
        }

    }

    public void teleportSuccess (Location playerLoc, Player player, CommandSender sender, Location backlocation){

        //Esegue il tp e salva la posizione di partenza per un eventuale comando back

        player.playSound (player.getLocation (), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 1);
        player.teleport (playerLoc, PlayerTeleportEvent.TeleportCause.PLUGIN );
        sender.sendMessage(ChatMessages.SUCCESS ( Messages.TPSUCCESS ));
        if (player.isFlying ()) player.setFlying (false);
        saveLastLocation (player,backlocation);
    }

    private void saveLastLocation(Player player, Location location){

        //Salva la posizione di partenza del player nell'HashMap per un eventuale comando back
        LastLocation.put ( player.getUniqueId (),location );

    }

    public void teleportBack (Location playerLoc, Player player, CommandSender sender){

        //Esegue il tp back ed elimina la posizione del player dall'HashMap
        player.playSound (player.getLocation (), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 1);
        player.teleport (playerLoc, PlayerTeleportEvent.TeleportCause.PLUGIN );
        sender.sendMessage(ChatMessages.SUCCESS ( Messages.TPSUCCESS ));
        if (player.isFlying ()) player.setFlying (false);
        LastLocation.remove ( player.getUniqueId () );
    }


    //---------------------------------------------------------------------------------//


}
