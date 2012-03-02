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

package uk.org.whoami.authme.commands;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class LoginCommand implements CommandExecutor {

    private Messages m = Messages.getInstance();
    //private Settings settings = Settings.getInstance();
    private Utils utils = Utils.getInstance();
    private DataSource database;
    private FileCache playerCache = new FileCache();
    
    public LoginCommand(DataSource database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (!sender.hasPermission("authme." + label.toLowerCase())) {
            sender.sendMessage(m._("no_perm"));
            return true;
        }

        Player player = (Player) sender;
        String name = player.getName().toLowerCase();
        String ip = player.getAddress().getAddress().getHostAddress();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            player.sendMessage(m._("logged_in"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(m._("usage_log"));
            return true;
        }

        if (!database.isAuthAvailable(player.getName().toLowerCase())) {
            player.sendMessage(m._("user_unknown"));
            return true;
        }
        
        PlayerAuth hash = database.getAuth(name);
        try {
            if (PasswordSecurity.comparePasswordWithHash(args[0], hash.getHash())) {
                // Group id From Vbullettin Board for unactivated User
                if(hash.getGroupId() == Settings.getNonActivatedGroup) {
                        player.sendMessage(m._("vb_nonActiv"));
                        return true;                  
                }
                PlayerAuth auth = new PlayerAuth(name, hash.getHash(), ip, new Date().getTime());
                database.updateSession(auth);
                PlayerCache.getInstance().addPlayer(auth);
                LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                if (limbo != null) {
                    player.getInventory().setContents(limbo.getInventory());
                    player.getInventory().setArmorContents(limbo.getArmour());
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
                    if (Settings.isTeleportToSpawnEnabled && !Settings.isForceSpawnLocOnJoinEnabled) {                  
                                 if(Settings.isSaveQuitLocationEnabled && database.getAuth(name).getQuitLocY() != 0 ) {
                                     Location quitLoc = new Location(player.getWorld(),(double)database.getAuth(name).getQuitLocX()+0.5,(double)database.getAuth(name).getQuitLocY()+0.5,(double)database.getAuth(name).getQuitLocZ()+0.5);
                                     player.teleport(quitLoc);
                                     //System.out.println("quit location from db:"+quitLoc);
                                 } else {
                                  //System.out.println("quit location from bukkit:"+limbo.getLoc());
                                 player.teleport(limbo.getLoc());
                                 }
                               
                    } else {
                        if(Settings.isForceSpawnLocOnJoinEnabled) {
                            player.teleport(player.getWorld().getSpawnLocation());  
                        } else {
                        if ( Settings.isSaveQuitLocationEnabled && database.getAuth(name).getQuitLocY() != 0) {
                          Location quitLoc = new Location(player.getWorld(),(double)database.getAuth(name).getQuitLocX()+0.5,(double)database.getAuth(name).getQuitLocY()+0.5,(double)database.getAuth(name).getQuitLocZ()+0.5);
                          player.teleport(quitLoc);  
                          //System.out.println("quit location from db:"+quitLoc);
                        } else {
                                player.teleport(limbo.getLoc());
                                //System.out.println("quit location from bukkit:"+limbo.getLoc()); 
                                }  
                        }
                    } 
                sender.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                LimboCache.getInstance().deleteLimboPlayer(name);
                if(playerCache.doesCacheExist(name)) {
                        playerCache.removeCache(name);
                    }   
                }
                player.sendMessage(m._("login"));
                ConsoleLogger.info(player.getDisplayName() + " logged in!");
               
                
            } else {
                ConsoleLogger.info(player.getDisplayName() + " used the wrong password");
                if (Settings.isKickOnWrongPasswordEnabled) {
                    player.kickPlayer(m._("wrong_pwd"));
                } else {
                    player.sendMessage(m._("wrong_pwd"));
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
            sender.sendMessage(m._("error"));
        }
        return true;
    }
}
