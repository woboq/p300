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
package de.guruz.p300.search.dataextractor;

import java.io.File;
import java.util.Observable;

import de.guruz.p300.search.Common;

//This class gets notified on each new file to index
//It returns the file size as XML "size" attribute to the IndexerObservable
//It also counts the total size of all files indexed
public class FileSizeObserver extends FileInfo2XMLObserver {
	public long sizeCount;
	
	public void update(Observable o, Object arg) {
		if (!(arg instanceof File)) {
			return;
		}
		File f = (File)arg;
		String xmlString = "";
		if (!f.isDirectory()) {
			long fileSize = f.length();
			xmlString = "size=\"" + Common.encodeForXMLAttribute(String.valueOf(fileSize)) + '"';
			this.sizeCount += fileSize;
		}
		((IndexerObservable)o).xmlStringList.add(xmlString);
	}
	
	public FileSizeObserver() {
		this.sizeCount = 0;
	}
}