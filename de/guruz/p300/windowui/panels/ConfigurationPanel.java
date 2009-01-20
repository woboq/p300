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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.logging.D;
import de.guruz.p300.search.IndexerThread;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.utils.DirectoryPicker;
import de.guruz.p300.utils.DirectoryUtils;
import de.guruz.p300.utils.FileNameUtils;
import de.guruz.p300.utils.FileUtils;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.windowui.ShareTableModel;

public class ConfigurationPanel extends JTabbedPane implements ActionListener {

	private static final String SET_DOWNLOAD_DIRECTORY = "SetDownloadDirectory";

	public static final long serialVersionUID = 0l;

	/**
	 * 
	 * @author guruz
	 */
	private JTextField passwordField;

	/**
	 * Button to add a new share to the shares list Opens windows to enter name
	 * and directory
	 * 
	 * @author guruz
	 */
	private JButton addshareButton;

	/**
	 * Button used to modify a share directory
	 * 
	 * @author guruz
	 */
	private JButton modifyshareButton;

	/**
	 * Button used to delete a share
	 * 
	 * @author guruz
	 */
	private JButton deleteShareButton;

	/**
	 * Table showing the list of shares
	 * 
	 * @author guruz
	 */
	private JTable shareTable;

	private JButton copymyadressbutton;
	private JButton openWebinterfaceButton;

	protected JPanel m_sharesPanel;

	protected JCheckBox m_chatSoundCheckbox;

	private JPanel m_downloadsPanel;

	public ConfigurationPanel() {
		{
			// see webinterface stuff
			JPanel webInterfacePanel = new JPanel();
			webInterfacePanel
					.add(new JLabel(
							"<HTML><CENTER>You can use the webinterface to configure most of the settings</CENTER></HTML>"));

			this.copymyadressbutton = new JButton(
					"Copy my address to clipboard");
			this.copymyadressbutton.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
				}

				public void mouseEntered(MouseEvent arg0) {
				}

				public void mouseExited(MouseEvent arg0) {
				}

				public void mousePressed(MouseEvent arg0) {
					de.guruz.p300.windowui.LocalURLsPopupMenu.instance().show(
							copymyadressbutton, 0,
							copymyadressbutton.getSize().height);
				}

				public void mouseReleased(MouseEvent arg0) {
				}
			});

			webInterfacePanel.add(this.copymyadressbutton);

			openWebinterfaceButton = new JButton("Open Webinterface");
			openWebinterfaceButton
					.setActionCommand(MainDialog.ACTION_OPEN_MY_WEBINTERFACE);
			openWebinterfaceButton.addActionListener(MainDialog.getInstance());

			webInterfacePanel.add(this.openWebinterfaceButton);

			this.addTab("Webinterface", null, webInterfacePanel);

		}

		{
			// shares stuff

			this.addshareButton = new JButton();
			this.addshareButton.setToolTipText("Add a share");
			this.addshareButton.setIcon(IconChooser
					.iconImageFromResource("22x22/list-add.png"));
			this.addshareButton.setActionCommand(MainDialog.ACTION_ADD_SHARE);
			this.addshareButton.addActionListener(this);

			this.modifyshareButton = new JButton();
			this.modifyshareButton.setToolTipText("Modify share");
			this.modifyshareButton.setIcon(IconChooser
					.iconImageFromResource("22x22/preferences-system.png"));
			this.modifyshareButton
					.setActionCommand(MainDialog.ACTION_MODIFY_SHARE);
			this.modifyshareButton.addActionListener(this);

			this.deleteShareButton = new JButton();
			this.deleteShareButton.setToolTipText("Delete share");
			this.deleteShareButton.setIcon(IconChooser
					.iconImageFromResource("22x22/list-remove.png"));
			this.deleteShareButton
					.setActionCommand(MainDialog.ACTION_DELETE_SHARE);
			this.deleteShareButton.addActionListener(this);

			this.shareTable = new JTable(ShareTableModel.instance());
			this.shareTable.setShowGrid(false);
			JScrollPane shareScrollPane = new JScrollPane(this.shareTable);
			// shareScrollPane.setPreferredSize(shareTable.getMinimumSize());

			// FIXME: spalten gescheite groesse machen
			this.shareTable
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.shareTable.setPreferredScrollableViewportSize(new Dimension(
					300, 70));
			this.shareTable
					.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

			m_sharesPanel = new JPanel();
			m_sharesPanel.setLayout(new BorderLayout());
			m_sharesPanel.add(new JLabel(
					"You offer files in these directories:"),
					BorderLayout.NORTH);
			m_sharesPanel.add(shareScrollPane, BorderLayout.CENTER);

			JPanel sharesSubPanel = new JPanel();

			m_sharesPanel.add(sharesSubPanel, BorderLayout.SOUTH);

			sharesSubPanel.add(this.addshareButton);
			sharesSubPanel.add(this.modifyshareButton);
			sharesSubPanel.add(this.deleteShareButton);

			this.addTab("Shares", null, m_sharesPanel);
		}
		
		
		{
			m_downloadsPanel = new JPanel ();
			JButton setDownloadDirectoryButton = new JButton ("Set Download Directory", IconChooser
					.iconImageFromResource("22x22/system-file-manager.png"));
			setDownloadDirectoryButton.setActionCommand(ConfigurationPanel.SET_DOWNLOAD_DIRECTORY);
			setDownloadDirectoryButton.addActionListener(this);
			
			m_downloadsPanel.add(setDownloadDirectoryButton);
			
			this.addTab("Downloads", null, m_downloadsPanel);
		}

		// password stuff
		{
			this.passwordField = new JTextField(20);
			this.passwordField
					.setHorizontalAlignment((int) JTextField.CENTER_ALIGNMENT);
			this.passwordField.setEditable(false);

			JButton resetadminpasswordbutton = new JButton("Reset");
			resetadminpasswordbutton
					.setActionCommand(MainDialog.ACTION_RESET_ADMIN_PASSWORD);
			resetadminpasswordbutton.addActionListener(this);

			JButton getadminpasswordButton = new JButton("Show/Hide");
			getadminpasswordButton
					.setActionCommand(MainDialog.ACTION_SHOW_HIDE_ADMINPASSWORD);
			getadminpasswordButton.addActionListener(this);

			JPanel passwordPanel = new JPanel();
			passwordPanel.setLayout(new BoxLayout(passwordPanel,
					BoxLayout.PAGE_AXIS));
			passwordPanel
					.add(new JLabel(
							"You can set a password if you want to access the configuration remotely:"));

			JPanel passwordSubPanel = new JPanel();
			passwordPanel.add(passwordSubPanel);

			passwordSubPanel.add(resetadminpasswordbutton);
			passwordSubPanel.add(getadminpasswordButton);
			passwordSubPanel.add(this.passwordField);
			this.addTab("Admin Password", null, passwordPanel);
		}

		
		{
			
			m_chatSoundCheckbox = new JCheckBox ("Play sound when chat message is sent or received", Configuration.instance().isPlayChatSound());
			m_chatSoundCheckbox.addChangeListener(new ChangeListener () {

				public void stateChanged(ChangeEvent e) {
					Configuration.instance().setPlayChatSound(m_chatSoundCheckbox.isSelected());
					
				}});
			
			JPanel soundPanel = new JPanel ();
			soundPanel.add(m_chatSoundCheckbox);
			this.addTab("Sounds", null, soundPanel);
		}
		

	}

	public void actionPerformed(ActionEvent ae) {
		String acs = ae.getActionCommand();
		if (acs.equals(MainDialog.ACTION_SHOW_HIDE_ADMINPASSWORD)) {
			if (this.passwordField.getText().length() == 0) {
				this.passwordField.setText(Configuration.instance()
						.getAdminPassword());
			} else {
				this.passwordField.setText("");
			}

		} else if (acs.equals(MainDialog.ACTION_RESET_ADMIN_PASSWORD)) {
			MainDialog.resetAdminPassword();
			this.passwordField.setText(Configuration.instance()
					.getAdminPassword());
		} else if (acs.equals(MainDialog.ACTION_ADD_SHARE)) {

			String dir = DirectoryPicker.pick(MainDialog.getWindow(), "Add",
					null,  "Add");

			if (dir != null) {
				File d = new File(dir);

				String s = (String) JOptionPane
						.showInputDialog(
								MainDialog.getWindow(),
								"Pick a name for a share (only normal letters and numbers)",
								"Pick share name", JOptionPane.PLAIN_MESSAGE,
								null, null, d.getName());

				if ((s != null) && (s.length() > 0)) {

					boolean succ = ShareManager.instance().addShare(s, dir);

					if (succ) {
						// tell indexer to restart
						if (MainDialog.indexerThread != null) {
							IndexerThread.restartIndexer();
						}

						this.shareTable.revalidate();

						JOptionPane
								.showMessageDialog(this,
										"Share added. It will take a while until it can be searched");
					} else {
						JOptionPane
								.showMessageDialog(this,
										"Error: Share was not added. Check the console");
					}
				}
			}

		} else if (acs.equals(MainDialog.ACTION_MODIFY_SHARE)) {
			int row = this.shareTable.getSelectedRow();

			if (row == -1) {
				return;
			}

			Object s_ = this.shareTable.getModel().getValueAt(row, 0);

			if (!(s_ instanceof String)) {
				return;
			}

			String s = (String) s_;

			if ((s != null) && (s.length() > 0)) {
				String old_dir = ShareManager.instance().getShare(s)
						.getFileLocation();
				String dir = DirectoryPicker.pick(MainDialog.getWindow(),
						"Modify", old_dir, "Modify");

				if (dir != null) {
					// ShareManager.instance().addShare(s, dir);

					ShareManager.instance().getShare(s).setLocation(dir);
					ShareManager.instance().notifyObservers();

					this.shareTable.revalidate();
				}
			}
		} else if (acs.equals(MainDialog.ACTION_DELETE_SHARE)) {
			int row = this.shareTable.getSelectedRow();

			if (row == -1) {
				return;
			}

			if (row >= this.shareTable.getModel().getRowCount()) {
				return;
			}

			Object s_ = this.shareTable.getModel().getValueAt(row, 0);

			if (!(s_ instanceof String)) {
				return;
			}

			String s = (String) s_;

			if (s != null) {
				ShareManager.instance().removeShare(s);

				this.shareTable.revalidate();
			}
		} else if (acs.equals(ConfigurationPanel.SET_DOWNLOAD_DIRECTORY)) {
			// FIXME
			
			String dir = FileNameUtils.replaceVariables (Configuration.instance().getFinishedDownloadDir());
			DirectoryUtils.makeSureDirectoryExists(dir);
			
			JOptionPane.showMessageDialog(null, "Download directory is " + dir + "\n\nYou can set a new one.");
			
			dir = DirectoryPicker.pick(MainDialog.getWindow(), "Set Download Directory",
					dir, "Set");

			if (dir != null) {
				Configuration.instance().setFinishedDownloadDir(dir);
				
				JOptionPane.showMessageDialog(null, "Download directory is now" + dir + "\n\nYou need to restart p300.");
			}
		}

	}

	public void addShare() {
		setSelectedComponent(m_sharesPanel);
		actionPerformed(new ActionEvent(this, 0, MainDialog.ACTION_ADD_SHARE));
	}

	public void showDownloadSettings() {
		setSelectedComponent(m_downloadsPanel);
	}

}
