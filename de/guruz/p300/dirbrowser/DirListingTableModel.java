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

import javax.swing.table.AbstractTableModel;

import de.guruz.p300.utils.IconChooser;


public class DirListingTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5979587745059126251L;
	DirListing dirListing;
	
	public DirListingTableModel (DirListing d)
	{
		dirListing = d;
	}
	
	@Override
	public String getColumnName(int col) {
		if (col == 0) {
			return "";
		} else if (col == 1) {
			return "Name";
		} else {
			return "???";
		}

	}

	public int getRowCount() {
		return dirListing.getEntities ().size();
	}

	public int getColumnCount() {
		return 2;
	}

	
	public Object getValueAt(int row, int col) {
		if (col == 1) {
			return dirListing.getEntities ().get(row);
		} else {
			// FIXME: icons may be too large?
			// the cell with the icon
			RemoteEntity re = dirListing.getEntities ().get(row);
			return IconChooser.getImageIconFromEntity(re);
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	


}
