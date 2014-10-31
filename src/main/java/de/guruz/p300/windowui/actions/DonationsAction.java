package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class DonationsAction extends AbstractAction {
	public DonationsAction() {
		super("Donate Money", IconChooser.getDonationsIcon());
	}
	
	
	public void actionPerformed(ActionEvent e) {
		BareBonesBrowserLaunch.openURL(MainDialog.getMyURL() + "fwd/donate");
	}

}
