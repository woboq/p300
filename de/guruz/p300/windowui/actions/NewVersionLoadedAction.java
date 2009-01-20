package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class NewVersionLoadedAction extends AbstractAction {
	public NewVersionLoadedAction() {
		super("New Version Loaded", IconChooser.getDonationsIcon());
	}
	
	
	public void actionPerformed(ActionEvent e) {
		if (!MainDialog.isHeadless())
			BareBonesBrowserLaunch.openURL(MainDialog.getMyURL() + "fwd/new_version_loaded");
	}

}
