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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.affiliates.LeadBulletOpener;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.downloader.DownloadEntity;
import de.guruz.p300.downloader.DownloadsTableModel;
import de.guruz.p300.utils.DirectoryUtils;
import de.guruz.p300.utils.FileNameUtils;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.launchers.FileManagerLaunch;
import de.guruz.p300.windowui.actions.AddDowloadAction;

// FIXME we only display running downloads right now? we need tabs for paused and finished downloads
public class DownloadsPanel extends JTabbedPane implements ListSelectionListener {
	
	private static final String OPEN_DOWNLOAD_SETTINGS = "OpenDownloadSettings";

	private static final String MANUAL_ADD = "manualAdd";

	private static final String OPEN_FINISHED_DOWNLOADS_DIR = "openFinishedDownloadsDir";

	private static final String PRODUCTS = "products";

	private static final String PAUSE = "pause";

	private static final String STOP = "stop";

	public static final long serialVersionUID = 0l;
	
	JTable m_activeDownloadsTable;
	
	JTable m_finishedDownloadsTable;
	
	DefaultTableModel m_finishedDownloadsTableTableModel;
	
	ActionListener myActionListener = new ActionListener ()
	{
		public void actionPerformed(ActionEvent ae) {
			String ac = ae.getActionCommand();
			if (ac.equals(PAUSE))
			{
				JOptionPane.showMessageDialog(MainDialog.getWindow(), "Sorry, not possible yet");
			}
			else if (ac.equals(STOP))
			{
				JOptionPane.showMessageDialog(MainDialog.getWindow(), "Sorry, not possible yet. Look on http://p300.eu/faq/ -> How can I abort a download?");
			}
			
			else if (ac.equals(DownloadsPanel.OPEN_DOWNLOAD_SETTINGS))
			{
				ConfigurationPanel cp = MainDialog.instance.configurationPanel;
				MainDialog.instance.showSubWindow(null, "Configuration", cp, true);
				cp.showDownloadSettings ();
			}
			else if (ac.equals(OPEN_FINISHED_DOWNLOADS_DIR))
			{
				//JOptionPane.showMessageDialog(MainDialog.getWindow(), "TBD");
				String dd = FileNameUtils.replaceVariables (Configuration.instance().getFinishedDownloadDir());
				DirectoryUtils.makeSureDirectoryExists(dd);
				FileManagerLaunch.openDirectory(dd);
			}
		}
	};

	private JButton productsButton;
	
	public Object getCurrentClickedItem() {
		try 
		{
			return this.m_activeDownloadsTable.getValueAt(((this.m_activeDownloadsTable.getSelectedRows())[0]), 1);
		} catch (Exception e) {
			return null;
		}
	}
	
	public DownloadsPanel() {
		super();
		
		JPanel activeDownloads = new JPanel (new BorderLayout ());

		JPanel topPanel = new JPanel ();
		JButton manualAddButton = new JButton (new AddDowloadAction ());
		manualAddButton.setToolTipText("Add manually");
		manualAddButton.setIcon(IconChooser.iconImageFromResource("22x22/list-add.png"));
		manualAddButton.setText("");
		topPanel.add(manualAddButton);
		JButton pauseButton = new JButton ();
		pauseButton.setToolTipText("Pause");
		pauseButton.setIcon(IconChooser.iconImageFromResource("22x22/media-playback-pause.png"));
		pauseButton.setActionCommand(PAUSE);
		pauseButton.addActionListener(myActionListener);
		topPanel.add(pauseButton);
		JButton stopButton = new JButton ();
		stopButton.setToolTipText("Stop");
		stopButton.setIcon(IconChooser.iconImageFromResource("22x22/list-remove.png"));
		stopButton.setActionCommand(STOP);
		stopButton.addActionListener(myActionListener);
		topPanel.add(stopButton);
		productsButton = new JButton ();
		productsButton.setToolTipText("Products");
		productsButton.setIcon(IconChooser.iconImageFromResource("22x22/face-monkey.png"));
		productsButton.setActionCommand(PRODUCTS);
		productsButton.addActionListener(myActionListener);
		productsButton.setEnabled(false);
		//topPanel.add(productsButton);
		
		activeDownloads.add(topPanel, 
				BorderLayout.NORTH);
		
		
		m_activeDownloadsTable = new JTable (DownloadsTableModel.instance ());
		m_activeDownloadsTable.setShowGrid(false);
		m_activeDownloadsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_activeDownloadsTable.getSelectionModel().addListSelectionListener(this);
		m_activeDownloadsTable.getColumnModel().getColumn(0).setMinWidth(40);
		m_activeDownloadsTable.getColumnModel().getColumn(0).setMaxWidth(40);
		m_activeDownloadsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		m_activeDownloadsTable.getColumnModel().getColumn(2).setMinWidth(50);
		m_activeDownloadsTable.getColumnModel().getColumn(2).setMaxWidth(200);
		m_activeDownloadsTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		m_activeDownloadsTable.setRowHeight(22);
		activeDownloads.add(new JScrollPane (m_activeDownloadsTable), BorderLayout.CENTER);
		
		
		
		JPanel finishedDownloads = new JPanel (new BorderLayout ());
		
		JButton openFinishedDownloadsDirButton = new JButton ();
		openFinishedDownloadsDirButton.setIcon(IconChooser.iconImageFromResource("22x22/system-file-manager.png"));
		openFinishedDownloadsDirButton.setToolTipText("Finished Downloads Directory");
		openFinishedDownloadsDirButton.addActionListener(myActionListener);
		openFinishedDownloadsDirButton.setActionCommand(OPEN_FINISHED_DOWNLOADS_DIR);
		
		JPanel upperFinishedPanel = new JPanel ();
		upperFinishedPanel.add(openFinishedDownloadsDirButton);
		
		
		JButton downloadSettingsButton = new JButton ();
		downloadSettingsButton.setToolTipText("Settings");
		downloadSettingsButton.setIcon(IconChooser.iconImageFromResource("22x22/applications-system.png"));
		downloadSettingsButton.setActionCommand(DownloadsPanel.OPEN_DOWNLOAD_SETTINGS);
		downloadSettingsButton.addActionListener(myActionListener);
		upperFinishedPanel.add(downloadSettingsButton);
		
		finishedDownloads.add (upperFinishedPanel, BorderLayout.NORTH);

		
		m_finishedDownloadsTable = new JTable() {
			public Class<?> getColumnClass(int column) {
				if (getValueAt(0, column) == null)
					return String.class;
				else
					return getValueAt(0, column).getClass();
			}
		};
		m_finishedDownloadsTableTableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		m_finishedDownloadsTableTableModel.addColumn("");
		m_finishedDownloadsTableTableModel.addColumn("File");
//		m_finishedDownloadsTableTableModel.addColumn("");

		m_finishedDownloadsTable.setShowGrid(false);
		m_finishedDownloadsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_finishedDownloadsTable.getSelectionModel().addListSelectionListener(this);
		m_finishedDownloadsTable.setModel(m_finishedDownloadsTableTableModel);
		
		
		m_finishedDownloadsTable.getColumnModel().getColumn(0).setMinWidth(40);
		m_finishedDownloadsTable.getColumnModel().getColumn(0).setMaxWidth(40);
		m_finishedDownloadsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
//		m_finishedDownloadsTable.getColumnModel().getColumn(2).setMinWidth(50);
//		m_finishedDownloadsTable.getColumnModel().getColumn(2).setMaxWidth(200);
//		m_finishedDownloadsTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		m_finishedDownloadsTable.setRowHeight(22);
		finishedDownloads.add(new JScrollPane (m_finishedDownloadsTable), BorderLayout.CENTER);
		
		
		this.addTab ("Active", null, activeDownloads);
		this.addTab ("Finished", null, finishedDownloads);
		
		// this should update the UI as soon as we get displayed
		this.addComponentListener(new ComponentListener () {

			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void componentShown(ComponentEvent e) {
				MainDialog.downloadManager.updateDownloadsTableModel();
				
			} });
	}

	public void valueChanged(ListSelectionEvent e) {
		productsButton.setEnabled(getCurrentClickedItem() != null);
	}

	public void addFinishedDownload(DownloadEntity de) {
		m_finishedDownloadsTableTableModel.addRow(new Object []{de.getUIRow().getDownloadIcon(), de.getUIRow().getDisplayName()});
		
	}

}
