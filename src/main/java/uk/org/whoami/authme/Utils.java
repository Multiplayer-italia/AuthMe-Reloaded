/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.org.whoami.authme;

import java.util.Iterator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.settings.Settings;

/**
 *
 * @author stefano
 */
public class Utils {
     //private Settings settings = Settings.getInstance();
     private Player player;
     private String currentGroup;
     private static Utils singleton;
     private String unLoggedGroup = Settings.getUnloggedinGroup;
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
                AuthMe.permission.playerAddGroup(player, Settings.unRegisteredGroup);
                break;
            }
            case REGISTERED: {
                String currentGroup = AuthMe.permission.getPrimaryGroup(player);
                AuthMe.permission.playerRemoveGroup(player, currentGroup);
                AuthMe.permission.playerAddGroup(player, Settings.getRegisteredGroup);
                break;
            }                
        }
        return;
    }
  
    public String removeAll(Player player) {
        if(!Utils.getInstance().useGroupSystem()){
            return null;
        }
        
        /*if (AuthMe.permission.playerAdd(this.player,"-*") ) {
            AuthMe.permission.playerAdd(this.player,"authme.login");
            return true;
        }*/
        
        
        //System.out.println("permissions? "+ hasPerm);
        if( !Settings.getJoinPermissions.isEmpty() ) {
            hasPermOnJoin(player);
        }
        
        this.currentGroup = AuthMe.permission.getPrimaryGroup(player.getWorld(),player.getName().toString());
        //System.out.println("current grop" + currentGroup);
        if(AuthMe.permission.playerRemoveGroup(player.getWorld(),player.getName().toString(), currentGroup) && AuthMe.permission.playerAddGroup(player.getWorld(),player.getName().toString(),this.unLoggedGroup)) {
            
            return currentGroup;
        }
        
        return null;
        
    }
    
    public boolean addNormal(Player player, String group) {
       if(!Utils.getInstance().useGroupSystem()){
            return false;
        }
       // System.out.println("in add normal");
       /* if (AuthMe.permission.playerRemove(this.player, "-*"))
            return true;
       */      
        if(AuthMe.permission.playerRemoveGroup(player.getWorld(),player.getName().toString(),this.unLoggedGroup) && AuthMe.permission.playerAddGroup(player.getWorld(),player.getName().toString(),group)) {
        //System.out.println("vecchio "+this.unLoggedGroup+ "nuovo" + group);
            return true;
        
        }
        return false;
    }    

    private String hasPermOnJoin(Player player) {
       /* if(Settings.getJoinPermissions.isEmpty())
            return null; */
              Iterator<String> iter = Settings.getJoinPermissions.iterator();
                while (iter.hasNext()) {
                    String permission = iter.next();
                 // System.out.println("permissions? "+ permission);
                     
                   if(AuthMe.permission.playerHas(player, permission)){
                     //  System.out.println("player has permissions " +permission);
                       AuthMe.permission.playerAddTransient(player, permission);
                   }
                }
           return null;
    }
    
    public boolean isUnrestricted(Player player) {
        
        
        if(Settings.getUnrestrictedName.isEmpty() || Settings.getUnrestrictedName == null)
            return false;
        
     //   System.out.println("name to escape "+player.getName());
        if(Settings.getUnrestrictedName.contains(player.getName())) {
       //     System.out.println("name to escape correctly"+player.getName());
            return true;
        }
        
        return false;
        
         
    }
     public static Utils getInstance() {
        
            singleton = new Utils();
        
        return singleton;
    } 
    
    private boolean useGroupSystem() {
        
        if(Settings.isPermissionCheckEnabled && !Settings.getUnloggedinGroup.isEmpty()) {
            return true;
        } return false;
            
    }
     
    public enum groupType {
        UNREGISTERED, REGISTERED, NOTLOGGEDIN, LOGGEDIN
    }
    
}
