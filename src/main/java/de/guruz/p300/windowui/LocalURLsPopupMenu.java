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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.guruz.p300.windowui.actions.CopyToClipboardAction;

public class LocalURLsPopupMenu extends JPopupMenu implements
		PopupMenuListener {
	private static final long serialVersionUID = -5016740035615589056L;

	protected LocalURLsPopupMenu() {
		super("Copy own URL to clipboard");
		this.addPopupMenuListener(this);
	}

	private static LocalURLsPopupMenu i = null;

	public static synchronized LocalURLsPopupMenu instance() {
		if (LocalURLsPopupMenu.i == null) {
			LocalURLsPopupMenu.i = new LocalURLsPopupMenu();
		}

		return LocalURLsPopupMenu.i;
	}

	public void popupMenuCanceled(PopupMenuEvent arg0) {
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
		this.removeAll();

		addItems(this);
	}



	public static void addItems(Object menu) {
		String urls[] = de.guruz.p300.MainDialog.listenThread
				.getKnownLocalURLs();

		for (String url : urls) {
			CopyToClipboardAction item = new CopyToClipboardAction(url, url);
			

			if (menu instanceof JPopupMenu)
			{
				((JPopupMenu)menu).add(item);
			} 
			else if (menu instanceof JMenu)
			{
				((JMenu)menu).add(item);
			}
		}
	}
}
