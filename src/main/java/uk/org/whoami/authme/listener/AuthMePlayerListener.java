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
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.citizens.CitizensCommunicator;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;


public class AuthMePlayerListener extends PlayerListener {

    private Settings settings = Settings.getInstance();
    private Messages m = Messages.getInstance();
    private JavaPlugin plugin;
    private DataSource data;

    public AuthMePlayerListener(JavaPlugin plugin, DataSource data) {
        this.plugin = plugin;
        this.data = data;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        
        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }

        String msg = event.getMessage();
        //WorldEdit GUI Shit
        if (msg.equalsIgnoreCase("/worldedit cui")) {
            return;
        }

        String cmd = msg.split(" ")[0];
        if (cmd.equalsIgnoreCase("/login") || cmd.equalsIgnoreCase("/register")) {
            return;
        }

        event.setMessage("/notloggedin");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (data.isAuthAvailable(name)) {
            player.sendMessage(m._("login_msg"));
        } else {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
            if (settings.isChatAllowed()) {
                return;
            }
            player.sendMessage(m._("reg_msg"));
        }
        if (!settings.isChatAllowed()) {
            System.out.println("debug chat: chat isnt allowed");
            event.setCancelled(true);
            return;
        }
        System.out.println("debug chat: chat is allow?");
        
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (data.isAuthAvailable(name)) {
            event.setTo(event.getFrom());
            return;
        }

        if (!settings.isForcedRegistrationEnabled()) {
            return;
        }

        if (!settings.isMovementAllowed()) {
            event.setTo(event.getFrom());
            return;
        }

        if (settings.getMovementRadius() == 0) {
            return;
        }

        int radius = settings.getMovementRadius();
        Location spawn = player.getWorld().getSpawnLocation();
        Location to = event.getTo();

        if (to.getX() > spawn.getX() + radius || to.getX() < spawn.getX() - radius ||
            to.getY() > spawn.getY() + radius || to.getY() < spawn.getY() - radius ||
            to.getZ() > spawn.getZ() + radius || to.getZ() < spawn.getZ() - radius) {
            event.setTo(event.getFrom());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        
        if (event.getResult() != Result.ALLOWED || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        int min = settings.getMinNickLength();
        int max = settings.getMaxNickLength();
        String regex = settings.getNickRegex();

        if (name.length() > max || name.length() < min) {
            event.disallow(Result.KICK_OTHER, "Your nickname has the wrong length. MaxLen: " + max + ", MinLen: " + min);
            return;
        }
        if (!player.getName().matches(regex) || name.equals("Player")) {
            event.disallow(Result.KICK_OTHER, "Your nickname contains illegal characters. Allowed chars: " + regex);
            return;
        }
        // Big problem on this chek
        //Check if forceSingleSession is set to true, so kick player that has joined with same nick of online player
        if(player.isOnline() && settings.isForceSingleSessionEnabled()) {
               event.disallow(PlayerLoginEvent.Result.KICK_OTHER, m._("same_nick")); 
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
        if (settings.isKickNonRegisteredEnabled()) {
            if (!data.isAuthAvailable(name)) {
                event.disallow(Result.KICK_OTHER, m._("reg_only"));
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        String ip = player.getAddress().getAddress().getHostAddress();
       
       
       if(settings.isAllowRestrictedIp() && !settings.getRestrictedIp(name, ip)) {
            player.kickPlayer( "You are not the Owner of this account, please try another name!");
            return;           
       }
       
        if (CitizensCommunicator.isNPC(player)) {
            return;
        }
        
        if (PlayerCache.getInstance().isAuthenticated(name)) {      
            return;
        }

        if (data.isAuthAvailable(name)) {
            if (settings.isSessionsEnabled()) {
                PlayerAuth auth = data.getAuth(name);
                long timeout = settings.getSessionTimeout() * 60000;
                long lastLogin = auth.getLastLogin();
                long cur = new Date().getTime();
            //
            // TODO: rewrite how session work!
            //
             if((cur - lastLogin < timeout || timeout == 0) && !auth.getIp().equals("198.18.0.1") ) {
                if (auth.getNickname().equals(name) && auth.getIp().equals(ip) ) {
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
            }
          }
        } else {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }

        LimboCache.getInstance().addLimboPlayer(player);
        player.getInventory().setArmorContents(new ItemStack[0]);
        player.getInventory().setContents(new ItemStack[36]);
        player.setGameMode(GameMode.SURVIVAL);
        if (settings.isTeleportToSpawnEnabled() || settings.isForceSpawnLocOnJoinEnabled()) {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        String msg = data.isAuthAvailable(name) ? m._("login_msg") : m._("reg_msg");
        int time = settings.getRegistrationTimeout() * 20;
        int msgInterval = settings.getWarnMessageInterval();
        BukkitScheduler sched = plugin.getServer().getScheduler();
        if (time != 0) {
            int id = sched.scheduleSyncDelayedTask(plugin, new TimeoutTask(plugin, name), time);
            LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);
        }
        sched.scheduleSyncDelayedTask(plugin, new MessageTask(plugin, name, msg, msgInterval));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        
        
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
    //
    // Fix for Exact spawn usses that bukkit has
    // Fix for Quit location when player where kicked for timeout
    
    if (PlayerCache.getInstance().isAuthenticated(name) ) {  
        if(settings.isForceExactSpawnEnabled() || settings.isSaveQuitLocationEnabled() ) {
            PlayerAuth auth = new PlayerAuth(event.getPlayer().getName().toLowerCase(),(int)player.getLocation().getX(),(int)player.getLocation().getY(),(int)player.getLocation().getZ());
            data.updateQuitLoc(auth);
        }
    }
    
        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        
        if (LimboCache.getInstance().hasLimboPlayer(name)) {
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            player.getInventory().setArmorContents(limbo.getArmour());
            player.getInventory().setContents(limbo.getInventory());
            plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
            LimboCache.getInstance().deleteLimboPlayer(name);
        }
        PlayerCache.getInstance().removePlayer(name);
    }

     @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        
        
        if (CitizensCommunicator.isNPC(player)) {
            return;
        }
        
        // Check for Minecraft message kick request on same nickname
	// Work only for off-line server
		if (settings.isForceSingleSessionEnabled()) {
			if (event.getReason().equals("You logged in from another location")) {                        	
                            event.setCancelled(true); 
                            return;
                        }
                }
             
        String name = player.getName().toLowerCase();
        if (LimboCache.getInstance().hasLimboPlayer(name)) {
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            player.getInventory().setArmorContents(limbo.getArmour());
            player.getInventory().setContents(limbo.getInventory());
            player.teleport(limbo.getLoc());
            plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
            LimboCache.getInstance().deleteLimboPlayer(name);
        }
        PlayerCache.getInstance().removePlayer(name);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }

        event.setCancelled(true);
    }

     @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (CitizensCommunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!settings.isForcedRegistrationEnabled()) {
                return;
            }
        }
        event.setCancelled(true);
    }
    
}
