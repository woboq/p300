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
package de.guruz.p300.downloader.directories;

import de.guruz.p300.dirbrowser.DavDirListingProvider;
import de.guruz.p300.dirbrowser.DirListing;
import de.guruz.p300.dirbrowser.DirListingReceiver;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.downloader.DownloadSource;
import de.guruz.p300.downloader.SourceState;

public class RunningWebDavDirectoryDownload implements DirListingReceiver {
	protected DownloadDirectory downloadDirectory;
	protected DownloadSource source;

	public RunningWebDavDirectoryDownload (DownloadDirectory d, DownloadSource s) 
	{
		downloadDirectory = d;
		source = s;	
	}
	
	public void start ()
	{
		DavDirListingProvider.asyncFetch(this, (RemoteDir) source.getRemoteEntity(),
				source.getRemoteHost(), source.getRemotePort());
	}


	public void fetchDone(RemoteDir d, DirListing dl) {
		if (dl.getError() == null)
		{
			// no error
			source.setState(SourceState.OK);
			downloadDirectory.finishedProperly (dl);
		} else {
			// error
			source.setState(SourceState.UNKNOWN);
			source.noRetryTheNextMillisecs(60*1000*3);
			downloadDirectory.finishedWithError (dl.getError());
		}
	}

	protected DownloadSource getSource() {
		return source;
	}

}