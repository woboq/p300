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
package de.guruz.guruztray.implementation;

import de.guruz.guruztray.interfaces.GuruztrayManager;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GuruztrayManagerImplementation implements GuruztrayManager, MouseListener {

	protected SystemTray tray = null;
	protected TrayIcon trayIcon = null;

	public GuruztrayManagerImplementation () throws Exception {
        // workaround to disable system tray in Gnome 3
        //
        // see following comment for an explanation
        // https.//bugzilla.redhat.com/show_bug.cgi?id=683768#c4
        if ("gnome".equals(System.getenv("XDG_SESSION_DESKTOP"))) {
            throw new Exception("System tray support has been disabled for Gnome desktop environment");
        }
		if (!isSupported())
			throw new Exception ("Tray not supported");

		tray = SystemTray.getSystemTray();

		Dimension d = tray.getTrayIconSize();
		//System.out.println ("System Tray icon width=" + d.width + " height=" + d.height);

	}



	public void setTrayIcon(Image i_16x16, Image i_22x22, String tt) throws Exception {
		setTrayIcon (i_16x16, i_22x22, tt, null);
	}

	public void setTrayIcon(Image i_16x16, Image i_22x22, String tt, PopupMenu pm) throws Exception {
		boolean autoSize = false;
		Image i = null;
		Dimension d = tray.getTrayIconSize();
		if (d.height == 16 && d.width == 16)
		{
			i = i_16x16;
			//System.out.println ("Tray 16x16 picked");
			autoSize = true;
		}
		else
		{
			i = i_22x22;
			//System.out.println ("Tray 22x22 picked");
			autoSize = true;
		}

		trayIcon = new TrayIcon (i, tt);
		trayIcon.addMouseListener(this);
		trayIcon.setImageAutoSize(autoSize);

		if (pm != null)
			trayIcon.setPopupMenu(pm);

		tray.add(trayIcon);

		//System.out.println ("Tray real width=" + trayIcon.getSize().width + " height=" + trayIcon.getSize().height);

	}

	public boolean isSupported() {
		return SystemTray.isSupported ();
	}
	public void mouseClicked(MouseEvent arg0) {
		//System.out.println ("Clicked");

	}
	public void mouseEntered(MouseEvent arg0) {
		//System.out.println ("Entered");

	}
	public void mouseExited(MouseEvent arg0) {
		//System.out.println ("Exited");

	}

	/**
	 * If it is the left mouse button, show/hide the associated window
	 */
	public void mousePressed(MouseEvent arg0) {

		if (window != null && (arg0.getButton() == MouseEvent.BUTTON1)) {
			// do not do that on OSX :)
			if (isOSX ())
				return;

			//System.out.println ("Pressed left button");
			boolean v = window.isVisible();
			if (v) {
				window.setVisible(false);
			} else {
				window.setVisible(true);
				window.requestFocus();
				window.toFront ();
			}



		}


	}
	public void mouseReleased(MouseEvent arg0) {
		//System.out.println ("Released");

	}

	protected java.awt.Window window = null;

	public void setAssociatedWindow (java.awt.Window w) {
		window = w;
	}

	protected boolean isOSX() {
		return (System.getProperty("os.name").startsWith("Mac"));
	}


}
