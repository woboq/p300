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
package de.guruz.p300.dirbrowser;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;

import de.guruz.p300.affiliates.LeadBulletOpener;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.URL;

/**
 * This widget implements directory browsing in the UI
 * 
 * 
 * @author guruz
 * 
 */
public class BrowserWidget extends JPanel implements ActionListener,
		ListSelectionListener, DownloadStartedListener {

	enum stateType {
		INITIAL, FETCHING, DONE
	};

	stateType state = stateType.INITIAL;

	JTable table;

	JPanel toolbar;

	JPanel statusbar;

	JLabel helpLabel;

	JTextArea dirLabel;

	JTextArea errorLabel;

	JButton download;

	JButton index;

	JButton up;

	JButton reload;

	JButton loadInBrowser;

	JButton stop;

	JButton products;

	RemoteDir currentDirectory = null;

	RemoteDir currentRequestedDirectory = null;

	DirListingCache dirListingCache = new DirListingCache(this);

	Host host = null;

	public BrowserWidget(Host h) {
		super();
		this.host = h;
		this.createSubWidgets();
	}

	private void createSubWidgets() {
		this.setLayout(new BorderLayout());

		this.toolbar = new JPanel();
		this.add(this.toolbar, BorderLayout.NORTH);

		this.download = new JButton();
		this.download.setIcon(IconChooser.getDownloadIcon ());
		this.download.setToolTipText("Download");
		this.download.addActionListener(this);
		this.download.setActionCommand("download");
		this.download.setEnabled(false);

		this.index = new JButton();
		this.index.setIcon(IconChooser
				.iconImageFromResource("22x22/go-home.png"));
		this.index.setToolTipText("Index");
		this.index.addActionListener(this);
		this.index.setActionCommand("index");
		this.index.setEnabled(false);

		this.up = new JButton();
		this.up.setIcon(IconChooser.iconImageFromResource("22x22/go-up.png"));
		this.up.setToolTipText("Up");
		this.up.addActionListener(this);
		this.up.setActionCommand("up");
		this.up.setEnabled(false);

		this.reload = new JButton();
		this.reload.setIcon(IconChooser
				.iconImageFromResource("22x22/view-refresh.png"));
		this.reload.setToolTipText("Reload");
		this.reload.addActionListener(this);
		this.reload.setActionCommand("reload");
		this.reload.setEnabled(false);

		this.loadInBrowser = new JButton();
		this.loadInBrowser.setToolTipText("Open this directory in the Webbrowser");
		this.loadInBrowser.setIcon(IconChooser
				.iconImageFromResource("22x22/internet-web-browser.png"));
		this.loadInBrowser.addActionListener(this);
		this.loadInBrowser.setActionCommand("loadinbrowser");
		this.loadInBrowser.setEnabled(false);

		this.stop = new JButton();
		this.stop.setToolTipText("Stop");
		this.stop.setIcon(IconChooser
				.iconImageFromResource("22x22/process-stop.png"));
		this.stop.addActionListener(this);
		this.stop.setActionCommand("stop");
		this.stop.setEnabled(false);

		this.products = new JButton();
		this.products.setToolTipText("Products");
		this.products.setIcon(IconChooser.getSharemonkeyIcon());
		this.products.addActionListener(this);
		this.products.setActionCommand("products");
		this.products.setEnabled(false);

		// this.toolbar.add(new JLabel("Browse control: "));
		this.toolbar.add(this.download);
		this.toolbar.add(this.index);
		this.toolbar.add(this.up);
		this.toolbar.add(this.reload);
		this.toolbar.add(this.loadInBrowser);
		this.toolbar.add(this.stop);
		//this.toolbar.add(this.products);

		this.table = new JTable() {
			private static final long serialVersionUID = 1L;

			public Class<?> getColumnClass(int column) {
				return getValueAt(0, column).getClass();
			}
		};
		this.table.setShowGrid(false);
		this.table.setRowHeight(Math.max(22, this.table.getRowHeight()));
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.add(new JScrollPane(this.table), BorderLayout.CENTER);

		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table.getSelectionModel().addListSelectionListener(this);
		this.table.setPreferredScrollableViewportSize(new Dimension(300, 70));

		this.table.getTableHeader().setReorderingAllowed(false);

		this.statusbar = new JPanel(new BorderLayout());
		this.add(this.statusbar, BorderLayout.SOUTH);

		directorySubPanel = new JPanel();
		directorySubPanel.setLayout(new BorderLayout());
		errorSubPanel = new JPanel();
		errorSubPanel.setLayout(new BorderLayout());
		this.dirLabel = new JTextArea();
		this.dirLabel.setWrapStyleWord(false);
		this.dirLabel.setLineWrap(true);
		this.dirLabel.setEditable(false);
		this.errorLabel = new JTextArea();
		this.errorLabel.setWrapStyleWord(false);
		this.errorLabel.setLineWrap(true);
		this.errorLabel.setEditable(false);
		directorySubPanel.add(new JLabel("Current directory: "),
				BorderLayout.WEST);
		directorySubPanel.add(new JScrollPane(this.dirLabel),
				BorderLayout.CENTER);
		errorSubPanel.add(new JLabel("Error: "), BorderLayout.WEST);
		errorSubPanel.add(this.errorLabel, BorderLayout.CENTER);

		this.helpLabel = new JLabel(
				"<HTML><B></B></HTML>");

		this.statusbar.add(directorySubPanel, BorderLayout.NORTH);
		this.statusbar.add(helpLabel, BorderLayout.CENTER);
		this.statusbar.add(errorSubPanel, BorderLayout.SOUTH);

		errorSubPanel.setVisible(false);

		final BrowserWidget thisBrowserWidget = this;
		m_downloadPopupMenu = new JPopupMenu();
		m_downloadPopupMenu.addPopupMenuListener(new PopupMenuListener() {

			public void popupMenuCanceled(PopupMenuEvent e) {

			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				JPopupMenu pop = (JPopupMenu) e.getSource();
				pop.removeAll();

				// do we have a file selected?
				Object o = null;

				int row = -1;
				try {
					Point pos = table.getMousePosition();
					row = table.rowAtPoint(pos);
				} catch (Exception e2)
				{
					row = table.getSelectedRow();
				}

				try {
					o = table.getValueAt(row, 1);
					//System.out.println(o);
				} catch (Exception e2) {
					o = null;
				}

				if (o == null)
					o = getCurrentClickedItem();

				if (o != null && o instanceof RemoteFile) {
					RemoteFile f = (RemoteFile) o;
					JMenuItem file = new JMenuItem();
					DownloadFileAction dfa = new DownloadFileAction("Download "
							+ URL.decode(f.getPath()), host, f, f.getSize(), thisBrowserWidget);
					file.setAction(dfa);
					pop.add(file);
				} else if ((o != null && o instanceof RemoteDir)) {
					RemoteDir d = (RemoteDir) o;
					JMenuItem dir = new JMenuItem();

					DownloadDirAction dda = new DownloadDirAction(host, d, thisBrowserWidget);
					dda.putValue(Action.NAME, "Download "
							+ URL.decode(d.getPath()));
					dir.setAction(dda);
					pop.add(dir);
				}

				if (currentDirectory != null) {
					JMenuItem currentDir = new JMenuItem();
					DownloadDirAction dda = new DownloadDirAction(host,
							new RemoteDir(currentDirectory.getPath()), thisBrowserWidget);
					dda.putValue(Action.NAME, "Download "
							+ URL.decode(currentDirectory.getPath()));
					currentDir.setAction(dda);
					pop.add(currentDir);
				}
			}
		});

		this.table.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {

				// check if it is a double click
				if (e.getClickCount() >= 2 && (e.getButton() == e.BUTTON1)) {
					actionPerformed(new ActionEvent(this, 0, "open"));
				}

			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					m_downloadPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					m_downloadPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel directorySubPanel;

	private JPanel errorSubPanel;

	protected JPopupMenu m_downloadPopupMenu;

	public synchronized void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("download"))
		{
			m_downloadPopupMenu.show(this.download, 0, download.getHeight());
		} else if (ae.getActionCommand().equals("index")) {
			//D.out("|-- Index Action");
			this.fetchDir(new RemoteDir("/"));
		} else if (ae.getActionCommand().equals("open")) {
			Object o = getCurrentClickedItem();
			if (o instanceof RemoteEntity) {
				this.fetchCurrentlyClicked();
			}
		} else if (ae.getActionCommand().equals("up")) {
			RemoteDir oneUp = this.currentDirectory.getParent();
			this.fetchDir(oneUp);
		} else if (ae.getActionCommand().equals("reload")) {
			this.fetchDir(this.currentDirectory, true);
		} else if (ae.getActionCommand().equals("loadinbrowser")) {
			
			String url = this.getHost().getBestHostLocation().toHttpUrl();
			url = url + this.currentDirectory.getPath();
			de.guruz.p300.utils.launchers.BareBonesBrowserLaunch.openURL(url);

		} else if (ae.getActionCommand().equals("stop")) {
			if (this.state == stateType.FETCHING) {
				this.changeState(stateType.DONE);
				// just re-fetch the current dir, this will re-enable/disable
				// all buttons
				// and it will be served from cache anyway
				this.fetchDir(this.currentDirectory);
			}
		} else if (ae.getActionCommand().equals("products")) {
			Object o = getCurrentClickedItem();
			if (o != null && o instanceof RemoteEntity) {
				LeadBulletOpener
						.openPossibleProductsInBrowser((RemoteEntity) o);
			}
		}
	}

	/**
	 * Holt das subdir asynchron wo wir draufgeklickt haben
	 * 
	 */
	protected void fetchCurrentlyClicked() {
		try {
			// do not do anything when being ampty
			if (this.currentDirectory == null) {
				return;
			}

			Object o = this.getCurrentClickedItem();

			// should not happen anyway..
			if (!(o instanceof RemoteEntity))
				return;

			RemoteEntity re = (RemoteEntity) o;

			if (re.isDirectory()) {
				// check if it is a directory that was clicked on
				this
						.fetchDir(new RemoteDir(this.currentDirectory, re
								.getName()));
			} else {
				// a file. hooray. this will also add the other sources of that
				// file
				// de.guruz.p300.MainDialog.downloadManager.startDownload(host,
				// (RemoteFile) re, re.getSize());
				// helpLabel.setText("Trying to download "
				// + URL.decode(re.getName()));
				// MainDialog.downloadManager.updateDownloadsTableModel(true);
				new DownloadFileAction(null, host, (RemoteFile) re, re
						.getSize(), this).actionPerformed(null);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object getCurrentClickedItem() {
		try {
			return this.table
					.getValueAt(((this.table.getSelectedRows())[0]), 1);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Start a fetch. this may just hit the cache and our callback
	 * dirFetchingDone will be directly called
	 * 
	 * @param d
	 */
	public synchronized void fetchDir(RemoteDir d) {
		this.fetchDir(d, false);
	}

	private synchronized void fetchDir(RemoteDir d, boolean invalidateCached) {
		if (d == null)
			return;

		// disable/enable buttons
		this.download.setEnabled(false);
		this.stop.setEnabled(true);
		this.reload.setEnabled(false);
		this.up.setEnabled(false);
		this.index.setEnabled(false);
		this.setWaitCursor();

		// System.out.println("Starting to fetch " + d.getPath());
		this.changeState(stateType.FETCHING);
		this.currentRequestedDirectory = d;
		//D.out("|-- Starting to fetch " + d.getPath());
		this.dirListingCache.startFetch(d, invalidateCached);
	}

	/**
	 * Callback
	 */
	public synchronized void dirFetchingDone(RemoteDir dir, final DirListing dl) {
		if (dir.equals(this.currentRequestedDirectory)
				&& this.getState() != stateType.DONE) {
			// System.out.println("Fetched " + dir.getPath());
			//D.out("|-- BrowserWidget: dirFetchingDone for " + dir.getPath());

			// change state
			this.setNormalCursor();
			this.changeState(stateType.DONE);

			// was it successful?
			if (dl.getError() == null) {

				try {
					// switch model
					if (!SwingUtilities.isEventDispatchThread()) {
						//D.out("|-- Switching table model in Swing-Thread for "
						//		+ dir.getPath());
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								table.setModel(new DirListingTableModel(dl));
							}
						});
						//D
						//		.out("|-- Done switching table model in Swing-Thread for "
						//				+ dir.getPath());
					} else {
						//D.out("|-- Switching table model here for "
						//		+ dir.getPath());
						table.setModel(new DirListingTableModel(dl));
						//D.out("|-- Done switching table model here for "
						//		+ dir.getPath());
					}

					TableColumn firstColumn = this.table.getColumnModel()
							.getColumn(0);
					firstColumn.setMinWidth((int) (IconChooser.getGuiIconWidth ()*1.3));
					firstColumn.setMaxWidth((int) (IconChooser.getGuiIconWidth ()*1.3));
					firstColumn.setPreferredWidth((int) (IconChooser.getGuiIconWidth ()*1.3));

					this.currentDirectory = dir;

					this.dirLabel.setText(URL.decode(dir.getPath()));
					errorSubPanel.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				this.errorLabel.setText(dl.getError() + " while fetching "
						+ URL.decode(dir.getPath()));
				errorSubPanel.setVisible(true);

			}

			if (this.currentDirectory == null) {
				this.download.setEnabled(false);
				this.up.setEnabled(false);
				this.index.setEnabled(true);
				this.reload.setEnabled(false);
				this.loadInBrowser.setEnabled(false);
				this.stop.setEnabled(false);
			} else {
				this.download.setEnabled(true);
				// only be able to go up when not in /.
				this.up.setEnabled(!this.currentDirectory.isRoot());

				// only be able to go to index when not in /.
				this.index.setEnabled(!this.currentDirectory.isRoot());

				// disable stop button and enable reload button
				this.reload.setEnabled(true);
				this.loadInBrowser.setEnabled(true);
				this.stop.setEnabled(false);
			}
		}
	}

	private void setWaitCursor() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	private void setNormalCursor() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private stateType getState() {
		return this.state;
	}

	protected void changeState(stateType s) {
		this.state = s;
	}

	public Host getHost() {
		return this.host;
	}

	public void valueChanged(ListSelectionEvent e) {
		products.setEnabled(getCurrentClickedItem() != null);

	}

	public void downloadWasStarted(Host h, RemoteEntity e) {
		helpLabel.setText("Downloading " + e.getName());
	}

}
