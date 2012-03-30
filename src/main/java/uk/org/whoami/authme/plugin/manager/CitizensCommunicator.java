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

package uk.org.whoami.authme.plugin.manager;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.citizensnpcs.api.CitizensManager;
import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class CitizensCommunicator {

    public static boolean isNPC(Entity player) {
        PluginManager pm = player.getServer().getPluginManager();
        Plugin plugin = pm.getPlugin("Citizens");
         
        if(plugin != null) {
            /*
             *  Checl the Citizens Version 1.1.6 or 2.0
             */
            String Ver = plugin.getDescription().getVersion();
            String[] args = Ver.split("\\.");
            
            if(args[0].equals("1")) 
                return CitizensManager.isNPC(player);
            else return CitizensAPI.getNPCManager().isNPC(player);
            /* old method
            try {
                if( Class.forName("net.citizensnpcs.api.CitizensManager") != null)
                    return CitizensManager.isNPC(player);
                else return CitizensAPI.getNPCManager().isNPC(player);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CitizensCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            }
             */
        }
        return false;
    }
}
