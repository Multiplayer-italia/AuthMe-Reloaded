package uk.org.whoami.authme;

import java.security.NoSuchAlgorithmException;
import java.util.Date;


import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class Management {

    private Messages m = Messages.getInstance();
    private Utils utils = Utils.getInstance();
    private FileCache playerCache = new FileCache();
    private DataSource database;
    
    public Management(DataSource database) {
        this.database = database;
    }

	public String performLogin(Player player, String password)
	{
        String name = player.getName().toLowerCase();
        String ip = player.getAddress().getAddress().getHostAddress();
        
        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return m._("logged_in");
           
        }

        if (!database.isAuthAvailable(player.getName().toLowerCase())) {
            return m._("user_unknown");
        }
        
        String hash = database.getAuth(name).getHash();
        try {
            if (PasswordSecurity.comparePasswordWithHash(password, hash)) {
                PlayerAuth auth = new PlayerAuth(name, hash, ip, new Date().getTime());
                database.updateSession(auth);
                PlayerCache.getInstance().addPlayer(auth);
                LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                if (limbo != null) {
                    if(Settings.protectInventoryBeforeLogInEnabled) {
                        player.getInventory().setContents(limbo.getInventory());
                        player.getInventory().setArmorContents(limbo.getArmour());
                    }
                    player.setGameMode(GameMode.getByValue(limbo.getGameMode()));
                    player.setOp(limbo.getOperator());
                    /*                  
                     * if (limbo.getOperator()) {
                         System.out.println("player is an operator after login success");
                    }*/
                    utils.addNormal(player, limbo.getGroup());
                   //System.out.println("il gruppo in logincommand "+limbo.getGroup());
                   //
                   // TODO: completly rewrite this part, too much if, else ...
                   // check quit location, check correct spawn, 
                   //
                    World world = player.getWorld();
                    if (Settings.isTeleportToSpawnEnabled && !Settings.isForceSpawnLocOnJoinEnabled) {                  
                                 // This is initial work around for prevent ppl to quit on bukkit bug
                                 // take last quit location from database and subtract y from safe spawn             
                                 // if the error range is smaller then 1, player can come back in his quit location
                                 // otherwise he try to spawn in a unsafe location!
                                
                                 if(Settings.isSaveQuitLocationEnabled && database.getAuth(name).getQuitLocY() != 0) {
                                     Location quitLoc = new Location(player.getWorld(),(double)database.getAuth(name).getQuitLocX()+0.5,(double)database.getAuth(name).getQuitLocY()+0.5,(double)database.getAuth(name).getQuitLocZ()+0.5);
                                     //pre-load chunk before teleport player to quit location
                                     
                                     if(!world.getChunkAt(quitLoc).isLoaded()) {
                                         //System.out.println("Debug chunk insent loaded");
                                         world.getChunkAt(quitLoc).load();
                                     }
                                     player.teleport(quitLoc);
                                     //System.out.println("quit location from db:"+quitLoc);
                                 } else {
                                    //pre-load chunk before teleport player to quit location
                                    if(!world.getChunkAt(limbo.getLoc()).isLoaded())
                                            world.getChunkAt(limbo.getLoc()).load();                                      
                                 player.teleport(limbo.getLoc());
                                 //System.out.println("quit location from bukkit:"+limbo.getLoc());
                                 } 
                    } else {
                        if(Settings.isForceSpawnLocOnJoinEnabled) {
                            player.teleport(player.getWorld().getSpawnLocation());  
                        } else {
                        if ( Settings.isSaveQuitLocationEnabled && database.getAuth(name).getQuitLocY() != 0) {
                          Location quitLoc = new Location(player.getWorld(),(double)database.getAuth(name).getQuitLocX()+0.5,(double)database.getAuth(name).getQuitLocY()+0.5,(double)database.getAuth(name).getQuitLocZ()+0.5);
                          //pre-load chunk before teleport player to quit location
                              if(!world.getChunkAt(quitLoc).isLoaded()) {
                                  //System.out.println("Debug chunk insent loaded");
                                  world.getChunkAt(quitLoc).load(); 
                              }
                          player.teleport(quitLoc);  
                          //System.out.println("quit location from db:"+quitLoc);
                        } else {
                          //pre-load chunk before teleport player to quit location
                              if(!world.getChunkAt(limbo.getLoc()).isLoaded()) {
                                  //System.out.println("Debug chunk insent loaded");
                                  world.getChunkAt(limbo.getLoc()).load();      
                              }
                                player.teleport(limbo.getLoc());
                                //System.out.println("quit location from bukkit:"+limbo.getLoc()); 
                                }  
                        }
                    } 
                player.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                LimboCache.getInstance().deleteLimboPlayer(name);
                if(playerCache.doesCacheExist(name)) {
                        playerCache.removeCache(name);
                    }   
                }
                
               /*
                *  Little Work Around under Registration Group Switching for admins that
                *  add Registration thru a web Scripts.
                */
                if ( Settings.isPermissionCheckEnabled && AuthMe.permission.playerInGroup(player, Settings.unRegisteredGroup) && !Settings.unRegisteredGroup.isEmpty() ) {
                    AuthMe.permission.playerRemoveGroup(player.getWorld(), player.getName(), Settings.unRegisteredGroup);
                    AuthMe.permission.playerAddGroup(player.getWorld(), player.getName(), Settings.getRegisteredGroup);
                }
                    
                    
                player.sendMessage(m._("login"));
                ConsoleLogger.info(player.getDisplayName() + " logged in!");
                player.saveData();
                
            } else {
                ConsoleLogger.info(player.getDisplayName() + " used the wrong password");
                if (Settings.isKickOnWrongPasswordEnabled) {
                    player.kickPlayer(m._("wrong_pwd"));
                } else {
                    return (m._("wrong_pwd"));
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return (m._("error"));
        }
        return "";
	}
}
