package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class SendFeedbackAction extends AbstractAction {
	public SendFeedbackAction() {
		super("Send Feedback", null);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		BareBonesBrowserLaunch.openURL("http://p300.wufoo.com/forms/p300-feedback/");
	}

}
