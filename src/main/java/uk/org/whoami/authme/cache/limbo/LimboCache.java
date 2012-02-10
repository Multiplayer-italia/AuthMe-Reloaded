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

package uk.org.whoami.authme.cache.limbo;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.settings.Settings;

public class LimboCache {

    private static LimboCache singleton = null;
    private HashMap<String, LimboPlayer> cache;
    private Settings settings = Settings.getInstance();
    private FileCache playerData = new FileCache();
    
    private LimboCache() {
        this.cache = new HashMap<String, LimboPlayer>();
    }

    public void addLimboPlayer(Player player) {
        String name = player.getName().toLowerCase();
        Location loc = player.getLocation();
        int gameMode = player.getGameMode().getValue();
        ItemStack[] arm;
        ItemStack[] inv;
        boolean operator;
        String playerGroup = "";
        
        if (playerData.doesCacheExist(name)) {
            //DataFileCache playerInvArmor =  playerData.readCache(name);    
             inv =  playerData.readCache(name).getInventory();
             arm =  playerData.readCache(name).getArmour();
             playerGroup = playerData.readCache(name).getGroup();
             operator = playerData.readCache(name).getOperator();
        } else {
        inv =  player.getInventory().getContents();
        arm =  player.getInventory().getArmorContents();
            
        // check if player is an operator, then save it to ram if cache dosent exist!
        
            if(player.isOp() ) {
                //System.out.println("player is an operator in limboCache");
                operator = true;
                }
                   else operator = false;      
        }

       
        
        if(settings.isForceSurvivalModeEnabled()) {
            if(settings.isResetInventoryIfCreative() && gameMode != 0 ) {
               player.sendMessage("Your inventory has been cleaned!");
               inv = new ItemStack[36];
               arm = new ItemStack[4];
            }
            gameMode = 0;
        } 
        if(player.isDead()) {
        	loc = player.getWorld().getSpawnLocation();
        }
        
        if(cache.containsKey(name) && playerGroup.isEmpty()) {
            //System.out.println("contiene il player "+name);
            LimboPlayer groupLimbo = cache.get(name);
            playerGroup = groupLimbo.getGroup();
        }
        
        cache.put(player.getName().toLowerCase(), new LimboPlayer(name, loc, inv, arm, gameMode, operator, playerGroup));
    //System.out.println("il gruppo in limboChace "+playerGroup);
    }
    
    public void addLimboPlayer(Player player, String group) {
        
        cache.put(player.getName().toLowerCase(), new LimboPlayer(player.getName().toLowerCase(), group));
   //System.out.println("il gruppo in limboChace "+group);
    }
    
    public void deleteLimboPlayer(String name) {
        cache.remove(name);
    }

    public LimboPlayer getLimboPlayer(String name) {
        return cache.get(name);
    }

    public boolean hasLimboPlayer(String name) {
        return cache.containsKey(name);
    }
    
    
    public static LimboCache getInstance() {
        if (singleton == null) {
            singleton = new LimboCache();
        }
        return singleton;
    }
}
