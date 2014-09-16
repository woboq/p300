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

import de.guruz.p300.utils.URL;


/**
 * A directory or file on a remote p300 host
 * @author guruz
 *
 */
public abstract class RemoteEntity {
	private String completePath;
	
	/**
	 * Removes double /, replaces \ with /, checks so that there is no trailing /
	 *
	 */
	protected static String cleanupPath (String p) {
		String ret = p.replace('\\', '/');
		ret = ret.replace("//", "/");
		
		if (ret.equals("/"))
			return ret;
		
		if (ret.endsWith("/"))
			return ret.substring(0, ret.length() - 1);
		else
			return ret;
		
	}
	
	public RemoteEntity(String pathname) {
		completePath = cleanupPath (pathname);
	}

	public RemoteEntity(RemoteDir d, String pathname) {
		completePath = d.getPath();
		String toBeConcatenated = cleanupPath (pathname);
		
		if (completePath.endsWith("/") || toBeConcatenated.startsWith("/")) {
			completePath = completePath + toBeConcatenated;
		} else {
			completePath = completePath + '/' + toBeConcatenated;
		}
		
		completePath = cleanupPath (completePath);
	}
	
	public String getPath () {
		return completePath;
	}
	
	/**
	 * Returns the String after the last / or if this is empty then the one before
	 * @return
	 */
	public String getName () {
		if (isRoot ())
			return getPath ();
		
		String path = getPath();
		int slashPos = path.lastIndexOf('/');
		String ret = path.substring(slashPos+1);
		return ret;
	}
	
	public RemoteDir getParent () {
		if (isRoot ())
			return null;
		
		String path = getPath();
		int slashPos = path.lastIndexOf('/');
		String parentPath = path.substring(0, slashPos+1);
		return new RemoteDir (parentPath);
	}
	
	public String toString () {
		return URL.decode (this.getName());
	}
	
	
	public boolean isRoot () {
		return false;
	}
	
	public boolean isDirectory () {
		return false;
	}
	
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof RemoteEntity))
			return false;
		
		RemoteEntity arg = (RemoteEntity) arg0;
		
		if (arg.getPath().equals(this.getPath()))
			return true;
		
		return false;
	}
	
	private long size = 0;
	
	public void setSize (long s) {
		size = s;
	}

	public long getSize() {
		return size;
	}
	

}
