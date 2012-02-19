package uk.org.whoami.authme.gui.screens;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.gui.Clickable;
import uk.org.whoami.authme.gui.CustomButton;
import uk.org.whoami.authme.listener.AuthMeSpoutListener;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Settings;

public class LoginScreen extends GenericPopup implements Clickable{

	private SpoutPlayer splayer;
	private AuthMe plugin = AuthMe.getInstance();
	private Settings settings = Settings.getInstance();
	private AuthMeSpoutListener listener;
	
	private CustomButton exitBtn;
	private CustomButton loginBtn;
	private GenericTextField passBox;
	private GenericLabel titleLbl;
	private GenericLabel textLbl;
	private GenericLabel errorLbl;
	
	String exitTxt = "Quit";
	String loginTxt = "Login";
	String exitMsg = "Good Bye";
	String title = settings.getString("LoginGUI.title","LOGIN");
	String[] textlines = (String[]) settings.getStringList("LoginGUI.text", new ArrayList<String>()).toArray();

	public LoginScreen(AuthMeSpoutListener listener, SpoutPlayer player) {
		this.splayer = player;
		this.listener = listener;
		
		createScreen();
	}

	private void createScreen() {
		int objects = textlines.length + 4;
		int part = !(textlines.length <= 5) ? 195 / objects : 20;
		int freespace = part / 4;
		int h = 3*part/4, w = 8*part;
		
		titleLbl = new GenericLabel();
		titleLbl
			.setText(title)
			.setTextColor(new Color(1.0F, 0, 0, 1.0F))
			.setAlign(WidgetAnchor.TOP_CENTER)
			.setHeight(h)
			.setWidth(w)
			.setX(maxWidth / 2 )
			.setY(25);
		this.attachWidget(plugin, titleLbl);
		
		int ystart = 25 + h + part/2;
		for (int x=0; x<textlines.length;x++)
		{
			textLbl = new GenericLabel();
			textLbl
				.setText(textlines[x])
				.setAlign(WidgetAnchor.TOP_CENTER)
				.setHeight(h)
				.setWidth(w)
				.setX(maxWidth / 2)
				.setY(ystart + x*part);
			this.attachWidget(plugin, textLbl);
		}
		
		passBox = new GenericTextField();
		passBox
			.setMaximumCharacters(18)
			.setMaximumLines(1)
			.setHeight(h-2)
			.setWidth(w-2)
			.setY(220-h - 2*part);
		passBox.setPasswordField(true);
		setXToMid(passBox);
		this.attachWidget(plugin, passBox);
		
		errorLbl = new GenericLabel();
		errorLbl
			.setText("")
			.setTextColor(new Color(1.0F, 0, 0, 1.0F))
			.setHeight(h)
			.setWidth(w)
			.setX(passBox.getX() + passBox.getWidth() + 2)
			.setY(passBox.getY());
		this.attachWidget(plugin, errorLbl);
		
		loginBtn = new CustomButton(this);
		loginBtn
			.setText(loginTxt)
			.setHeight(h)
			.setWidth(w)
			.setY(220-h-part);
		setXToMid(loginBtn);
		this.attachWidget(plugin, loginBtn);
		
		exitBtn = new CustomButton(this);
		exitBtn
			.setText(exitTxt)
			.setHeight(h)
			.setWidth(w)
			.setY(220-h);
		setXToMid(exitBtn);
		this.attachWidget(plugin, exitBtn);
		
	}

	public void handleClick(ButtonClickEvent event) {
		Button b = event.getButton();
		if (b.equals(loginBtn))
		{
			String result = listener.performLogin(event.getPlayer(), passBox.getText());
			if(result == null) event.getPlayer().closeActiveWindow();
			else
			{
				errorLbl.setText(result);
			}
		}else if(b.equals(exitBtn))
		{
			event.getPlayer().kickPlayer(exitMsg);
		}
	}

	private void setXToMid(Widget w) {
		w.setX( (maxWidth - w.getWidth()) / 2);
	}
	

}
