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
package de.guruz.p300.windowui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.guruz.p300.windowui.maintree.GenericTreeItem;
import de.guruz.p300.windowui.maintree.MainTree;

public class MainDialogTreeCellRenderer extends DefaultTreeCellRenderer {

	private MainTree m_tree;

	public MainDialogTreeCellRenderer(MainTree tree) {
		m_tree = tree;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		
		GenericTreeItem gti = (GenericTreeItem) value;

		// JLabel caption = new JLabel ( gti.toString());
		//JLabel caption = (JLabel) super.getTreeCellRendererComponent(tree,
		//		value, sel, expanded, leaf, row, false);
		JLabel caption = new JLabel (gti.toString());
		caption.setIcon(gti.getIcon());

		caption.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		if (sel) {
			caption.setForeground(getTextSelectionColor());
			caption.setBackground(getBackgroundSelectionColor());
			caption.setOpaque(true);
		}
		else
		{
			caption.setForeground(getTextNonSelectionColor());
			caption.setBackground(getBackgroundNonSelectionColor());
			caption.setOpaque(false);

		}

		return caption;

	}
}
