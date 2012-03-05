package uk.org.whoami.authme.listener;

/**
 * @Author Hoezef
 */
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;

import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.gui.screens.LoginScreen;

public class AuthMeSpoutListener implements Listener {
	
	@EventHandler
	public void onSpoutCraftEnable(final SpoutCraftEnableEvent event)
	{
		if (!PlayerCache.getInstance().isAuthenticated(event.getPlayer().getName().toLowerCase())) {
			event.getPlayer().getMainScreen().attachPopupScreen(new LoginScreen(event.getPlayer()));
        }
	}
}
