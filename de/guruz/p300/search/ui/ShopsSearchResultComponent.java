package de.guruz.p300.search.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import de.guruz.p300.affiliates.AmazonCaUrlOpenData;
import de.guruz.p300.affiliates.AmazonComUrlOpenData;
import de.guruz.p300.affiliates.AmazonDeUrlOpenData;
import de.guruz.p300.affiliates.AmazonFrUrlOpenData;
import de.guruz.p300.affiliates.AmazonJpUrlOpenData;
import de.guruz.p300.affiliates.AmazonUkUrlOpenData;
import de.guruz.p300.affiliates.BuecherDeUrlOpenData;
import de.guruz.p300.affiliates.BuyComUrlOpenData;
import de.guruz.p300.affiliates.EbayDeUrlOpenData;
import de.guruz.p300.affiliates.ITunesBeUrlOpenData;
import de.guruz.p300.affiliates.ITunesComUrlOpenData;
import de.guruz.p300.affiliates.ITunesDeUrlOpenData;
import de.guruz.p300.affiliates.LeadBulletUrlOpenData;
import de.guruz.p300.affiliates.UrlOpenData;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;

public class ShopsSearchResultComponent extends JPanel {

	JButton m_openButton;

	JTable m_shopsTable;

	JLabel m_msgLabel;

	DefaultTableModel m_shopsTableModel;

	String m_searchWords;

	public ShopsSearchResultComponent(String words) {
		// add (new JLabel ("Shops for " + words));

		m_searchWords = words;

		m_shopsTableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		m_shopsTableModel.setRowCount(0);
		m_shopsTableModel.setColumnCount(0);
		m_shopsTableModel.addColumn("");
		m_shopsTableModel.addColumn("Name");
		m_shopsTableModel.addRow(new Object[] {
				IconChooser.getSharemonkeyIcon(),
				new LeadBulletUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { IconChooser.getAmazonIcon(),
				new AmazonComUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { IconChooser.getAmazonIcon(),
				new AmazonCaUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { IconChooser.getAmazonIcon(),
				new AmazonUkUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { IconChooser.getAmazonIcon(),
				new AmazonDeUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { IconChooser.getAmazonIcon(),
				new AmazonFrUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { IconChooser.getAmazonIcon(),
				new AmazonJpUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { IconChooser.getBuecherDeIcon(),
				new BuecherDeUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { null,
				new BuyComUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { null,
				new EbayDeUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { null,
				new ITunesComUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { null,
				new ITunesDeUrlOpenData(m_searchWords) });
		m_shopsTableModel.addRow(new Object[] { null,
				new ITunesBeUrlOpenData(m_searchWords) });
		

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();

		m_openButton = new JButton();
		m_openButton.setAction(new AbstractAction () {
			public void actionPerformed(ActionEvent e) {
				openSelectedShop();
			}
			
		});
		m_openButton.getAction().putValue(Action.SMALL_ICON, IconChooser
				.iconImageFromResource("22x22/internet-web-browser.png"));
		m_openButton.getAction().putValue(Action.SHORT_DESCRIPTION, "Open");
		topPanel.add(m_openButton);
		m_openButton.setEnabled(false);
		
		add(topPanel, BorderLayout.NORTH);

		m_shopsTable = new JTable() {
			public Class<?> getColumnClass(int column) {
				if (getValueAt(0, column) == null)
					return String.class;
				else
					return getValueAt(0, column).getClass();
			}
		};
		m_shopsTable.addMouseListener(new MouseAdapter(){
		     public void mouseClicked(MouseEvent e){
		      if (e.getClickCount() == 2){
		         openSelectedShop();
		         }
		      }
		     } );
		
		
		m_shopsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_shopsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener () {

			public void valueChanged(ListSelectionEvent e) {
				m_openButton.setEnabled(m_shopsTable.getSelectedRow() != -1);
			}});
		
		m_shopsTable.setShowGrid(false);
		m_shopsTable.setRowHeight(Math.max(22, m_shopsTable.getRowHeight()));
		m_shopsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		m_shopsTable.setModel(m_shopsTableModel);
		TableColumn firstColumn = m_shopsTable.getColumnModel().getColumn(0);
		firstColumn.setMinWidth((int) (IconChooser.getGuiIconWidth() * 1.3));
		firstColumn.setMaxWidth((int) (IconChooser.getGuiIconWidth() * 1.3));
		firstColumn
				.setPreferredWidth((int) (IconChooser.getGuiIconWidth() * 1.3));
		TableColumn secondColumn = m_shopsTable.getColumnModel().getColumn(1);

		add(new JScrollPane(m_shopsTable), BorderLayout.CENTER);

		m_msgLabel = new JLabel(m_shopsTableModel.getRowCount() + " shops");
		m_msgLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		add(m_msgLabel, BorderLayout.SOUTH);
	}

	public void openSelectedShop() {
		int idx = m_shopsTable.getSelectedRow();

		if (idx != -1) {
			if (m_shopsTableModel.getValueAt(idx, 1) instanceof UrlOpenData) {
				UrlOpenData uod = (UrlOpenData) m_shopsTableModel.getValueAt(
						idx, 1);
				BareBonesBrowserLaunch.openURL(uod.getURL());
			}
		}
	}

}
