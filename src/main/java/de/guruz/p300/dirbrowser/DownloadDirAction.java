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

import java.awt.event.ActionEvent;

import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.windowui.actions.ShowDownloadsAction;

public class DownloadDirAction extends DownloadEntityAction {
	private static final long serialVersionUID = 1L;
	protected Host m_host;
	protected RemoteDir m_remoteDir;
	protected DownloadStartedListener m_listener;
	
	public DownloadDirAction (Host h, RemoteDir rd, DownloadStartedListener listener) 
	{
		m_host = h;
		m_remoteDir = rd;
		m_listener = listener;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		MainDialog.downloadManager.startDownload(m_host.getLocationsAsList(), m_remoteDir);
		MainDialog.downloadManager.invalidateUi ();
		
		if (m_listener != null)
		{
			m_listener.downloadWasStarted(m_host, m_remoteDir);
		}
		
		if (MainDialog.instance != null)
		{
			new ShowDownloadsAction (false).actionPerformed(null);
		}
	}

}
