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
import java.io.IOException;

import de.guruz.p300.logging.D;

public class Share implements Comparable {
	private File location;
	private String name;
	
	public Share(String n, File loc) throws InvalidShareException {
		this.setName(n);
		if (!Share.isValidShare(loc)) {
			throw new InvalidShareException(n, loc);
		}
		this.setLocation(loc);
	}
	
	public Share(String n, String dir) throws InvalidShareException {
		File f = new File(dir);
		if (!Share.isValidShare(f)) {
			throw new InvalidShareException(n, f);
		}
		this.setName(n);
		this.setLocation(f);
	}

	/**
	 * Returns the directory as a File object
	 */
	public File getLocation() {
		return this.location;
	}
	
	public String getFileLocation() {
		String p = "";
		File f = this.getLocation();
		if (f == null) {
			ShareManager.instance().removeShare(this.getName());
			return "";
		}
		try {
			p = this.getLocation().getCanonicalPath();
		} catch (IOException ioe) {
			D.out("Couldn't retrieve canonical path of share " + this.getName() + ": " + ioe.getLocalizedMessage());
		}
		return p;
	}

	public void setLocation(File location) {
		this.location = location;
	}
	
	public void setLocation(String location) {
		this.location = new File (location);
	}

	public String getName() {
		return this.name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public static boolean isValidShare(File f) {
		return f.canRead() && f.exists() && f.isDirectory();
	}
	
	public String toString () {
		return getName ();
	}

	public int compareTo(Object arg0) {
		return toString ().compareTo(arg0.toString());
	}

}
