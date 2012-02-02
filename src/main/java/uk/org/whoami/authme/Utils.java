/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.org.whoami.authme;

import org.bukkit.entity.Player;
import uk.org.whoami.authme.settings.Settings;

/**
 *
 * @author stefano
 */
public class Utils {
     private Settings settings = Settings.getInstance();
     private String name;
  
  public Utils(String name) {
      this.name = name;
      
  }
  public void setGroup(Player player, groupType group) {
    if (!player.isOnline())
            return;
   
        switch(group) {
            case UNREGISTERED: {

                String currentGroup = AuthMe.permission.getPrimaryGroup(player);
                AuthMe.permission.playerRemoveGroup(player, currentGroup);
                AuthMe.permission.playerAddGroup(player, settings.unRegisteredGroup());
                break;
            }
            case REGISTERED: {

                String currentGroup = AuthMe.permission.getPrimaryGroup(player);
                AuthMe.permission.playerRemoveGroup(player, currentGroup);
                AuthMe.permission.playerAddGroup(player, settings.registeredGroup());
                break;
            }
            case NOTLOGGEDIN: {

                String currentGroup = AuthMe.permission.getPrimaryGroup(player);
                AuthMe.permission.playerRemoveGroup(player, currentGroup);
                AuthMe.permission.playerAddGroup(player, settings.unLoggedInGroup());
                break;
            }
            case LOGGEDIN: {

                String currentGroup = AuthMe.permission.getPrimaryGroup(player);
                AuthMe.permission.playerRemoveGroup(player, currentGroup);
                AuthMe.permission.playerAddGroup(player, settings.loggedInGroup());
                break;
            }                
            
        }
        return;
    }
    
    public enum groupType {
        UNREGISTERED, REGISTERED, NOTLOGGEDIN, LOGGEDIN
    }
    
}
