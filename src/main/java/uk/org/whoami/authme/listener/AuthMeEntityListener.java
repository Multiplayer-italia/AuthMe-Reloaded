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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.citizens.CitizensCommunicator;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Settings;

public class AuthMeEntityListener extends EntityListener {

    private DataSource data;
    private Settings settings = Settings.getInstance();

    public AuthMeEntityListener(DataSource data) {
        this.data = data;
    }

   @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
 
        if (!(entity instanceof Player)) {
            return;
        }
        /*
         System.out.println("[ Entity Damage ] "+event.getEntity().toString());
         @Future implementation till CombatTag dont release any apis
        if(event.getEntity().toString().indexOf("PvPLogger") != -1 ) {
            System.out.println("la stringa contiene PvPLogger 2");
            return;
        }
        */
        if(CitizensCommunicator.isNPC(entity)) {
            return;
        }

        Player player = (Player) entity;
        String name = player.getName().toLowerCase();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
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
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        String name = player.getName().toLowerCase();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
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
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        String name = player.getName().toLowerCase();

        if (PlayerCache.getInstance().isAuthenticated(name)) {
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
