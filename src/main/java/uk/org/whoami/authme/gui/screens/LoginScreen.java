package uk.org.whoami.authme.gui.screens;

/**
 * @Author Hoezef
 */
import java.util.List;

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
import uk.org.whoami.authme.gui.Clickable;
import uk.org.whoami.authme.gui.CustomButton;
import uk.org.whoami.authme.settings.SpoutCfg;

public class LoginScreen extends GenericPopup implements Clickable{

	private AuthMe plugin = AuthMe.getInstance();
	private SpoutCfg spoutCfg = SpoutCfg.getInstance();
	
	private CustomButton exitBtn;
	private CustomButton loginBtn;
	private GenericTextField passBox;
	private GenericLabel titleLbl;
	private GenericLabel textLbl;
	private GenericLabel errorLbl;
	
	String exitTxt = spoutCfg.getString("LoginScreen.exit button"); //"Quit";
	String loginTxt = spoutCfg.getString("LoginScreen.login button"); //"Login";
	String exitMsg = spoutCfg.getString("LoginScreen.exit message"); //"Good Bye";
	String title = spoutCfg.getString("LoginScreen.title"); //"LOGIN"
	List<String> textlines = spoutCfg.getStringList("LoginScreen.text");
	@SuppressWarnings("unused")
	private SpoutPlayer splayer;
	
	public LoginScreen(SpoutPlayer player) {
		this.splayer = player;
		
		createScreen();
	}

	private void createScreen() {
		int objects = textlines.size() + 4;
		int part = !(textlines.size() <= 5) ? 195 / objects : 20;
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
		for (int x=0; x<textlines.size();x++)
		{
			textLbl = new GenericLabel();
			textLbl
				.setText(textlines.get(x))
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

	@SuppressWarnings("deprecation")
	public void handleClick(ButtonClickEvent event) {
		Button b = event.getButton();
		if (b.equals(loginBtn))
		{
			//System.out.println("player: " + event.getPlayer().getName() + ", Password: " + passBox.getText() + ", Management: " + plugin.management + ", Plugin: " + plugin);
			String result = plugin.management.performLogin(event.getPlayer(), passBox.getText());
			if(result == "") event.getPlayer().closeActiveWindow();
			else
			{
				errorLbl.setText(result);
				passBox.setText("");
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
