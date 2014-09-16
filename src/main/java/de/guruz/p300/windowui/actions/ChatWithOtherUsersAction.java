package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.Configuration;
import de.guruz.p300.utils.URL;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class ChatWithOtherUsersAction extends AbstractAction {
	public ChatWithOtherUsersAction() {
		super("Chat With Other p300 Users", null);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		//BareBonesBrowserLaunch.openURL("http://p300.eu/support/");
		BareBonesBrowserLaunch.openURL("http://widget.mibbit.com/?server=irc.euirc.net&channel=%23p300&autoConnect=true&nick="
				+ URL.encode(Configuration.instance().getLocalDisplayName()));
	}

}
