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
package de.guruz.p300.windowui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import de.guruz.p300.shares.Share;
import de.guruz.p300.shares.ShareManager;

/**
 * The table model for listing the current shares in the UI configuration
 * 
 * @author guruz
 * 
 */
public class ShareTableModel extends AbstractTableModel implements Observer {
	public ShareTableModel() {
		super();
		ShareManager.instance().addObserver(this);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getColumnName(int col) {
		if (col == 0) {
			return "Share";
		} else {
			return "Directory";
		}
	}

	public int getRowCount() {
		return ShareManager.instance().getShares().length;
	}

	public int getColumnCount() {
		return 2;
	}

	public Object getValueAt(int row, int col) {
		Share shares[] = ShareManager.instance().getShares();

		if (row >= shares.length) {
			return null;
		}

		if (col == 0) {
			return shares[row].getName();

		} else {
			return shares[row].getFileLocation();
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	protected static ShareTableModel i = null;

	public static synchronized ShareTableModel instance() {
		if (ShareTableModel.i == null) {
			synchronized (ShareTableModel.class) {
				ShareTableModel.i = new ShareTableModel();
			}
		}

		return ShareTableModel.i;
	}

	/**
	 * Called when our data has changed. we notify the UI about this
	 */
	public void update(Observable arg0, Object arg1) {
		this.fireTableDataChanged();
		this.fireTableStructureChanged();

	}

	// public void setValueAt(Object value, int row, int col) {
	// rowData[row][col] = value;
	// fireTableCellUpdated(row, col);
	// }

}
