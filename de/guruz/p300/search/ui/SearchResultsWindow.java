package de.guruz.p300.search.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import de.guruz.p300.MainDialog;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.webdav.search.client.SearchResultCollector;
import de.guruz.p300.webdav.search.client.WebDAVSearchClient;

public class SearchResultsWindow extends JComponent {
	JTable m_resultProvidersTable;

	DefaultTableModel m_resultProvidersTableModel;

	String m_searchWords;

	Map<Host, FileSearchResultComponent> m_fileSearchResultComponents = new HashMap<Host, FileSearchResultComponent>();

	ShopsSearchResultComponent m_shopsSearchResultComponent;

	JPanel m_rightComponent;

	protected JSplitPane m_splitPane;

	public SearchResultsWindow(String s) {
		super();
		setLayout(new BorderLayout());
		m_searchWords = s;

		m_resultProvidersTable = new JTable() {
			public Class<?> getColumnClass(int column) {
				if (getValueAt(0, column) == null)
					return String.class;
				else
					return getValueAt(0, column).getClass();
			}

			public void valueChanged(ListSelectionEvent e) {
				openSelectedItem();
			}
		};
		m_resultProvidersTableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		m_resultProvidersTable
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_resultProvidersTable.setShowGrid(false);
		m_resultProvidersTable.setRowHeight(Math.max(22, m_resultProvidersTable
				.getRowHeight()));
		m_resultProvidersTable
				.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		
		JScrollPane resultProvidersScrollPane = new JScrollPane(
				m_resultProvidersTable);
		//resultProvidersScrollPane.setMinimumSize(new Dimension(200, 200));
		resultProvidersScrollPane.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		
		m_rightComponent = new JPanel(new BorderLayout ());
		m_rightComponent.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		//m_rightComponent.setMinimumSize(new Dimension(400, 200));
		//m_rightComponent.add(new JLabel("Please select on the left"), BorderLayout.CENTER);

		m_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		m_splitPane.setLeftComponent(resultProvidersScrollPane);
		m_splitPane.setRightComponent(m_rightComponent);
		
		// m_splitPane.setDividerLocation(-1);
		//m_splitPane.setResizeWeight(0.0);

		add(m_splitPane);
		// add(new JLabel("Double-click to open results in a web browser"),
		// BorderLayout.SOUTH);
	}

	public String getTitle() {
		return "Search Results for: " + m_searchWords;
	}

	public void asyncSearchStartAllHosts() {
		asyncSearchStart(null);
	}
	
	public void asyncSearchStartOneHost(Host h) {
		asyncSearchStart(h);
	}
	
	protected void asyncSearchStart(final Host h) {
		D.out("Searching for " + m_searchWords);

		Runnable r = new Runnable() {

			public void run() {
				m_resultProvidersTableModel.setRowCount(0);
				m_resultProvidersTableModel.setColumnCount(0);
				m_resultProvidersTableModel.addColumn("");
				m_resultProvidersTableModel.addColumn("#");
				m_resultProvidersTableModel.addColumn("Source");

				

				List<Host> searchedHosts = new ArrayList<Host>();
				
				List<Host> hosts;
				if (h != null)
				{
					hosts = Collections.singletonList(h);
				}
				else
				{
					hosts = Arrays.asList (MainDialog.hostMap.getHosts());
				}
				
				for (Host h : hosts) {
					if (h.seemsOnline()) {
						searchedHosts.add(h);
						FileSearchResultComponent fsrc = new FileSearchResultComponent(h, m_searchWords);
						m_fileSearchResultComponents.put(h, fsrc);
						m_resultProvidersTableModel.addRow(new Object[] {
								IconChooser.getOnlineHostImageIcon(), "?", h });
						
						// FIXME for testing
						//fsrc.newResult(h, Collections.singletonList ((RemoteEntity) new RemoteFile ("/crap/" + h.getDisplayName())));
					}
				}

				// The demultiplexer routes the search results to the correct components
				SearchResultCollector searchResultCollectorDemultiplexer = new SearchResultCollector ()
				{

					public void hasError(Host host, String msg) {
						if (m_fileSearchResultComponents.containsKey(host))
							m_fileSearchResultComponents.get(host).hasError(host, msg);
						setResultCountCellValue (host, "X");
					}

					public void newResult(Host host,
							List<RemoteEntity> resultList) {
						if (m_fileSearchResultComponents.containsKey(host))
							m_fileSearchResultComponents.get(host).newResult(host, resultList);
						setResultCountCellValue (host,""+resultList.size());
					}

					public void noResult(Host host) {
						if (m_fileSearchResultComponents.containsKey(host))
							m_fileSearchResultComponents.get(host).noResult(host);
						setResultCountCellValue (host,"0");
					}


				};
				// This launches the search
				WebDAVSearchClient.searchSpecificHosts(m_searchWords, Collections.singletonList(searchResultCollectorDemultiplexer), searchedHosts);				
				
				



				m_shopsSearchResultComponent = new ShopsSearchResultComponent(
						m_searchWords);
				m_resultProvidersTableModel.addRow(new Object[] { null, "",
						"Shopping" });

				m_resultProvidersTable.setModel(m_resultProvidersTableModel);

				TableColumn firstColumn = m_resultProvidersTable
						.getColumnModel().getColumn(0);
				firstColumn.setMinWidth((int) (IconChooser.getGuiIconWidth ()*1.3));
				firstColumn.setMaxWidth((int) (IconChooser.getGuiIconWidth ()*1.3));
				firstColumn.setPreferredWidth((int) (IconChooser.getGuiIconWidth ()*1.3));
				
				
				TableColumn secondColumn = m_resultProvidersTable
						.getColumnModel().getColumn(1);
				secondColumn.setMinWidth((int) (IconChooser.getGuiIconWidth ()*2.3));
				secondColumn.setMaxWidth((int) (IconChooser.getGuiIconWidth ()*2.3));
				secondColumn.setPreferredWidth((int) (IconChooser.getGuiIconWidth ()*2.3));
				
				
				m_splitPane.setDividerLocation(220);
			}

		};

		SwingUtilities.invokeLater(r);
	}

	public void openSelectedItem() {
		int row = m_resultProvidersTable.getSelectedRow();

		if (row != -1) {
			m_rightComponent.setVisible(false);
			
			Object o = m_resultProvidersTableModel.getValueAt(row, 2);
			if (o instanceof String) {
				if (m_shopsSearchResultComponent != null) {
					m_rightComponent.removeAll();
					m_rightComponent.add(m_shopsSearchResultComponent, BorderLayout.CENTER);
				}
			} else if (o instanceof Host) {
				if (m_fileSearchResultComponents.containsKey(o)) {
					m_rightComponent.removeAll();
					m_rightComponent.add(m_fileSearchResultComponents.get(o), BorderLayout.CENTER);
				}

			}
			
			
			m_rightComponent.layout();
			m_rightComponent.invalidate();
			m_rightComponent.repaint();
			m_resultProvidersTable.repaint();
			m_rightComponent.setVisible(true);
		}
	}
	
	private void setResultCountCellValue(Host host,
			String string) {
		for (int i = 0; i < m_resultProvidersTableModel.getRowCount(); i++)
		{
			Object o = m_resultProvidersTableModel.getValueAt(i, 2);
			if (o == host)
			{
				m_resultProvidersTableModel.setValueAt(string, i, 1);
			}
		}
		
	}

}
