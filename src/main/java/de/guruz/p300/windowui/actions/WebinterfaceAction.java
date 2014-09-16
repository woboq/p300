package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.requests.OneTimeManager;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.RandomGenerator;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class WebinterfaceAction extends AbstractAction {

	public WebinterfaceAction() {
		super("Webinterface", IconChooser.getWebinterfaceIcon());
	}
	
	public void actionPerformed(ActionEvent e) {
		String oneTimeKey = RandomGenerator.string();
		String oneTimeURL = MainDialog.getMyURL() + "onetime/" + oneTimeKey;
		OneTimeManager.register(oneTimeKey);
		BareBonesBrowserLaunch.openURL(oneTimeURL);

	}

}
