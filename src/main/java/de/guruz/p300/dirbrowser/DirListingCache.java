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

import java.util.HashMap;

/**
 * This cache caches dirlistings of remote p300s
 * @author guruz
 *
 */
public class DirListingCache  implements DirListingReceiver {
	BrowserWidget browserWidget;
	
	HashMap<String,DirListing> listings = new HashMap<String, DirListing> ();
	
	public DirListingCache(BrowserWidget widget) {
		this.browserWidget = widget;
	}

	/**
	 * Called by the browser widget that wants a dirlisting
	 * @param d
	 * @param invalidateCached set to true if always fetch from network
	 */
	public void startFetch(RemoteDir d, boolean invalidateCached) {
		if (d == null)
			return;
		
		synchronized (this) {
			if (this.listings.containsKey(d.getPath()) && !invalidateCached && this.listings.get(d.getPath()).getError() == null) {
				// already in cache, just deliver
				this.browserWidget.dirFetchingDone(d, this.listings.get(d.getPath()));
				//System.out.println ("   (cached)");
				//D.out ("|-- From cache: " + d.getPath());
			} else {
				// start fetching
				// FIXME: We have only dav :)
				//D.out ("|-- Async fetch: " + d.getPath());
				DavDirListingProvider.asyncFetch(this, d, this.browserWidget.getHost ());
			}
		}
		
	}

	/**
	 * Called by a dirlisting provider when it has created a dirlisting
	 * 
	 * we put it into our cache here and then notify the browser widget
	 * @param d
	 * @param dl
	 */
	public void fetchDone (RemoteDir d, DirListing dl) {
		synchronized (this) {
			//System.out.println ("Fetch done for " + d.getPath());
			//D.out ("|-- Async fetch done for: " + d.getPath());
			this.listings.put(d.getPath(), dl);
			this.browserWidget.dirFetchingDone(d, dl);
		}
	}
	
	

}
