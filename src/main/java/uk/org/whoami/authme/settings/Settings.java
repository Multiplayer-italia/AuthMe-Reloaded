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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
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
    public static final String MESSAGE_FILE = Settings.PLUGIN_FOLDER + "/messages.yml";
    public static final String SETTINGS_FILE = Settings.PLUGIN_FOLDER + "/config.yml";
    private static List<String> joinPerm = null;
    private static List<String> unrestricted = null;
    private static List<String> getRestrictedIp;
    private static Settings singleton;
    public final Plugin plugin;
    private final File file;    
    
    public static DataSourceType getDataSource;
    public static HashAlgorithm getPasswordHash;
    
    public static Boolean isPermissionCheckEnabled, isRegistrationEnabled, isForcedRegistrationEnabled,
            isTeleportToSpawnEnabled, isSessionsEnabled, isChatAllowed, isAllowRestrictedIp, 
            isMovementAllowed, isKickNonRegisteredEnabled, isForceSingleSessionEnabled,
            isForceSpawnLocOnJoinEnabled, isForceExactSpawnEnabled, isSaveQuitLocationEnabled,
            isForceSurvivalModeEnabled, isResetInventoryIfCreative, isCachingEnabled, isKickOnWrongPasswordEnabled;
            
            
    public static String getNickRegex, getUnloggedinGroup, getMySQLHost, getMySQLPort, 
            getMySQLUsername, getMySQLPassword, getMySQLDatabase, getMySQLTablename, 
            getMySQLColumnName, getMySQLColumnPassword, getMySQLColumnIp, getMySQLColumnLastLogin,
            getMySQLColumnSalt, getMySQLColumnGroup, unRegisteredGroup,
            getcUnrestrictedName, getRegisteredGroup;
            
    
    public static int getWarnMessageInterval, getSessionTimeout, getRegistrationTimeout, getMaxNickLength,
            getMinNickLength, getPasswordMinLen, getMovementRadius, getmaxRegPerIp, getNonActivatedGroup;
            
    protected static YamlConfiguration configFile;
    
   public Settings(Plugin plugin) {
        //super(new File(Settings.PLUGIN_FOLDER + "/config.yml"), this.plugin);
        this.file = new File(plugin.getDataFolder(),"config.yml");
        
        this.plugin = plugin;

              

        //options().indent(4); 
        // Override to always indent 4 spaces
        if(exists())
            load();
        else {
            loadDefaults(file.getName());
            checkDefaults();

            load();
        }
        
        configFile = (YamlConfiguration) plugin.getConfig();
        
        
    }
   
   public void loadConfigOptions() {
       
       plugin.getLogger().info("Loading Configuration File...");
       
       isForcedRegistrationEnabled = configFile.getBoolean("permission.EnablePermissionCheck", true);
        isRegistrationEnabled = configFile.getBoolean("settings.registration.force", true);
        isForcedRegistrationEnabled = configFile.getBoolean("settings.registration.enabled",true);
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
        getcJoinPermissions();
        isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword",false);
        isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered",false);
        isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession",true);
        isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled",false);
        isForceExactSpawnEnabled = configFile.getBoolean("settings.restrictions.ForceExactSpawn",false);
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
        getMySQLColumnSalt = configFile.getString("VBullettinOptions.mySQLColumnSalt","");
        getMySQLColumnGroup = configFile.getString("VBullettinOptions.mySQLColumnGroup","");
        getNonActivatedGroup = configFile.getInt("VBullettinOptions.nonActivedUserGroup", -1);
        unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup","");
        //getcUnrestrictedName();
        getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup","");  
        
        
   }
   
   public static void reloadConfigOptions(YamlConfiguration newConfig) {
       configFile = newConfig;
              
       //plugin.getLogger().info("RELoading Configuration File...");
        isForcedRegistrationEnabled = configFile.getBoolean("permission.EnablePermissionCheck", true);
        isRegistrationEnabled = configFile.getBoolean("settings.registration.force", true);
        isForcedRegistrationEnabled = configFile.getBoolean("settings.registration.enabled",true);
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
        getcJoinPermissions();
        isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword",false);
        isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered",false);
        isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession",true);
        isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled",false);
        isForceExactSpawnEnabled = configFile.getBoolean("settings.restrictions.ForceExactSpawn",false);
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
        getMySQLColumnSalt = configFile.getString("VBullettinOptions.mySQLColumnSalt","");
        getMySQLColumnGroup = configFile.getString("VBullettinOptions.mySQLColumnGroup","");
        getNonActivatedGroup = configFile.getInt("VBullettinOptions.nonActivedUserGroup", -1);
        unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup","");
        //getcUnrestrictedName();
        getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup",""); 
        
        System.out.println(getMySQLDatabase);
         
   }
/*    private void write() {
        isPermissionCheckEnabled();
        isRegistrationEnabled();
        isForcedRegistrationEnabled();
        isTeleportToSpawnEnabled();
        getWarnMessageInterval();
        isSessionsEnabled();
        getSessionTimeout();
        getRegistrationTimeout();
        isChatAllowed();
        getMaxNickLength();
        getMinNickLength();
        getPasswordMinLen();
        getNickRegex();
        isAllowRestrictedIp();
        getRestrictedIp("accountest" , "127.0.0.1");
        isMovementAllowed();
        getMovementRadius();
        getcJoinPermissions();
        isKickNonRegisteredEnabled();
        isForceSingleSessionEnabled();
        isForceSpawnLocOnJoinEnabled();
        isForceExactSpawnEnabled();
        isSaveQuitLocationEnabled();
        isForceSurvivalModeEnabled();
        isResetInventoryIfCreative();
        getmaxRegPerIp();
        getPasswordHash();
        getUnloggedinGroup();
        getDataSource();
        isCachingEnabled();
        getMySQLHost();
        getMySQLPort();
        getMySQLUsername();
        getMySQLPassword();
        getMySQLDatabase();
        getMySQLTablename();
        getMySQLColumnName();
        getMySQLColumnPassword();
        getMySQLColumnIp();
        getMySQLColumnLastLogin();
        getMySQLColumnSalt();
        getMySQLColumnGroup();
        getNonActivatedGroup();
        unRegisteredGroup();
        getcUnrestrictedName();
        getRegisteredGroup();
        save();
    }
   
    //
    // This diable all group switching and permissions check!
    //
    public boolean isPermissionCheckEnabled() {
        String key = "permission.EnablePermissionCheck";
        if (getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }
     
    public boolean isForcedRegistrationEnabled() {
        String key = "settings.registration.force";
        if (getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }

    public boolean isRegistrationEnabled() {
        String key = "settings.registration.enabled";
        if (getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }

    public int getWarnMessageInterval() {
        String key = "settings.registration.messageInterval";
        if (getString(key) == null) {
            setProperty(key, 5);
        }
        return getInt(key, 5);
    }

    public boolean isSessionsEnabled() {
        String key = "settings.sessions.enabled";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public int getSessionTimeout() {
        String key = "settings.sessions.timeout";
        if (getString(key) == null) {
            setProperty(key, 10);
        }
        return getInt(key, 10);
    }

    public boolean isKickOnWrongPasswordEnabled() {
        String key = "settings.restrictions.kickOnWrongPassword";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }

    public int getMinNickLength() {
        String key = "settings.restrictions.minNicknameLength";
        if (getString(key) == null) {
            setProperty(key, 3);
        }
        return getInt(key, 3);
    }

    public int getmaxRegPerIp() {
        String key = "settings.restrictions.maxRegPerIp";
        if (getString(key) == null) {
            setProperty(key, 1);
        }
        return getInt(key, 1);        
    }
    
    public int getMaxNickLength() {
        String key = "settings.restrictions.maxNicknameLength";
        if (getString(key) == null) {
            setProperty(key, 20);
        }
        return getInt(key, 20);
    }

    public String getNickRegex() {
        String key = "settings.restrictions.allowedNicknameCharacters";
        if (getString(key) == null) {
            setProperty(key, "[a-zA-Z0-9_?]*");
        }
        return getString(key, "[a-zA-Z0-9_?]*");
    }

    public int getRegistrationTimeout() {
        String key = "settings.restrictions.timeout";
        if (getString(key) == null) {
            setProperty(key, 30);
        }
        return getInt(key, 30);
    }

    public boolean isChatAllowed() {
        String key = "settings.restrictions.allowChat";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }

    public boolean isMovementAllowed() {
        String key = "settings.restrictions.allowMovement";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }

    public int getMovementRadius() {
        String key = "settings.restrictions.allowedMovementRadius";
        if (getString(key) == null) {
            setProperty(key, 100);
        }
        return getInt(key, 100);
    }

    public boolean isKickNonRegisteredEnabled() {
        String key = "settings.restrictions.kickNonRegistered";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public boolean isForceSingleSessionEnabled() {
        String key = "settings.restrictions.ForceSingleSession";
        if (getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }
    
    public boolean isForceExactSpawnEnabled() {
        String key = "settings.restrictions.ForceExactSpawn";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
               
    public boolean isSaveQuitLocationEnabled() {
        String key = "settings.restrictions.SaveQuitLocation";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public boolean isForceSpawnLocOnJoinEnabled() {
        String key = "settings.restrictions.ForceSpawnLocOnJoinEnabled";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);        
    }
    
    public boolean isTeleportToSpawnEnabled() {
        String key = "settings.restrictions.teleportUnAuthedToSpawn";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }
    
    public boolean isForceSurvivalModeEnabled() {
        String key = "settings.GameMode.ForceSurvivalMode";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);        
    }

    public boolean isResetInventoryIfCreative() {
        String key = "settings.GameMode.ResetInventotyIfCreative";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);        
    }
    
    public HashAlgorithm getPasswordHash() {
        String key = "settings.security.passwordHash";
        if (getString(key) == null) {
            setProperty(key, "SHA256");
        }

        try {
            return PasswordSecurity.HashAlgorithm.valueOf(getString(key).toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.showError("Unknown Hash Algorithm; defaulting to SHA256");
            return PasswordSecurity.HashAlgorithm.SHA256;
        }
    }

    public int getPasswordMinLen() {
        String key = "settings.security.minPasswordLength";
        if (getString(key) == null) {
            setProperty(key, 4);
        }
        return getInt(key, 4);        
    }
    
    public String getUnloggedinGroup() {
        String key = "settings.security.unLoggedinGroup";
        if (getString(key) == null) {
            setProperty(key, "unLoggedinGroup");
        }
        return getString(key, "unLoggedinGroup*");
            
    }
    
    public boolean isCachingEnabled() {
        String key = "DataSource.caching";
        if (getString(key) == null) {
            setProperty(key, true);
        }
        return getBoolean(key, true);
    }

    public DataSourceType getDataSource() {
        String key = "DataSource.backend";
        if (getString(key) == null) {
            setProperty(key, "file");
        }

        try {
            return DataSource.DataSourceType.valueOf(getString(key).toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.showError("Unknown database backend; defaulting to file database");
            return DataSource.DataSourceType.FILE;
        }
    }

    public String getMySQLHost() {
        String key = "DataSource.mySQLHost";
        if (getString(key) == null) {
            setProperty(key, "127.0.0.1");
        }
        return getString(key);
    }

    public String getMySQLPort() {
        String key = "DataSource.mySQLPort";
        if (getString(key) == null) {
            setProperty(key, "3306");
        }
        return getString(key);
    }

    public String getMySQLUsername() {
        String key = "DataSource.mySQLUsername";
        if (getString(key) == null) {
            setProperty(key, "authme");
        }
        return getString(key);
    }

    public String getMySQLPassword() {
        String key = "DataSource.mySQLPassword";
        if (getString(key) == null) {
            setProperty(key, "12345");
        }
        return getString(key);
    }

    public String getMySQLDatabase() {
        String key = "DataSource.mySQLDatabase";
        if (getString(key) == null) {
            setProperty(key, "authme");
        }
        return getString(key);
    }

    public String getMySQLTablename() {
        String key = "DataSource.mySQLTablename";
        if (getString(key) == null) {
            setProperty(key, "authme");
        }
        return getString(key);
    }

    public String getMySQLColumnName() {
        String key = "DataSource.mySQLColumnName";
        if (getString(key) == null) {
            setProperty(key, "username");
        }
        return getString(key);
    }

    public String getMySQLColumnPassword() {
        String key = "DataSource.mySQLColumnPassword";
        if (getString(key) == null) {
            setProperty(key, "password");
        }
        return getString(key);
    }

    public String getMySQLColumnIp() {
        String key = "DataSource.mySQLColumnIp";
        if (getString(key) == null) {
            setProperty(key, "ip");
        }
        return getString(key);
    }

    public String getMySQLColumnLastLogin() {
        String key = "DataSource.mySQLColumnLastLogin";
        if (getString(key) == null) {
            setProperty(key, "lastlogin");
        }
        return getString(key);
    }

    //
    // Vbulletin Board Hook 3.X 4.X
    //
    public String getMySQLColumnSalt() {
        String key = "VBullettinOptions.mySQLColumnSalt";
        if (getString(key) == null) {
            setProperty(key, "");
        }
        return getString(key);
    }
    
    public String getMySQLColumnGroup() {
        String key = "VBullettinOptions.mySQLColumnGroup";
        if (getString(key) == null) {
            setProperty(key, "");
        }
        return getString(key);
    }
    
    public int getNonActivatedGroup() {
        String key = "VBullettinOptions.nonActivedUserGroup";
        if (getString(key) == null) {
            setProperty(key, "-1");
        }
        return getInt(key, -1);
    }
    
    //
    // Check if Restriction against IP is TRUE
    //
    public Boolean isAllowRestrictedIp() {
        String key = "settings.restrictions.AllowRestrictedUser";
        if (getString(key) == null) {
            setProperty(key, false);
        }
        return getBoolean(key, false);
    }        
         
     
    //
    // Config option for setting and check restricted user by
    // username;ip , return false if ip and name doesnt amtch with
    // player that join the server, so player has a restricted access
    //   
    public Boolean getRestrictedIp(String name, String ip) {
        List<String> restricted = getStringList("settings.restrictions.AllowedRestrictedUser", new ArrayList<String>());
            if(restricted.isEmpty()) {
                setProperty("settings.restrictions.AllowedRestrictedUser",restricted);           
            }     
            
              Iterator<String> iter = restricted.iterator();
                while (iter.hasNext()) {
                   String[] args =  iter.next().split(";");
                  //System.out.println("name restricted "+args[0]+"name 2:"+name+"ip"+args[1]+"ip2"+ip);
                   if(args[0].equals(name) ) {
                           if(args[1].equals(ip)) {
                       //System.out.println("name restricted "+args[0]+"name 2:"+name+"ip"+args[1]+"ip2"+ip);
                           return true;
                            } else return false;
                        } 
                }
            return true;
    }
    
    //
    // Config option for set playername that should bypass registration
    // this is needed for mods like buildcraft but it is very doungerous!
    // return true if input name is found inside string list
    //
    private List<String> getcUnrestrictedName() {
        this.unrestricted = getStringList("settings.unrestrictions.UnrestrictedName", new ArrayList<String>());
            if(unrestricted.isEmpty()) {
                //unrestricted = Arrays.asList("mynameisunrestricted");
                setProperty("settings.unrestrictions.UnrestrictedName",unrestricted);           
            }             
            return unrestricted;
    }
    
    public List<String> getUnrestrictedName(){
        return this.unrestricted;
        
    }
    
    //
    // Config option for set player permissions on join, it will check
    // if given permissions is founded in String arraty list.
    // return true if input permissions is found inside string list
    //
    private List<String> getcJoinPermissions() {
        this.joinPerm = getStringList("GroupOptions.Permissions.PermissionsOnJoin", new ArrayList<String>());
            if(joinPerm.isEmpty()) {
                setProperty("GroupOptions.Permissions.PermissionsOnJoin",joinPerm);           
            }
       return joinPerm;
    }
    
    public List<String> getJoinPermissions() {
        return this.joinPerm;
    }
    
    //
    // Config option of different group settings about UNREGISTERED, REGISTERED
    //
    public String unRegisteredGroup() {
        String key = "GroupOptions.UnregisteredPlayerGroup";
        if (getString(key) == null) {
            setProperty(key, "");
        }
        return getString(key);        
    }
   
    public String getRegisteredGroup() {
        String key = "GroupOptions.RegisteredPlayerGroup";
        if (getString(key) == null) {
            setProperty(key, "");
        }
        return getString(key);        
    }
    */
   
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
                   if(args[0].equals(name) ) {
                           if(args[1].equals(ip)) {
                       //System.out.println("name restricted "+args[0]+"name 2:"+name+"ip"+args[1]+"ip2"+ip);
                           return true;
                            } else return false;
                        } 
                }
            return true;
    }

    //
    // Config option for set playername that should bypass registration
    // this is needed for mods like buildcraft but it is very doungerous!
    // return true if input name is found inside string list
    //
    public static List<String> getcUnrestrictedName() {
        return unrestricted;
        /*
        this.unrestricted = getStringList("settings.unrestrictions.UnrestrictedName", new ArrayList<String>());
            if(unrestricted.isEmpty()) {
                //unrestricted = Arrays.asList("mynameisunrestricted");
                setProperty("settings.unrestrictions.UnrestrictedName",unrestricted);           
            }             
            return unrestricted;*/
    }
    
    public static List<String> getUnrestrictedName(){
        return unrestricted;
        
    }
    
    //
    // Config option for set player permissions on join, it will check
    // if given permissions is founded in String arraty list.
    // return true if input permissions is found inside string list
    //
    private static List<String> getcJoinPermissions() {
        return joinPerm;
       /* this.joinPerm = getStringList("GroupOptions.Permissions.PermissionsOnJoin", new ArrayList<String>());
            if(joinPerm.isEmpty()) {
                setProperty("GroupOptions.Permissions.PermissionsOnJoin",joinPerm);           
            }
       return joinPerm;*/
    }
    
    public static List<String> getJoinPermissions() {
        return joinPerm;
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

    public void checkDefaults() {
        if(getValues(true).size() < getDefaults().getValues(true).size()) {
            saveDefaults();
        }
    }
 /*   
    public static Settings getInstance() {
        if (singleton == null) {
            singleton = new Settings();
        }
        return singleton;
    }
*/

}
