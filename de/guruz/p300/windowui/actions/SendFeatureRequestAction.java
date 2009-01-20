package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class SendFeatureRequestAction extends AbstractAction {

	
	public SendFeatureRequestAction() {
		super("Suggest Feature", null);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		BareBonesBrowserLaunch.openURL("http://p300.uservoice.com/");
	}

}
