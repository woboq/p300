package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.IconChooser;

public class ShowDownloadsAction extends AbstractAction {

	boolean m_moveToForeground = true;
	
	public ShowDownloadsAction() {
		super("Downloads", IconChooser.getDownloadsIcon());
	}
	
	public ShowDownloadsAction(boolean b) {
		super("Downloads", IconChooser.getDownloadsIcon());
		m_moveToForeground = b;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		MainDialog.getInstance().showSubWindow(null, "Downloads",
				MainDialog.getInstance().downloadsPanel, m_moveToForeground);
	}

}
