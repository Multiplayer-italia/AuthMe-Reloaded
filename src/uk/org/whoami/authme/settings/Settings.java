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

package uk.org.whoami.authme.settings;

import java.io.File;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.datasource.DataSource.DataSourceType;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.security.PasswordSecurity.HashAlgorithm;

public final class Settings extends YamlConfiguration {

    public static final String PLUGIN_FOLDER = "./plugins/AuthMe";
    public static final String CACHE_FOLDER = Settings.PLUGIN_FOLDER + "/cache";
    public static final String AUTH_FILE = Settings.PLUGIN_FOLDER + "/auths.db";
    public static final String MESSAGE_FILE = Settings.PLUGIN_FOLDER + "/messages";
    public static final String SETTINGS_FILE = Settings.PLUGIN_FOLDER + "/config.yml";
    public static List<String> getJoinPermissions = null;
    public static List<String> getUnrestrictedName = null;
    private static List<String> getRestrictedIp;
   
    public final Plugin plugin;
    private final File file;    
    
    public static DataSourceType getDataSource;
    public static HashAlgorithm getPasswordHash;
    
    public static Boolean isPermissionCheckEnabled, isRegistrationEnabled, isForcedRegistrationEnabled,
            isTeleportToSpawnEnabled, isSessionsEnabled, isChatAllowed, isAllowRestrictedIp, 
            isMovementAllowed, isKickNonRegisteredEnabled, isForceSingleSessionEnabled,
            isForceSpawnLocOnJoinEnabled, isForceExactSpawnEnabled, isSaveQuitLocationEnabled,
            isForceSurvivalModeEnabled, isResetInventoryIfCreative, isCachingEnabled, isKickOnWrongPasswordEnabled,
            getEnablePasswordVerifier, protectInventoryBeforeLogInEnabled, isBackupActivated, isBackupOnStart,
            isBackupOnStop, enablePasspartu;
            
            
    public static String getNickRegex, getUnloggedinGroup, getMySQLHost, getMySQLPort, 
            getMySQLUsername, getMySQLPassword, getMySQLDatabase, getMySQLTablename, 
            getMySQLColumnName, getMySQLColumnPassword, getMySQLColumnIp, getMySQLColumnLastLogin,
            getMySQLColumnSalt, getMySQLColumnGroup, unRegisteredGroup, backupWindowsPath,
            getcUnrestrictedName, getRegisteredGroup, messagesLanguage, getMySQLlastlocX, getMySQLlastlocY, getMySQLlastlocZ;
            
    
    public static int getWarnMessageInterval, getSessionTimeout, getRegistrationTimeout, getMaxNickLength,
            getMinNickLength, getPasswordMinLen, getMovementRadius, getmaxRegPerIp, getNonActivatedGroup,
            passwordMaxLength;
                    
    protected static YamlConfiguration configFile;
    
   public Settings(Plugin plugin) {
        //super(new File(Settings.PLUGIN_FOLDER + "/config.yml"), this.plugin);
        this.file = new File(plugin.getDataFolder(),"config.yml");
        
        this.plugin = plugin;

              

        //options().indent(4); 
        // Override to always indent 4 spaces
        if(exists()) {
            load();         
         }
        else {
            loadDefaults(file.getName());
            load();
        }
        
        configFile = (YamlConfiguration) plugin.getConfig();
        
        //saveDefaults();
        
    }
   
   public void loadConfigOptions() {
       
        plugin.getLogger().info("Loading Configuration File...");
        
        mergeConfig();
        
        messagesLanguage = checkLang(configFile.getString("settings.messagesLanguage","en"));
        isPermissionCheckEnabled = configFile.getBoolean("permission.EnablePermissionCheck", false);
        isForcedRegistrationEnabled  = configFile.getBoolean("settings.registration.force", true);
        isRegistrationEnabled = configFile.getBoolean("settings.registration.enabled", true);
        isTeleportToSpawnEnabled = configFile.getBoolean("settings.restrictions.teleportUnAuthedToSpawn",false);
        getWarnMessageInterval = configFile.getInt("settings.registration.messageInterval",5);
        isSessionsEnabled = configFile.getBoolean("settings.sessions.enabled",false);
        getSessionTimeout = configFile.getInt("settings.sessions.timeout",10);
        getRegistrationTimeout = configFile.getInt("settings.restrictions.timeout",30);
        isChatAllowed = configFile.getBoolean("settings.restrictions.allowChat",false);
        getMaxNickLength = configFile.getInt("settings.restrictions.maxNicknameLength",20);
        getMinNickLength = configFile.getInt("settings.restrictions.minNicknameLength",3);
        getPasswordMinLen = configFile.getInt("settings.security.minPasswordLength",4);
        getNickRegex = configFile.getString("settings.restrictions.allowedNicknameCharacters","[a-zA-Z0-9_?]*");
        isAllowRestrictedIp = configFile.getBoolean("settings.restrictions.AllowRestrictedUser",false);
        getRestrictedIp = configFile.getStringList("settings.restrictions.AllowedRestrictedUser");
        isMovementAllowed = configFile.getBoolean("settings.restrictions.allowMovement",false);
        getMovementRadius = configFile.getInt("settings.restrictions.allowedMovementRadius",100);
        getJoinPermissions = configFile.getStringList("GroupOptions.Permissions.PermissionsOnJoin");
        isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword",false);
        isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered",false);
        isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession",true);
        isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled",false);
        isSaveQuitLocationEnabled = configFile.getBoolean("settings.restrictions.SaveQuitLocation", false);
        isForceSurvivalModeEnabled = configFile.getBoolean("settings.GameMode.ForceSurvivalMode", false);
        isResetInventoryIfCreative = configFile.getBoolean("settings.GameMode.ResetInventotyIfCreative",false);
        getmaxRegPerIp = configFile.getInt("settings.restrictions.maxRegPerIp",1);
        getPasswordHash = getPasswordHash();
        getUnloggedinGroup = configFile.getString("settings.security.unLoggedinGroup","unLoggedInGroup");
        getDataSource = getDataSource();
        isCachingEnabled = configFile.getBoolean("DataSource.caching",true);
        getMySQLHost = configFile.getString("DataSource.mySQLHost","127.0.0.1");
        getMySQLPort = configFile.getString("DataSource.mySQLPort","3306");
        getMySQLUsername = configFile.getString("DataSource.mySQLUsername","authme");
        getMySQLPassword = configFile.getString("DataSource.mySQLPassword","12345");
        getMySQLDatabase = configFile.getString("DataSource.mySQLDatabase","authme");
        getMySQLTablename = configFile.getString("DataSource.mySQLTablename","authme");
        getMySQLColumnName = configFile.getString("DataSource.mySQLColumnName","username");
        getMySQLColumnPassword = configFile.getString("DataSource.mySQLColumnPassword","password");
        getMySQLColumnIp = configFile.getString("DataSource.mySQLColumnIp","ip");
        getMySQLColumnLastLogin = configFile.getString("DataSource.mySQLColumnLastLogin","lastlogin");
        getMySQLColumnSalt = configFile.getString("ExternalBoardOptions.mySQLColumnSalt");
        getMySQLColumnGroup = configFile.getString("ExternalBoardOptions.mySQLColumnGroup","");
        getMySQLlastlocX = configFile.getString("DataSource.mySQLlastlocX","x");
        getMySQLlastlocY = configFile.getString("DataSource.mySQLlastlocY","y");
        getMySQLlastlocZ = configFile.getString("DataSource.mySQLlastlocZ","z");
        getNonActivatedGroup = configFile.getInt("ExternalBoardOptions.nonActivedUserGroup", -1);
        unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup","");
        getUnrestrictedName = configFile.getStringList("settings.unrestrictions.UnrestrictedName");
        getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup","");
        getEnablePasswordVerifier = configFile.getBoolean("settings.restrictions.enablePasswordVerifier" , true);
        protectInventoryBeforeLogInEnabled = configFile.getBoolean("settings.restrictions.ProtectInventoryBeforeLogIn", true);
        passwordMaxLength = configFile.getInt("settings.security.passwordMaxLength", 20);
        isBackupActivated = configFile.getBoolean("BackupSystem.ActivateBackup",false);
        isBackupOnStart = configFile.getBoolean("BackupSystem.OnServerStart",false);
        isBackupOnStop = configFile.getBoolean("BackupSystem.OnServeStop",false);
        backupWindowsPath = configFile.getString("BackupSystem.MysqlWindowsPath", "C:\\Program Files\\MySQL\\MySQL Server 5.1\\");
        enablePasspartu = configFile.getBoolean("Passpartu.enablePasspartu",false);


        saveDefaults();
            
            //System.out.println("[AuthMe debug] Config " + getEnablePasswordVerifier.toString());
            //System.out.println("[AuthMe debug] Config " + getEnablePasswordVerifier.toString());

 
   }
   
   public static void reloadConfigOptions(YamlConfiguration newConfig) {
       configFile = newConfig;
              
       //plugin.getLogger().info("RELoading Configuration File...");
        messagesLanguage = checkLang(configFile.getString("settings.messagesLanguage","en"));
        isPermissionCheckEnabled = configFile.getBoolean("permission.EnablePermissionCheck", false);
        isForcedRegistrationEnabled = configFile.getBoolean("settings.registration.force", true);
        isRegistrationEnabled = configFile.getBoolean("settings.registration.enabled", true);
        isTeleportToSpawnEnabled = configFile.getBoolean("settings.restrictions.teleportUnAuthedToSpawn",false);
        getWarnMessageInterval = configFile.getInt("settings.registration.messageInterval",5);
        isSessionsEnabled = configFile.getBoolean("settings.sessions.enabled",false);
        getSessionTimeout = configFile.getInt("settings.sessions.timeout",10);
        getRegistrationTimeout = configFile.getInt("settings.restrictions.timeout",30);
        isChatAllowed = configFile.getBoolean("settings.restrictions.allowChat",false);
        getMaxNickLength = configFile.getInt("settings.restrictions.maxNicknameLength",20);
        getMinNickLength = configFile.getInt("settings.restrictions.minNicknameLength",3);
        getPasswordMinLen = configFile.getInt("settings.security.minPasswordLength",4);
        getNickRegex = configFile.getString("settings.restrictions.allowedNicknameCharacters","[a-zA-Z0-9_?]*");
        isAllowRestrictedIp = configFile.getBoolean("settings.restrictions.AllowRestrictedUser",false);
        getRestrictedIp = configFile.getStringList("settings.restrictions.AllowedRestrictedUser");
        isMovementAllowed = configFile.getBoolean("settings.restrictions.allowMovement",false);
        getMovementRadius = configFile.getInt("settings.restrictions.allowedMovementRadius",100);
        getJoinPermissions = configFile.getStringList("GroupOptions.Permissions.PermissionsOnJoin");
        isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword",false);
        isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered",false);
        isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession",true);
        isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled",false);     
        isSaveQuitLocationEnabled = configFile.getBoolean("settings.restrictions.SaveQuitLocation",false);
        isForceSurvivalModeEnabled = configFile.getBoolean("settings.GameMode.ForceSurvivalMode",false);
        isResetInventoryIfCreative = configFile.getBoolean("settings.GameMode.ResetInventotyIfCreative",false);
        getmaxRegPerIp = configFile.getInt("settings.restrictions.maxRegPerIp",1);
        getPasswordHash = getPasswordHash();
        getUnloggedinGroup = configFile.getString("settings.security.unLoggedinGroup","unLoggedInGroup");
        getDataSource = getDataSource();
        isCachingEnabled = configFile.getBoolean("DataSource.caching",true);
        getMySQLHost = configFile.getString("DataSource.mySQLHost","127.0.0.1");
        getMySQLPort = configFile.getString("DataSource.mySQLPort","3306");
        getMySQLUsername = configFile.getString("DataSource.mySQLUsername","authme");
        getMySQLPassword = configFile.getString("DataSource.mySQLPassword","12345");
        getMySQLDatabase = configFile.getString("DataSource.mySQLDatabase","authme");
        getMySQLTablename = configFile.getString("DataSource.mySQLTablename","authme");
        getMySQLColumnName = configFile.getString("DataSource.mySQLColumnName","username");
        getMySQLColumnPassword = configFile.getString("DataSource.mySQLColumnPassword","password");
        getMySQLColumnIp = configFile.getString("DataSource.mySQLColumnIp","ip");
        getMySQLColumnLastLogin = configFile.getString("DataSource.mySQLColumnLastLogin","lastlogin");
        getMySQLlastlocX = configFile.getString("DataSource.mySQLlastlocX","x");
        getMySQLlastlocY = configFile.getString("DataSource.mySQLlastlocY","y");
        getMySQLlastlocZ = configFile.getString("DataSource.mySQLlastlocZ","z");
        getMySQLColumnSalt = configFile.getString("ExternalBoardOptions.mySQLColumnSalt","");
        getMySQLColumnGroup = configFile.getString("ExternalBoardOptions.mySQLColumnGroup","");
        getNonActivatedGroup = configFile.getInt("ExternalBoardOptions.nonActivedUserGroup", -1);
        unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup","");
        getUnrestrictedName = configFile.getStringList("settings.unrestrictions.UnrestrictedName");
        getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup",""); 
        getEnablePasswordVerifier = configFile.getBoolean("settings.restrictions.enablePasswordVerifier" , true);
        protectInventoryBeforeLogInEnabled = configFile.getBoolean("settings.restrictions.ProtectInventoryBeforeLogIn", true);
        passwordMaxLength = configFile.getInt("settings.security.passwordMaxLength", 20);
        isBackupActivated = configFile.getBoolean("BackupSystem.ActivateBackup",false);
        isBackupOnStart = configFile.getBoolean("BackupSystem.OnServerStart",false);
        isBackupOnStop = configFile.getBoolean("BackupSystem.OnServeStop",false);     
        backupWindowsPath = configFile.getString("BackupSystem.MysqlWindowsPath", "C:\\Program Files\\MySQL\\MySQL Server 5.1\\");
        enablePasspartu = configFile.getBoolean("Passpartu.enablePasspartu",false);
       
        //System.out.println(getMySQLDatabase);
        
         
   }
   
   public void mergeConfig() {
      
       //options().copyDefaults(false);
       //System.out.println("merging config?"+contains("settings.restrictions.ProtectInventoryBeforeLogIn")+checkDefaults());
       if(!contains("settings.restrictions.ProtectInventoryBeforeLogIn")) {
           set("settings.restrictions.enablePasswordVerifier", true);
           set("settings.restrictions.ProtectInventoryBeforeLogIn", true);
       } 
       
       if(!contains("settings.security.passwordMaxLength")) {
           set("settings.security.passwordMaxLength", 20);
       }
       
       if(!contains("BackupSystem.ActivateBackup")) {
           set("BackupSystem.ActivateBackup",false);
           set("BackupSystem.OnServerStart",false);
           set("BackupSystem.OnServeStop",false);
       }
       
       
       if(!contains("BackupSystem.MysqlWindowsPath")) {
           set("BackupSystem.MysqlWindowsPath", "C:\\Program Files\\MySQL\\MySQL Server 5.1\\");
       }
       
       if(!contains("settings.messagesLanguage")) {
           set("settings.messagesLanguage","en");
       }
       
       if(!contains("passpartu.enablePasspartu")) {
           set("Passpartu.enablePasspartu",false);
       } else return;
       
       plugin.getLogger().info("Merge new Config Options..");
       plugin.saveConfig();
       
       return;
   }
   /** 
    * 
    * 
    * 
    */   
    private static HashAlgorithm getPasswordHash() {
        String key = "settings.security.passwordHash";

        try {
            return PasswordSecurity.HashAlgorithm.valueOf(configFile.getString(key,"SHA256").toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.showError("Unknown Hash Algorithm; defaulting to SHA256");
            return PasswordSecurity.HashAlgorithm.SHA256;
        }
    }
    
   /** 
    * 
    * 
    * 
    */
    private static DataSourceType getDataSource() {
        String key = "DataSource.backend";

        try {
            return DataSource.DataSourceType.valueOf(configFile.getString(key).toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.showError("Unknown database backend; defaulting to file database");
            return DataSource.DataSourceType.FILE;
        }
    }

    /**
     * Config option for setting and check restricted user by
     * username;ip , return false if ip and name doesnt amtch with
     * player that join the server, so player has a restricted access
    */   
    public static Boolean getRestrictedIp(String name, String ip) {
                 
              Iterator<String> iter = getRestrictedIp.iterator();
                while (iter.hasNext()) {
                   String[] args =  iter.next().split(";");
                  //System.out.println("name restricted "+args[0]+"name 2:"+name+"ip"+args[1]+"ip2"+ip);
                   if(args[0].equalsIgnoreCase(name) ) {
                           if(args[1].equalsIgnoreCase(ip)) {
                       //System.out.println("name restricted "+args[0]+"name 2:"+name+"ip"+args[1]+"ip2"+ip);
                           return true;
                            } else return false;
                        } 
                }
            return true;
    }

    
    /**
     * Loads the configuration from disk
     *
     * @return True if loaded successfully
     */
    public final boolean load() {
        try {
            load(file);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    public final void reload() {
        load();
        loadDefaults(file.getName());
    }

    /**
     * Saves the configuration to disk
     *
     * @return True if saved successfully
     */
    public final boolean save() {
        try {
            save(file);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Simple function for if the Configuration file exists
     *
     * @return True if configuration exists on disk
     */
    public final boolean exists() {
        return file.exists();
    }

    /**
     * Loads a file from the plugin jar and sets as default
     *
     * @param filename The filename to open
     */
    public final void loadDefaults(String filename) {
        InputStream stream = plugin.getResource(filename);
        if(stream == null) return;

        setDefaults(YamlConfiguration.loadConfiguration(stream));
    }

    /**
     * Saves current configuration (plus defaults) to disk.
     *
     * If defaults and configuration are empty, saves blank file.
     *
     * @return True if saved successfully
     */
    public final boolean saveDefaults() {
        options().copyDefaults(true);
        options().copyHeader(true);
        boolean success = save();
        options().copyDefaults(false);
        options().copyHeader(false);

        return success;
    }


    /**
     * Clears current configuration defaults
     */
    public final void clearDefaults() {
        setDefaults(new MemoryConfiguration());
    }

    /**
* Check loaded defaults against current configuration
*
* @return false When all defaults aren't present in config
*/
    public boolean checkDefaults() {
        if (getDefaults() == null) {
            return true;
        }
        return getKeys(true).containsAll(getDefaults().getKeys(true));
    }
 /*   
    public static Settings getInstance() {
        if (singleton == null) {
            singleton = new Settings();
        }
        return singleton;
    }
*/
    public static String checkLang(String lang) {
        for(messagesLang language: messagesLang.values()) {
            //System.out.println(language.toString());
            if(lang.toLowerCase().contains(language.toString())) {
                ConsoleLogger.info("Set Language: "+lang);
                return lang;
            }    
        }
        ConsoleLogger.info("Set Default Language: En ");
        return "en";
    }
    
    public enum messagesLang {
        en, de, br, cz, pl, fr
    } 
}
