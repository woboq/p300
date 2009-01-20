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

// This class gets notified on each new file to index
// It returns the file name as XML "name" attribute to the IndexerObservable
// It also counts the amount of files indexed
public class FileNameObserver extends FileInfo2XMLObserver {
	public String shareLocation;
	public int fileCount;
	
	public void update(Observable o, Object arg) {
		if (!((o instanceof IndexerObservable) && (arg instanceof File))) {
			return;
		}
		File f = (File)arg;
		String fileLocation = f.getAbsolutePath();
		if (fileLocation.startsWith(this.shareLocation)) {
			fileLocation = fileLocation.substring(this.shareLocation.length());
		}
		if (fileLocation.startsWith(File.pathSeparator)) {
			fileLocation = fileLocation.substring(File.pathSeparator.length());
		}
		String xmlString = "name=\"" + Common.encodeForXMLAttribute(fileLocation) + '"';
		((IndexerObservable)o).xmlStringList.add(xmlString);
		this.fileCount++;
	}
	
	public FileNameObserver() {
		this.fileCount = 0;
	}
}