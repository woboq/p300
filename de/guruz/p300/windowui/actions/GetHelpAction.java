package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class GetHelpAction extends AbstractAction {
	public GetHelpAction() {
		super("Get Help", null);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		BareBonesBrowserLaunch.openURL(MainDialog.getMyURL() + "fwd/support");
	}

}
