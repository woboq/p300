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
package de.guruz.p300.shares;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import de.guruz.p300.Configuration;
import de.guruz.p300.logging.D;

public class ShareManager extends Observable{
	private static ShareManager i = null;
	
	public static ShareManager instance () {
		synchronized (ShareManager.class) {
			if (ShareManager.i == null) {
				ShareManager.i = new ShareManager ();
			}
		}
		
		return ShareManager.i;
	}
	
	
	private Map<String, Share> shareMap = Collections.synchronizedMap(new HashMap<String, Share>());
	private boolean initialized = false;
		
	public boolean addShare(Share s) {
		this.loadShares();
		if ((s == null) || this.shareMap.containsKey(s.getName())) {
			return false;
		}
		this.shareMap.put(s.getName(), s);
		Configuration.instance().addShare(s);

		this.notifyObservers();
		
		return true;
	}
	
	public boolean addShare(String name, File dir) {
		if ((name == null) || (dir == null) || this.shareMap.containsKey(name)) {
			return false;
		}
		Share sh;
		try {
			sh = new Share(name, dir);
		} catch (InvalidShareException e) {
			D.out("Invalid share. Name: " + e.getShareName() + ", location: " + e.getLocation());
			return false;
		}
		if (!Share.isValidShare(dir)) {
			D.out("New share " + name + " @ " + dir + " not readable or not a directory.");
			return false;
		}
		return this.addShare(sh);
	}
	
	public boolean addShare(String name, String dir) {
		if ((name == null) || (dir == null)|| this.shareMap.containsKey(name)) {
			return false;
		}
		File f = new File(dir);
		if (!Share.isValidShare(f)) {
			D.out("New share " + name + " @ " + dir + " not readable or not a directory.");
			return false;
		}
		return this.addShare(name, f);
	}
	
	public Share getShare(String name) {
		if (name == null) {
			return null;
		}
		this.loadShares();
		return this.shareMap.get(name);
	}
	
	public void removeShare(String name) {
		if (name == null) {
			return;
		}
		this.loadShares();
		if (!this.shareMap.containsKey(name)) {
			return;
		}
		Share s = this.shareMap.get(name);
		this.shareMap.remove(name);
		Configuration.instance().removeShare(s);
		
		this.notifyObservers();
	}
	
	private synchronized void loadShares() {
		if (this.isInitialized()) {
			return;
		}
//		D.out("Loading all shares...");
		String[] shareNames = Configuration.instance().getShareNames();
		if (shareNames == null) {
			return;
		}
		for (String n: shareNames) {
			String dir = Configuration.instance().getDirFromShareName(n);
			Share s = null;
			try {
				s = new Share(n, dir);
			} catch (InvalidShareException e) {
				System.err.println("Share " + n + " is invalid. Removing share.");
				Configuration.instance().removeShare(n);
			}
			if (s != null) {
				this.shareMap.put(n, s);
			}
		}
		this.setInitialized(true);
		
		this.notifyObservers();
	}
	
	public String[] getShareNames() {
		this.loadShares();
		String shares[] =  this.shareMap.keySet().toArray(new String[0]);
		
		java.util.Arrays.sort(shares);
		
		return shares;
	}
	
	public Share[] getShares () {
		this.loadShares();
		
		// there may be convenience methods for doing all this, but toArray() worked bad
		// for guruz in other cases
		Collection<Share> s = this.shareMap.values();
		
		int size = s.size();
		Share[] ret = new Share[size];
		Iterator<Share> it = s.iterator();
		int i = 0;
		while (it.hasNext ()) {
			ret[i] = it.next();
			
			i++;
		}
		
		java.util.Arrays.sort(ret);
		
		return ret;
	}
	
	public boolean isValidShareName(String name) {
		if (name == null) {
			return false;
		}
		this.loadShares();
		return this.shareMap.containsKey(name);
	}

	private synchronized boolean isInitialized() {
		return this.initialized;
	}

	private synchronized void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
}
