package uk.org.whoami.authme.listener;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;

import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.gui.screens.LoginScreen;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class AuthMeSpoutListener implements Listener {

	private DataSource database;
	private Settings settings = Settings.getInstance();
	private Messages m = Messages.getInstance();
	private AuthMe plugin = AuthMe.getInstance();

	public AuthMeSpoutListener(DataSource database)
	{
		this.database  = database;
	}
	
	@EventHandler
	public void onSpoutCraftEnable(final SpoutCraftEnableEvent event)
	{
		if (!PlayerCache.getInstance().isAuthenticated(event.getPlayer().getName().toLowerCase())) {
			event.getPlayer().getMainScreen().attachPopupScreen(new LoginScreen(this,event.getPlayer()));
        }
	}
	
	public String performLogin(Player player, String password)
	{
        return null;
	}
}
