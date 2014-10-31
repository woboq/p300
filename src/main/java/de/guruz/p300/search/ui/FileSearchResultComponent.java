package de.guruz.p300.search.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import de.guruz.p300.dirbrowser.DownloadDirAction;
import de.guruz.p300.dirbrowser.DownloadEntityAction;
import de.guruz.p300.dirbrowser.DownloadFileAction;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.URL;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;
import de.guruz.p300.webdav.search.client.SearchResultCollector;
import de.guruz.p300.windowui.actions.BrowseHostAction;

public class FileSearchResultComponent extends JPanel implements
		SearchResultCollector {

	JTable m_resultsTable;

	JLabel m_msgLabel;

	JButton m_downloadButton;

	JButton m_searchWithBrowserButton;
	
	JButton m_browseButton;

	Host m_host;

	String m_searchWords;

	public FileSearchResultComponent(Host h, String searchWords) {
		m_host = h;
		m_searchWords = searchWords;

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		
		//
		// Download file or dir
		//
		m_downloadButton = new JButton();
		m_downloadButton.setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				downloadSelectedEntity();
			}

		});
		m_downloadButton.getAction().putValue(Action.SMALL_ICON,
				IconChooser.getDownloadIcon());
		m_downloadButton.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Download");
		m_downloadButton.setEnabled(false);
		topPanel.add(m_downloadButton);

		
		// 
		// Browse dir
		// 
		m_browseButton = new JButton();
		m_browseButton.setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				browseSelectedEntity();
			}


		});
		m_browseButton.getAction().putValue(Action.SMALL_ICON,
				IconChooser.iconImageFromResource("22x22/folder-open.png"));
		m_browseButton.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Browse");
		m_browseButton.setEnabled(false);
		topPanel.add(m_browseButton);
		
		
		
		
		//
		// Search in browser
		//
		m_searchWithBrowserButton = new JButton();
		m_searchWithBrowserButton.setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				String url = m_host.toURL() + "/search?searchString="
						+ URL.encode(m_searchWords);

				BareBonesBrowserLaunch.openURL(url);
			}

		});
		m_searchWithBrowserButton.getAction().putValue(Action.SMALL_ICON,
				IconChooser.iconImageFromResource("22x22/applications-internet.png"));
		m_searchWithBrowserButton.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Open Results In Browsr");
		m_searchWithBrowserButton.setEnabled(false);
		topPanel.add(m_searchWithBrowserButton);
		
		
		

		add(topPanel, BorderLayout.NORTH);

		m_resultsTable = new JTable() {
			public Class<?> getColumnClass(int column) {
				if (getValueAt(0, column) == null)
					return String.class;
				else
					return getValueAt(0, column).getClass();
			}
		};
		m_resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener () {

			public void valueChanged(ListSelectionEvent e) {
				m_browseButton.setEnabled(m_resultsTable.getSelectedRow() != -1);
				m_downloadButton.setEnabled(m_resultsTable.getSelectedRow() != -1);
				m_searchWithBrowserButton.setEnabled(m_resultsTable.getSelectedRow() != -1);
			}});
		m_resultsTable.setShowGrid(false);
		m_resultsTable
				.setRowHeight(Math.max(22, m_resultsTable.getRowHeight()));
		m_resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		add(new JScrollPane(m_resultsTable), BorderLayout.CENTER);
		m_resultsTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					downloadSelectedEntity();
				}
			}
		});

		m_msgLabel = new JLabel("Searching ...");
		m_msgLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		add(m_msgLabel, BorderLayout.SOUTH);
	}

	protected void downloadSelectedEntity() {
		int idx = m_resultsTable.getSelectedRow();

		if (idx != -1
				&& m_resultsTable.getModel() != null
				&& m_resultsTable.getModel() instanceof FileSearchResultTableModel) {
			FileSearchResultTableModel m = (FileSearchResultTableModel) m_resultsTable
					.getModel();
			RemoteEntity e = m.getEntity(idx);
			D.out("Will load " + e.toString());

			DownloadEntityAction action;
			if (e.isDirectory()) {
				action = new DownloadDirAction(m_host, (RemoteDir) e, null);
			} else {
				action = new DownloadFileAction(null, m_host, (RemoteFile) e, e
						.getSize(), null);
			}
			action.actionPerformed(null);
		}
	}
	

	protected void browseSelectedEntity() {
		int idx = m_resultsTable.getSelectedRow();

		if (idx != -1
				&& m_resultsTable.getModel() != null
				&& m_resultsTable.getModel() instanceof FileSearchResultTableModel) {
			FileSearchResultTableModel m = (FileSearchResultTableModel) m_resultsTable
					.getModel();
			RemoteEntity e = m.getEntity(idx);
			D.out("Will browse " + e.toString());

			BrowseHostAction action;
			if (e.isDirectory()) {
				action = new BrowseHostAction (m_host,  (RemoteDir) e);
			} else {
				action = new BrowseHostAction (m_host, e.getParent());
			}
			action.actionPerformed(null);
		}
	}
	

	public void hasError(Host host, String msg) {
		m_msgLabel.setText("An error occured: " + msg);

	}

	public void newResult(Host host, List<RemoteEntity> resultList) {
		if (resultList.size() == 1)
			m_msgLabel.setText("1 result");
		else
			m_msgLabel.setText(resultList.size() + " results");
		m_resultsTable.setModel(new FileSearchResultTableModel(resultList));

		m_resultsTable.setRowHeight(22);

		TableColumn firstColumn = m_resultsTable.getColumnModel().getColumn(0);
		int firstColumnWidth = IconChooser.getGuiIconWidth();
		firstColumn.setMinWidth((int) (firstColumnWidth * 1.3));
		firstColumn.setMaxWidth((int) (firstColumnWidth * 1.3));
		firstColumn.setPreferredWidth((int) (firstColumnWidth * 1.3));
		// TableColumn secondColumn =
		// m_resultsTable.getColumnModel().getColumn(1);
		// firstColumn.setPreferredWidth((int) (150));

		m_resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	}

	public void noResult(Host host) {
		m_msgLabel.setText("Sorry, no results");

	}
}
