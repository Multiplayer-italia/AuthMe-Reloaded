/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.org.whoami.authme;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import uk.org.whoami.authme.settings.Settings;

/**
 *
 * @author stefano
 */
public class PerformBackup {
 
   private String dbName = Settings.getMySQLDatabase;
   private String dbUserName = Settings.getMySQLUsername;
   private String dbPassword = Settings.getMySQLPassword;
   private String tblname = Settings.getMySQLTablename;
   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
   String dateString = format.format( new Date()   );
   private String path = AuthMe.getInstance().getDataFolder()+"/backups/backup"+dateString;
   
   public boolean PerformBackup() {
       
       switch(Settings.getDataSource) {
           case FILE: return FileBackup("auths.db"); 

           case MYSQL: return MySqlBackup();

           case SQLITE: return FileBackup(Settings.getMySQLDatabase+".db");

       }
       
       return false;
   }
    
   private boolean MySqlBackup() {
       File dirBackup = new File(AuthMe.getInstance().getDataFolder()+"/backups");
       
       if(!dirBackup.exists())
           dirBackup.mkdir();
       if(checkWindows(Settings.backupWindowsPath)) {
        String executeCmd = Settings.backupWindowsPath+"\\bin\\mysqldump.exe -u " + dbUserName + " -p" + dbPassword + " " + dbName + " --tables " + tblname + " -r " + path+".sql";
        //ConsoleLogger.info(executeCmd);
        Process runtimeProcess;
        try {
            //System.out.println("path "+path+" cmd "+executeCmd);
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();

            if (processComplete == 0) {
                ConsoleLogger.info("Backup created successfully");
                return true;
            } else {
                ConsoleLogger.info("Could not create the backup");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }           
       } else {    
        String executeCmd = "mysqldump -u " + dbUserName + " -p" + dbPassword + " " + dbName + " --tables " + tblname + " -r " + path+".sql";
        Process runtimeProcess;
        try {
            //System.out.println("path "+path+" cmd "+executeCmd);
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();

            if (processComplete == 0) {
                ConsoleLogger.info("Backup created successfully");
                return true;
            } else {
                ConsoleLogger.info("Could not create the backup");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
       }
        return false;
    }
   
   private boolean FileBackup(String backend) {
       File dirBackup = new File(AuthMe.getInstance().getDataFolder()+"/backups");
       
       if(!dirBackup.exists())
           dirBackup.mkdir();
       
       try {
          
           copy(new File("plugins/AuthMe/"+backend),new File(path+".db"));
           return true;
          
       } catch (Exception ex) {
           ex.printStackTrace();
       }
       
       
       return false;
   }
   
   /*
    *  Check if we are under Windows and correct location
    * of mysqldump.exe otherwise return error.
    */
   private boolean checkWindows(String windowsPath) {
      String isWin = System.getProperty("os.name").toLowerCase();
       
      if(isWin.indexOf("win") >= 0) {
          //ConsoleLogger.info(windowsPath+"\\bin\\mysqldump.exe");
          if(new File(windowsPath+"\\bin\\mysqldump.exe").exists()) {
              return true;
          } else {
              ConsoleLogger.showError("Mysql Windows Path is incorrect please check it");
              return true;
          }
       } else return false;
       
       
   }

   /*
    * Copyr src bytefile into dst file
    */
   void copy(File src, File dst) throws IOException {
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);

    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
    }
    in.close();
    out.close();
    }
   
}
