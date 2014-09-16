package de.guruz.p300.search.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.utils.IconChooser;

public class FileSearchResultTableModel extends AbstractTableModel {

	@Override
	public String getColumnName(int c) {
		if (c == 0)
		{
			return "";
		}
		else if (c == 1)
		{
			return "Filename";
		}
		else if (c == 2)
		{
			return "Path";
		}

		return null;
	}

	protected List<RemoteEntity> m_entities;
	
	public FileSearchResultTableModel (List<RemoteEntity> resultList)
	{
		m_entities = resultList;
	}
	
	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return m_entities.size();
	}

	public Object getValueAt(int r, int c) {
		if (c == 0)
		{
			RemoteEntity e = m_entities.get(r);
			return IconChooser.getImageIconFromEntity(e);
		}
		else if (c == 1)
		{
			return m_entities.get(r).getName();
		}
		else if (c == 2)
		{
			return m_entities.get(r).getPath();
		}

		return null;
	}
	
	public Class<?> getColumnClass(int column) {
		return getValueAt(0, column).getClass();
	}

	public RemoteEntity getEntity(int idx) {
		return m_entities.get(idx);
	}

}
