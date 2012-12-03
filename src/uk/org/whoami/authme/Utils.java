/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.org.whoami.authme;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.settings.Settings;

/**
 *
 * @author stefano
 */
public class Utils {
     //private Settings settings = Settings.getInstance();
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
    if(!Settings.isPermissionCheckEnabled)
        return;
    
        switch(group) {
            case UNREGISTERED: {
                currentGroup = AuthMe.permission.getPrimaryGroup(player);
                AuthMe.permission.playerRemoveGroup(player, currentGroup);
                AuthMe.permission.playerAddGroup(player, Settings.unRegisteredGroup);
                break;
            }
            case REGISTERED: {
                currentGroup = AuthMe.permission.getPrimaryGroup(player);
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
    
    public void packCoords(World world, int x, final int y, int z, final Player pl)
    /*     */   {
    /* 156 */     final Location loc = new Location(world, x + 0.5D, y + 0.6D, z + 0.5D);
    /*     */ 
    /* 158 */     if (!world.getChunkAt(loc).isLoaded())
    /*     */     {
    /* 160 */       world.getChunkAt(loc).load();
    /*     */     }
    /*     */ 
    /* 163 */     pl.teleport(loc);
    /*     */ 
    			final int id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(AuthMe.authme, new Runnable()
    			{
					public void run() {
    		            int current = (int)pl.getLocation().getY();
    	    		     
   		             	if (current != y) {
   		             		//ConsoleLogger.showError("Problems on SpawnLocation: " + pl.getLocation());
   		             		pl.teleport(loc);
   		             	}
					}

    			}, 1L, 20L);
    
      Bukkit.getScheduler().scheduleAsyncDelayedTask(AuthMe.authme, new Runnable()
      {

		@Override
		public void run() {
			Bukkit.getScheduler().cancelTask(id);
			
		}
    	  
      }, 60L);
      }

	/*
     * Random Token for passpartu
     * 
     */
    public boolean obtainToken() {
        File file = new File("plugins/AuthMe/passpartu.token");

	if (file.exists())
            file.delete();

		FileWriter writer = null;
		try {
			file.createNewFile();
			writer = new FileWriter(file);
                        String token = generateToken();
                        writer.write(token+":"+System.currentTimeMillis()/1000+"\r\n");
                        writer.flush();
                        System.out.println("[AuthMe] Security passpartu token: "+ token);
                        writer.close();
                        return true;
                } catch(Exception e) {
                    e.printStackTrace(); 
                }
                
        
        return false;
    }
    
    /*
     * Read Toekn
     */
    public boolean readToken(String inputToken) {
        File file = new File("plugins/AuthMe/passpartu.token");
        
	if (!file.exists()) 	
            return false;
        
        if (inputToken.isEmpty() )
            return false;
        
		Scanner reader = null;
		try {
			reader = new Scanner(file);

			while (reader.hasNextLine()) {
				final String line = reader.nextLine();

				if (line.contains(":")) {   
                                   String[] tokenInfo = line.split(":");
                                   //System.err.println("Authme input token "+inputToken+" saved token "+tokenInfo[0]);
                                   //System.err.println("Authme time "+System.currentTimeMillis()/1000+"saved time "+Integer.parseInt(tokenInfo[1]));
                                   if(tokenInfo[0].equals(inputToken) && System.currentTimeMillis()/1000-30 <= Integer.parseInt(tokenInfo[1]) ) { 
                                       file.delete();
                                       reader.close();
                                       return true;
                                   }
                                } 
                        }
                } catch(Exception e) {
                    e.printStackTrace(); 
                }
                
	reader.close();        
        return false;
    }
    /*
     * Generate Random Token
     */
    private String generateToken() {
           // obtain new random token 
           Random rnd = new Random ();
            char[] arr = new char[5];

            for (int i=0; i<5; i++) {
                    int n = rnd.nextInt (36);
                    arr[i] = (char) (n < 10 ? '0'+n : 'a'+n-10);
            }
            
            return new String(arr);        
    }
    public enum groupType {
        UNREGISTERED, REGISTERED, NOTLOGGEDIN, LOGGEDIN
    }
    
}
