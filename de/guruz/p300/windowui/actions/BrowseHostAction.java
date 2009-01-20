package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.windowui.panels.LANHostPanel;

public class BrowseHostAction extends AbstractAction{

	Host m_host;
	RemoteDir m_dir;
	
	public BrowseHostAction(Host h, RemoteDir dir) {
		super("Browse Host", IconChooser.getAddIcon());
		
		m_host = h;
		m_dir = dir;
	}

	public void actionPerformed(ActionEvent arg0) {
		LANHostPanel lhp = new LANHostPanel (m_host);
		MainDialog.instance.showSubWindow(null, "Browser: " + m_host.getDisplayName(), lhp);
		
		if (m_dir == null)
			lhp.openDirAsync(new RemoteDir ("/"));
		else
			lhp.openDirAsync(m_dir);
	}

}
