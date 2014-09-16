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
package de.guruz.guruztray.interfaces;

import java.awt.Image;
import java.awt.PopupMenu;

public interface GuruztrayManager {
	/**
	 * returns true if we have a systray on this platform
	 * @return
	 */
	public boolean isSupported ();
	
	/**
	 * In windows the size is 16x16, in OS X 22x22
	 * @param i_22x22 TODO
	 */
	public void setTrayIcon (Image i_16x16, Image i_22x22, String tt) throws Exception;
	public void setTrayIcon (Image i_16x16, Image i_22x22, String tt, PopupMenu pm) throws Exception;

	/**
	 * sets the associated window that is shown/hidden on click
	 * @param w
	 */
	public void setAssociatedWindow(java.awt.Window w);
}
