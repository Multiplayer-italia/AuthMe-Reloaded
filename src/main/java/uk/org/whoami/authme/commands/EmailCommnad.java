/*
 * Copyright 2012 darkwarriors.
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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.settings.Messages;

/**
 *
 * @author darkwarriors
 */
public class EmailCommnad implements CommandExecutor {

    private AuthMe plugin;
    private Messages m = Messages.getInstance();
    
    public EmailCommnad(Plugin plugin) {
        this.plugin = (AuthMe) plugin;
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

        if (args.length == 0) {
            player.sendMessage("usage: /email add your@email.it your@email.it ");
            player.sendMessage("usage: /email verify your@email.it [string from email] ");
            return true;
        }
        
        if(args[0].equalsIgnoreCase("add") && args.length == 3 ) {
            if(args[1].equals(args[2])) {
                // here add email in database
                
            } else {
                player.sendMessage("email doesnt match! ");
            }
        }
        
         if(args[0].equalsIgnoreCase("verify") && args.length == 3 ) {
             
         }
         
        return true;
    }
}
