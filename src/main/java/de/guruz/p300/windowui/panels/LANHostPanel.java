/*
 Copyright 2006, 2007, 2008 Markus Goetz, Sebastian Breier
 Webpage on http://p300.eu/
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.guruz.p300.windowui.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.guruz.p300.MainDialog;
import de.guruz.p300.dirbrowser.BrowserWidget;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.onetoonechat.ui.ChatWindowMap;
import de.guruz.p300.search.ui.SearchResultsWindow;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class LANHostPanel extends JPanel implements ActionListener {
	private static final String CHAT = "chat";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @author guruz
	 */
	protected JButton openLANHostInWWWButton;

	protected Host host = null;

	public Host getHost() {
		return this.host;
	}

	public void setHost(Host h) {
		this.host = h;

	}

	BrowserWidget browser = null;

	private JButton searchButton;

	public static final String ACTION_OPEN_LANHOST_IN_WWW = "openLANHostInWWW";

	public static final String SEARCH = "search";

	public LANHostPanel(Host host) {
		super();

		this.setHost(host);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.openLANHostInWWWButton = new JButton();
		this.openLANHostInWWWButton.setToolTipText("Webinterface");
		this.openLANHostInWWWButton.setIcon(IconChooser.iconImageFromResource("22x22/applications-internet.png"));
		
		this.openLANHostInWWWButton
				.setActionCommand(LANHostPanel.ACTION_OPEN_LANHOST_IN_WWW);
		this.openLANHostInWWWButton.addActionListener(this);

		this.searchButton = new JButton();
		this.searchButton.setToolTipText("Search");
		this.searchButton.setIcon(IconChooser.iconImageFromResource("22x22/system-search.png"));
		this.searchButton.setActionCommand(LANHostPanel.SEARCH);
		this.searchButton.addActionListener(this);
		
		JButton mountButton = new JButton ();
		mountButton.setToolTipText("Mount");
		mountButton.setIcon(IconChooser.iconImageFromResource("22x22/system-file-manager.png"));
		// 
		mountButton.setEnabled(false);
		
		JButton chatButton = new JButton ();
		chatButton.setToolTipText("Chat");
		chatButton.setIcon(IconChooser.iconImageFromResource("22x22/internet-mail.png"));
		chatButton.setActionCommand(LANHostPanel.CHAT);
		chatButton.addActionListener(this);
		

		JPanel lanHostSubPanel = new JPanel();
		lanHostSubPanel.add(this.openLANHostInWWWButton);
		lanHostSubPanel.add(searchButton);
		lanHostSubPanel.add(mountButton);
		lanHostSubPanel.add(chatButton);
		this.add(lanHostSubPanel, BorderLayout.NORTH);
		

		browser = new BrowserWidget(host);
		this.add(browser, BorderLayout.CENTER);


	}
	
	public void openDirAsync (final RemoteDir d)
	{
		SwingUtilities.invokeLater(new Runnable () {
			public void run() {
				browser.fetchDir(d);
			} });

	}

	public void actionPerformed(ActionEvent ae) {
		String acs = ae.getActionCommand();

		
		if (acs.equals(LANHostPanel.ACTION_OPEN_LANHOST_IN_WWW)) {
			String URL = host.toURL();

			BareBonesBrowserLaunch.openURL(URL);

		} else if (acs.equals(LANHostPanel.SEARCH)) {
			String s = JOptionPane.showInputDialog("Enter some keywords");
			
			if (s != null && s.trim().length() > 0)
			{
				SearchResultsWindow srw = new SearchResultsWindow(s);
				MainDialog.getInstance().showSubWindow(null, srw.getTitle(), srw);
				srw.asyncSearchStartOneHost(host);
			}
		} else if (acs.equals(LANHostPanel.CHAT)) {
			ChatWindowMap cwm = MainDialog.getInstance().chatWindowMap;
			
			if (cwm != null)
			{
				cwm.getChatWindowFor (host).setVisible(true);
				
				cwm.getChatComponentFor(host).tryToFocusInputField ();
			}
		}

	}

}
