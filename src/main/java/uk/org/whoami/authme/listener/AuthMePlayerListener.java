/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.whoami.authme.listener;

import com.trc202.CombatTag.CombatTag;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.backup.DataFileCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.plugin.manager.CitizensCommunicator;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.plugin.manager.CombatTagComunicator;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;


public class AuthMePlayerListener implements Listener {

    
    private Utils utils = Utils.getInstance();
    private Messages m = Messages.getInstance();
    private JavaPlugin plugin;
    private DataSource data;
    private FileCache playerBackup = new FileCache();

    public AuthMePlayerListener(JavaPlugin plugin, DataSource data) {
        this.plugin = plugin;
        this.data = data; 
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        
        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player) ) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled || !player.hasPermission("authme.force.register") {
                return;
            }
        }

        String msg = event.getMessage();
        //WorldEdit GUI Shit
        if (msg.equalsIgnoreCase("/worldedit cui")) {
            return;
        }

        String cmd = msg.split(" ")[0];
        if (cmd.equalsIgnoreCase("/login") || cmd.equalsIgnoreCase("/register") || cmd.equalsIgnoreCase("/passpartu") || cmd.equalsIgnoreCase("/l") || cmd.equalsIgnoreCase("/reg")) {
            return;
        }

        event.setMessage("/notloggedin");
        event.setCancelled(true);
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (data.isAuthAvailable(name)) {
            player.sendMessage(m._("login_msg"));
        } else {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
            player.sendMessage(m._("reg_msg"));
        }
        
        if (!Settings.isChatAllowed) {
            //System.out.println("debug chat: chat isnt allowed");
            event.setCancelled(true);
            return;
        }
        //System.out.println("debug chat: chat is allow?");
        
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (data.isAuthAvailable(name)) {
            event.setTo(event.getFrom());
            //event.setCancelled(true);
            return;
        }

        if (!Settings.isForcedRegistrationEnabled) {
            return;
        }

        if (!Settings.isMovementAllowed) {
            event.setTo(event.getFrom());
            //event.setCancelled(true);
            return;
        }

        if (Settings.getMovementRadius == 0) {
            return;
        }

        int radius = Settings.getMovementRadius;
        Location spawn = player.getWorld().getSpawnLocation();
        //Location to = event.getTo();
        
        if ((spawn.distance(player.getLocation()) > radius) ) {
            event.setTo(spawn);
        }
        /* old method
        if (to.getX() > spawn.getX() + radius || to.getX() < spawn.getX() - radius ||
            to.getY() > spawn.getY() + radius || to.getY() < spawn.getY() - radius ||
            to.getZ() > spawn.getZ() + radius || to.getZ() < spawn.getZ() - radius) {
            event.setTo(event.getFrom());
        } */
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
     
        if (event.getResult() != Result.ALLOWED || event.getPlayer() == null) {
            //System.out.println("non permesso?");
            return;
        }

        final Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
       
        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }
        
        //
        // If this check will fail mean that some permissions bypass kick, so player has to be
        // Switched on nonloggedIn group and try another time this kick!!
        //
        if(player.isOnline() && Settings.isForceSingleSessionEnabled ) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, m._("same_nick"));
            return;
        }
        
        if(data.isAuthAvailable(name) && !LimboCache.getInstance().hasLimboPlayer(name)) {
            if(!Settings.isSessionsEnabled) {
            //System.out.println("resetta il nick");  
            LimboCache.getInstance().addLimboPlayer(player , utils.removeAll(player));
            } else if(PlayerCache.getInstance().isAuthenticated(name)) {
                if(LimboCache.getInstance().hasLimboPlayer(player.getName().toLowerCase())) {
                        LimboCache.getInstance().deleteLimboPlayer(name);  
                    } 
                //System.out.println("nick gia autenticato");
                LimboCache.getInstance().addLimboPlayer(player , utils.removeAll(player));
            }
        }
        
        
       
        // Big problem on this chek
        //Check if forceSingleSession is set to true, so kick player that has joined with same nick of online player
        if(player.isOnline() && Settings.isForceSingleSessionEnabled ) {
             LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(player.getName().toLowerCase()); 
             //System.out.println(" limbo ? "+limbo.getGroup());
             event.disallow(PlayerLoginEvent.Result.KICK_OTHER, m._("same_nick"));
                    if(PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
                        utils.addNormal(player, limbo.getGroup());
                        LimboCache.getInstance().deleteLimboPlayer(player.getName().toLowerCase());
                    }            
            return;
               
        }
        

        int min = Settings.getMinNickLength;
        int max = Settings.getMaxNickLength;
        String regex = Settings.getNickRegex;

        if (name.length() > max || name.length() < min) {
            
            //LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name); 
            //utils.addNormal(player, limbo.getGroup());
            //LimboCache.getInstance().deleteLimboPlayer(name);
            event.disallow(Result.KICK_OTHER, "Your nickname is too Short or too long");
            return;
        }
        if (!player.getName().matches(regex) || name.equals("Player")) {
          //LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name); 
            
            event.disallow(Result.KICK_OTHER, "Your nickname contains illegal characters. Allowed chars: " + regex);
           // utils.addNormal(player, limbo.getGroup());
          //  LimboCache.getInstance().deleteLimboPlayer(name);
            return;
        }
 
       /* OLD METHOD 
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            System.out.println("[Debug name 3] "+player.getName());
            if (onlinePlayer.getName().equalsIgnoreCase(player.getName())) {
                System.out.println("[Debug name 2] "+onlinePlayer.getName());
                player.kickPlayer(m._("same_nick"));
                
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, m._("same_nick"));
                return;
            }
        }
       */
        if (Settings.isKickNonRegisteredEnabled) {
            if (!data.isAuthAvailable(name)) {    
                event.disallow(Result.KICK_OTHER, m._("reg_only"));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

       
        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }
        /* 
         * Why it has to return if a player is already authenticated? when should this happen?       
        if (PlayerCache.getInstance().isAuthenticated(name)) {      
            return;
        }
        */
        String ip = player.getAddress().getAddress().getHostAddress();
        //System.out.println("Debug Restricted: "+Settings.isAllowRestrictedIp.toString());
        // kick player that ip doesnt correspond to config.yml list!
            if(Settings.isAllowRestrictedIp && !Settings.getRestrictedIp(name, ip)) {
                player.kickPlayer("You are not the Owner of this account, please try another name!");
                return;           
                }         
        
        if (data.isAuthAvailable(name)) {    
       
            
            if (Settings.isSessionsEnabled) {
                PlayerAuth auth = data.getAuth(name);
                long timeout = Settings.getSessionTimeout * 60000;
                long lastLogin = auth.getLastLogin();
                long cur = new Date().getTime();

            //
            // TODO: rewrite how session work!
            //
             if((cur - lastLogin < timeout || timeout == 0) && !auth.getIp().equals("198.18.0.1") ) {
                if (auth.getNickname().equalsIgnoreCase(name) && auth.getIp().equals(ip) ) {
                  //  System.out.println("[Debug same name] "+auth.getNickname()+ "  "+name);
                  //  System.out.println("[Debug same ip] "+auth.getIp()+ "  "+ip);
                    PlayerCache.getInstance().addPlayer(auth);
                    player.sendMessage(m._("valid_session"));
                    return;
                } else {          
                    player.kickPlayer(m._("unvalid_session"));
                    return;
                }
            } else {
                 // TODO:
                 // Reset player data when session is ended, possible usses is, that player change ip
                 // and session isent ended he is kick out for invalid session, even if he is the rigth
                 // player
                PlayerCache.getInstance().removePlayer(name);
                LimboCache.getInstance().addLimboPlayer(player , utils.removeAll(player));
                //LimboCache.getInstance().addLimboPlayer(player);
                }
          } 
          // isent in session or session was ended correctly
          LimboCache.getInstance().addLimboPlayer(player);
          DataFileCache playerData = new DataFileCache(player.getInventory().getContents(),player.getInventory().getArmorContents());      
          playerBackup.createCache(name, playerData, LimboCache.getInstance().getLimboPlayer(name).getGroup(),LimboCache.getInstance().getLimboPlayer(name).getOperator());                      
        } else {  
            if(!Settings.unRegisteredGroup.isEmpty()){
               utils.setGroup(player, Utils.groupType.UNREGISTERED);
            }
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }



        if(Settings.protectInventoryBeforeLogInEnabled) {
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setContents(new ItemStack[36]);
        }
        
        player.setGameMode(GameMode.SURVIVAL);
        
        if(player.isOp()) 
            player.setOp(false);

        if (Settings.isTeleportToSpawnEnabled || Settings.isForceSpawnLocOnJoinEnabled) {
            player.teleport(player.getWorld().getSpawnLocation());  
        }

        String msg = data.isAuthAvailable(name) ? m._("login_msg") : m._("reg_msg");
        int time = Settings.getRegistrationTimeout * 20;
        int msgInterval = Settings.getWarnMessageInterval;
        BukkitScheduler sched = plugin.getServer().getScheduler();
        if (time != 0) {
            int id = sched.scheduleSyncDelayedTask(plugin, new TimeoutTask(plugin, name), time);
            if(!LimboCache.getInstance().hasLimboPlayer(name))
                 LimboCache.getInstance().addLimboPlayer(player);
            
            LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);
        }
        sched.scheduleSyncDelayedTask(plugin, new MessageTask(plugin, name, msg, msgInterval));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        
        
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
    //
    // Fix for Exact spawn usses that bukkit has
    // Fix for Quit location when player where kicked for timeout
    
    if (PlayerCache.getInstance().isAuthenticated(name) && !player.isDead()) { 
        if(Settings.isSaveQuitLocationEnabled) {
            PlayerAuth auth = new PlayerAuth(event.getPlayer().getName().toLowerCase(),(int)player.getLocation().getX(),(int)player.getLocation().getY(),(int)player.getLocation().getZ());
            data.updateQuitLoc(auth);
        }
    } 
    
        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        
        if (LimboCache.getInstance().hasLimboPlayer(name)) {
            //System.out.println("e' nel quit");
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            if(Settings.protectInventoryBeforeLogInEnabled) {
                player.getInventory().setArmorContents(limbo.getArmour());
                player.getInventory().setContents(limbo.getInventory());
            }
            utils.addNormal(player, limbo.getGroup());
            player.setOp(limbo.getOperator());
            //System.out.println("debug quit group reset "+limbo.getGroup());
            plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
            LimboCache.getInstance().deleteLimboPlayer(name);
            if(playerBackup.doesCacheExist(name)) {
                        playerBackup.removeCache(name);
            }
        }
        PlayerCache.getInstance().removePlayer(name);
        player.saveData();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        
        
        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }
        
        // Check for Minecraft message kick request on same nickname
	// Work only for off-line server
		if (Settings.isForceSingleSessionEnabled) {
			if (event.getReason().equals("You logged in from another location")) {                        	
                            event.setCancelled(true); 
                            return;
                        }
                }
         String name = player.getName().toLowerCase();
        if (PlayerCache.getInstance().isAuthenticated(name) && !player.isDead()) { 
            if(Settings.isSaveQuitLocationEnabled) {       
                PlayerAuth auth = new PlayerAuth(event.getPlayer().getName().toLowerCase(),(int)player.getLocation().getX(),(int)player.getLocation().getY(),(int)player.getLocation().getZ());
                data.updateQuitLoc(auth);
            }
        }              
       
        if (LimboCache.getInstance().hasLimboPlayer(name)) {
            //System.out.println("e' nel kick");
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            if(Settings.protectInventoryBeforeLogInEnabled) {
                player.getInventory().setArmorContents(limbo.getArmour());
                player.getInventory().setContents(limbo.getInventory());
            }
            player.teleport(limbo.getLoc());
            utils.addNormal(player, limbo.getGroup());
            player.setOp(limbo.getOperator());
            //System.out.println("debug quit group reset "+limbo.getGroup());     
            plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
            LimboCache.getInstance().deleteLimboPlayer(name);
             if(playerBackup.doesCacheExist(name)) {
                        playerBackup.removeCache(name);
                    }   
                
        }
        PlayerCache.getInstance().removePlayer(name);
        player.saveData();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        //System.out.println("player try to drop item");
        
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setCancelled(true);
    }

    /* TODO: hook in Multiverse Custom Event for better checking this situation!
     * Avoid the situation where player join the server and he is in the portal, 
     * simple workAround is TeleportUnAtuhToSpawn: true;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }
        
        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }        
        
        if(event.getCause().equals(TeleportCause.PLUGIN) || event.getCause().equals(TeleportCause.NETHER_PORTAL)) {
            return;
        }
        
        event.setCancelled(true);
    }
     * 
     */
}
