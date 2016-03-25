import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.io.File;

public class DockyMcDockface {
  static PopupMenu popup = null;
  static ActionListener restoreDockL = null;
  static String userFolder = null;

  public static void main(String[] args) throws MalformedURLException {
      TrayIcon trayIcon = null;
       if (SystemTray.isSupported()) {

       	userFolder = System.getProperty("user.home")

       	File configFolder = new File(userFolder + "/.docky/");
		if (configFolder.exists() == false) {
		   System.err.println("Creating configuration folder...");
		   configFolder.mkdir();
		}

           SystemTray tray = SystemTray.getSystemTray();
           Image image = null;
           if(detectDarkMode()){
             image = Toolkit.getDefaultToolkit().getImage("dock_white.png");
            }else{
              image = Toolkit.getDefaultToolkit().getImage("dock.png");
            }

           // create a action listener to listen for default action executed on the tray icon
           ActionListener saveDockL = new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   String profileName = (String)JOptionPane.showInputDialog(null, "Name of profile:");
                   if(profileName != null){
	                   File profile = new File(userFolder + "/.docky/" + profileName + ".plist");
	                   if(profile.exists()){
	                   		int overwrite = JOptionPane.showConfirmDialog(null, "A profile with that name already exists. Overwrite?", "Save anyway?", JOptionPane.YES_NO_OPTION);
	                   		if(overwrite == 0){
	                   			saveProfile(profileName);
	                   		}
	                   }else{
	                   		saveProfile(profileName);
	                   		MenuItem newProfile = new MenuItem(profileName);
	                   		newProfile.addActionListener(restoreDockL);
	                   		popup.insert(newProfile, 2);
	                   }
	               }
               }
           };

           ActionListener quitTheThingL = new ActionListener() {
           	public void actionPerformed(ActionEvent e) {
           		System.exit(0);
           	}
           };

           restoreDockL = new ActionListener() {
           	public void actionPerformed(ActionEvent e) {
           		restoreProfile(e.getActionCommand());
           	}
           };


           popup = new PopupMenu();

           MenuItem saveDock = new MenuItem("Save this dock...");
           saveDock.addActionListener(saveDockL);
           MenuItem quitTheThing = new MenuItem("Quit");
           quitTheThing.addActionListener(quitTheThingL);

           // Put the menu together!
           popup.add(saveDock);
           popup.addSeparator();

           int q = 0;
           File[] configFiles = configFolder.listFiles();

           MenuItem[] profileList = new MenuItem[configFiles.length + 5];

           for(int i = 0; i < configFiles.length; i++){
           	if(configFiles[i].isFile()){
           		profileList[q] = new MenuItem(configFiles[i].getName().replace(".plist", ""));
           		profileList[q].addActionListener(restoreDockL);
           		popup.add(profileList[q]);
           		q++;
           	}
           }

           popup.addSeparator();
           popup.add(quitTheThing);

           trayIcon = new TrayIcon(image, "Manage dock profiles", popup);

           try {
               tray.add(trayIcon);
           } catch (AWTException e) {
               System.err.println(e);
           }
       } else {
       	 System.err.println("You probably need a system tray for this to work.");
           System.exit(1);
       }

  }

  private static boolean detectDarkMode() {
    try {
        final Process proc = Runtime.getRuntime().exec(new String[] {"defaults", "read", "-g", "AppleInterfaceStyle"});
        proc.waitFor();
        return proc.exitValue() == 0;
    } catch (Exception ex) {
         System.err.println("Could not determine, whether 'dark mode' is being used. Falling back to default (light) mode.");
        return false;
    }
  }

  private static void saveProfile(String profileName){
  	try {
  		final Process proc = Runtime.getRuntime().exec(new String[] {"cp", userFolder + "/Library/Preferences/com.apple.dock.plist", userFolder + "/.docky/" + profileName + ".plist"});
  	} catch (Exception ex) {
  		System.err.println("Profile " + profileName + " could not be saved.");
  	}
  }

  private static void restoreProfile(String profileName){
  	try {
  		final Process proc = Runtime.getRuntime().exec(new String[] {"cp", userFolder + "/.docky/" + profileName + ".plist", userFolder + "/Library/Preferences/com.apple.dock.plist"});
  		final Process dockproc = Runtime.getRuntime().exec(new String[] {"killall", "Dock"});
  	} catch (Exception ex) {
  		System.err.println("Profile " + profileName + " could not be restored.");
  	}
  }
}