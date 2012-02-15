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
     private Player player;
     private String currentGroup;
     private static Utils singleton;
     private String unLoggedGroup = settings.getUnloggedinGroup();
  /*   
  public Utils(Player player) {
      this.player = player;
      
  }
  */
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
                AuthMe.permission.playerAddGroup(player, settings.getRegisteredGroup());
                break;
            }                
        }
        return;
    }
  
    public String removeAll(Player player) {
        
        /*if (AuthMe.permission.playerAdd(this.player,"-*") ) {
            AuthMe.permission.playerAdd(this.player,"authme.login");
            return true;
        }*/

        this.currentGroup = AuthMe.permission.getPrimaryGroup(player);
        //System.out.println("current grop" + currentGroup);
        if(AuthMe.permission.playerRemoveGroup(player, currentGroup) && AuthMe.permission.playerAddGroup(player,this.unLoggedGroup)) {
            
            return currentGroup;
        }
        
        return null;
        
    }
    
    public boolean addNormal(Player player, String group) {
       // System.out.println("dentro add normal");
       /* if (AuthMe.permission.playerRemove(this.player, "-*"))
            return true;
       */ 
        if(AuthMe.permission.playerRemoveGroup(player,this.unLoggedGroup) && AuthMe.permission.playerAddGroup(player,group)) {
        //System.out.println("vecchio "+this.unLoggedGroup+ "nuovo" + group);
            return true;
        
        }
        return false;
    }    
    
     public static Utils getInstance() {
        
            singleton = new Utils();
        
        return singleton;
    } 
     
    public enum groupType {
        UNREGISTERED, REGISTERED, NOTLOGGEDIN, LOGGEDIN
    }
    
}
