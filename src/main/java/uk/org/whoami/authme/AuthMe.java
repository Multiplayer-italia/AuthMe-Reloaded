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

package uk.org.whoami.authme;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.commands.AdminCommand;
import uk.org.whoami.authme.commands.ChangePasswordCommand;
import uk.org.whoami.authme.commands.LoginCommand;
import uk.org.whoami.authme.commands.LogoutCommand;
import uk.org.whoami.authme.commands.RegisterCommand;
import uk.org.whoami.authme.commands.UnregisterCommand;
import uk.org.whoami.authme.datasource.CacheDataSource;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.datasource.FileDataSource;
import uk.org.whoami.authme.datasource.MiniConnectionPoolManager.TimeoutException;
import uk.org.whoami.authme.datasource.MySQLDataSource;
import uk.org.whoami.authme.listener.AuthMeBlockListener;
import uk.org.whoami.authme.listener.AuthMeEntityListener;
import uk.org.whoami.authme.listener.AuthMePlayerListener;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;

public class AuthMe extends JavaPlugin {

    private DataSource database;
    private Settings settings;
    private Utils utils;
    private Messages m;
    public static Server server;
    public static Permission permission;
    

    @Override
    public void onEnable() {
        /*
         *  Metric part from Hidendra Stats
         */
        try {
        Metrics metrics = new Metrics();
        metrics.beginMeasuringPlugin(this);
            } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        
        settings = new Settings(this);
        settings.loadConfigOptions();
        
        m = Messages.getInstance();
        
        server = getServer();
        
        /*
         * Backend MYSQL - FILE - SQLITE
         */
        switch (settings.getDataSource) {
            case FILE:
                try {
                    database = new FileDataSource();
                } catch (IOException ex) {
                    ConsoleLogger.showError(ex.getMessage());
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                break;
            case MYSQL:
                try {
                    database = new MySQLDataSource();
                } catch (ClassNotFoundException ex) {
                    ConsoleLogger.showError(ex.getMessage());
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                } catch (SQLException ex) {
                    ConsoleLogger.showError(ex.getMessage());
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                } catch(TimeoutException ex) {
                    ConsoleLogger.showError(ex.getMessage());
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                break;
        }

        if (settings.isCachingEnabled) {
            database = new CacheDataSource(database);
        }
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new AuthMePlayerListener(this,database),this);
        pm.registerEvents(new AuthMeBlockListener(database),this);
        pm.registerEvents(new AuthMeEntityListener(database),this);
        
        //Find Permissions
        RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
            permission = permissionProvider.getProvider();
        else {
            if(settings.isPermissionCheckEnabled) {
            ConsoleLogger.showError("Vault and Permissions plugins is needed for enable AuthMe Reloaded!");
            this.getServer().getPluginManager().disablePlugin(this);  
            }
        }
        
        //System.out.println("[debug perm]"+permission);
        
        this.getCommand("authme").setExecutor(new AdminCommand(database));
        this.getCommand("register").setExecutor(new RegisterCommand(database));
        this.getCommand("login").setExecutor(new LoginCommand(database));
        this.getCommand("changepassword").setExecutor(new ChangePasswordCommand(database));
        this.getCommand("logout").setExecutor(new LogoutCommand(this,database));
        this.getCommand("unregister").setExecutor(new UnregisterCommand(this, database));
        
        //
        // Check for correct sintax in config file!
        //
        
        if(settings.isForceExactSpawnEnabled && !settings.isSaveQuitLocationEnabled) {
            ConsoleLogger.showError("You need to enable SaveQuitLocation if you want to enable ForceExactSpawn");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        if(!settings.isForceSingleSessionEnabled) {
            ConsoleLogger.info("ATTENTION by disabling ForceSingleSession Your server protection is set to low");
        }
        
        onReload(this.getServer().getOnlinePlayers());
        ConsoleLogger.info("Authme " + this.getDescription().getVersion() + " enabled");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
        //utils = Utils.getInstance();
       
        ConsoleLogger.info("Authme " + this.getDescription().getVersion() + " disabled");
    }

    private void onReload(Player[] players) {
        for (Player player : players) {
            String name = player.getName().toLowerCase();
            String ip = player.getAddress().getAddress().getHostAddress();

            boolean authAvail = database.isAuthAvailable(name);

            if (authAvail) {
                if (settings.isSessionsEnabled) {
                    PlayerAuth auth = database.getAuth(name);
                    if (auth.getNickname().equals(name) && auth.getIp().equals(ip)) {
                        PlayerCache.getInstance().addPlayer(auth);
                        player.sendMessage(m._("valid_session"));
                        break;
                    }
                }
            } else if (!settings.isForcedRegistrationEnabled) {
                break;
            } else if (settings.isKickNonRegisteredEnabled) {
                player.kickPlayer(m._("reg_only"));
                break;
            }

            LimboCache.getInstance().addLimboPlayer(player);
            player.getInventory().setArmorContents(new ItemStack[0]);
            player.getInventory().setContents(new ItemStack[36]);

            if (settings.isTeleportToSpawnEnabled) {
                player.teleport(player.getWorld().getSpawnLocation());
            }

            String msg = authAvail ? m._("login_msg") : m._("reg_msg");
            int time = settings.getRegistrationTimeout * 20;
            int msgInterval = settings.getWarnMessageInterval;
            BukkitScheduler sched = this.getServer().getScheduler();
            if (time != 0) {
                int id = sched.scheduleSyncDelayedTask(this, new TimeoutTask(this, name), time);
                LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id);
            }
            sched.scheduleSyncDelayedTask(this, new MessageTask(this, name, msg, msgInterval));
        }
    }
   
}
