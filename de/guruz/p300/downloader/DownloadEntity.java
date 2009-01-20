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
package de.guruz.p300.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public abstract class DownloadEntity {
	public static final int INFORMATION_FILE_VERSION = 2;
	
	protected DownloadState state = DownloadState.NEW;
	
	protected DownloadManager manager = null;
	
	protected DownloaderEntryUIRow uiRow;

	public abstract File getInformationFile();

	public abstract void merge();

	public abstract void startLoadingIfPossible();
	
	protected abstract void updateUiRow ();

	public DownloadState getState() {
		return state;
	}
	
	public DownloadEntity (DownloadManager m) {
		manager = m;
		uiRow = new DownloaderEntryUIRow ();
	}
	
	public DownloadManager getManager () {
		return manager;
	}

	public static FileType getType(File informationFile) {
		try {
			Properties props = new Properties ();
			FileInputStream fis = new FileInputStream (informationFile);
			props.loadFromXML(fis);
			fis.close();
			
			String type = props.getProperty("file_type", "FILE");
			
			FileType t = FileType.valueOf(type);
			
			return t;
		} catch (Exception e) {
			return FileType.UNKNOWN;
		}
	}
	
	public static boolean isValidInformationFile(File f) {
		try {
			Properties props = new Properties ();
			FileInputStream fis = new FileInputStream (f);
			props.loadFromXML(fis);
			fis.close();
			
			String type = props.getProperty("information_file_version", "");
			
			if (!type.equals("1") &&  !type.equals("2"))
				return false;
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	

	protected void setState(DownloadState state) {
		this.state = state;
	}
	
	public DownloaderEntryUIRow getUIRow () {
		return uiRow;
	}


}
