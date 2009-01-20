package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class EarnAction extends AbstractAction {
	public EarnAction() {
		super("Earn Money", null);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		BareBonesBrowserLaunch.openURL(MainDialog.getMyURL() + "fwd/earn");
	}

}
