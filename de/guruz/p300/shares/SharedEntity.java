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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;

import de.guruz.p300.Configuration;
import de.guruz.p300.utils.CaseInsensitiveStringComparator;
import de.guruz.p300.utils.DirectoryFileFilter;
import de.guruz.p300.utils.FileFileFilter;
import de.guruz.p300.utils.OsUtils;
import de.guruz.p300.utils.URL;


/**
 * Represents a shared directory or file
 */
public class SharedEntity {
	// FIXME: always use requestedPath
	protected SharedEntity(Share s, File f, String rp) {
		this.physical = f;
		this.share = s;
		this.requestedPath = rp;
	}
	
	protected String requestedPath = null;

	public static SharedEntity getRootDirectory () {
		// get the temp dir here and give that to printDAVPropfindElement. 
		// this is no security bug or else, it is just because printDAVPropfindElement
		// needs a File object (and that should be a directory)
		String td = System.getProperty("java.io.tmpdir");
		File tdf = new File (td);
		
		SharedEntity sharedEntity =  new SharedEntity (null, tdf, "/");
		return sharedEntity;
	}
	
	public static SharedEntity getSharesDirectory () {
		// get the temp dir here and give that to printDAVPropfindElement. 
		// this is no security bug or else, it is just because printDAVPropfindElement
		// needs a File object (and that should be a directory)
		String td = System.getProperty("java.io.tmpdir");
		File tdf = new File (td);
		
		SharedEntity sharedEntity =  new SharedEntity (null, tdf, "/shares");
		return sharedEntity;
	}
	
	public static SharedEntity getEntityFromShare (Share s) {
		String rp = "/shares/" + URL.encode(s.getName());
		SharedEntity sharedEntity =  new SharedEntity (s, s.getLocation(), rp);
		return sharedEntity;
	}

	public static SharedEntity requestPathToSharedEntity(String inRp) {
		try {
			if (!inRp.startsWith("/shares")) {
				return null;
			}

			String rp = inRp.substring(("/shares".length() + 1));
			
			//System.out.println (inRp + " -> " + rp);

			int slashIndex = rp.indexOf('/');

			if (slashIndex == -1) {
				rp = rp + '/';
				slashIndex = rp.indexOf('/');
			}

			String shareName = URL.decode(rp.substring(0, slashIndex));
			Share share = ShareManager.instance().getShare(shareName);

			// share not found!
			if (share == null) {
				return null;
			}

			String requestedFileName = rp.substring(slashIndex);
			String shareDirectory = share.getFileLocation();
			File shareDirectoryFile = (new File(shareDirectory))
					.getCanonicalFile();

			File f = SharedEntity.getFileFromRequest(shareDirectory, requestedFileName);
			// does the file exist?
			if (f == null) {
				return null;
			}

			f = f.getCanonicalFile();

			if (!f.toString().startsWith(shareDirectoryFile.toString())) {
				return null;
			}
			
			SharedEntity sharedEntity = new SharedEntity (share, f, inRp);
			
			return sharedEntity;

		} catch (Exception e) {
			return null;
		}
	}

	static SortedMap<String, Charset> charsetMap = Charset.availableCharsets();

	protected static File getFileFromRequest(String shareDirectory,
			String requestedFileName) {
		String fn = null;
		File f = null;

		// 2. use utf8
		fn = URL.decode(requestedFileName, "UTF-8");
		f = new File(shareDirectory, fn);
		if ((f != null) && f.exists()) {
			return f;
		}

		// 3. use iso latin 1
		fn = URL.decode(requestedFileName, "ISO-8859-1");
		f = new File(shareDirectory, fn);
		if ((f != null) && f.exists()) {
			return f;
		}

		// 4. try everything (does this need too much cpu? i dont hope so)
		Set<String> cks = SharedEntity.charsetMap.keySet();
		Iterator<String> cki = cks.iterator();
		while (cki.hasNext()) {
			String ck = cki.next();

			try {
				f = new File(shareDirectory, de.guruz.p300.utils.URL.decode(fn,
						ck));
			} catch (Exception e) {
				f = null;
			}

			if ((f != null) && f.exists()) {
				return f;
			}
		}

		return null;
	}

	protected File physical = null;

	protected Share share = null;

	public String getShareName () {
		return this.share.getName();
	}
	
	/**
	 * Returns true if the file exists, is readable and shareable (hidden files
	 * are not, for example)
	 * 
	 * @return
	 */
	public boolean isShareable() {
		// FIXME
		return  !(this.physical.isHidden() && !OsUtils.isWindows())
				//&& this.physical.exists()
				//&& this.physical.canRead()
				&& !Configuration.instance().isUnshareableFile(
						this.physical.getName());
	}

	public boolean isDirectory() {
		return this.physical.isDirectory();
	}
	
	public boolean isFile () {
		return this.physical.isFile();
	}
	
	public long getFileSize () {
		return this.physical.length();
	}
	
	public long getLastModified () {
		return this.physical.lastModified();
	}

	public File getPhysicalFileObject() {
		return this.physical;
	}

	public String getRequestedPath() {
		return this.requestedPath;
	}

	public void setRequestedPath(String requestedPath) {
		this.requestedPath = requestedPath;
	}
	
	public SharedEntity[] getFileSubEntities () {
		if (!this.isDirectory ()) {
			return new SharedEntity[0];
		}
		
		return this.filesToSubEntities (this.getPhysicalFileObject().listFiles(new FileFileFilter()));
	}
	
	public SharedEntity[] getDirectorySubEntities () {
		if (!this.isDirectory ()) {
			return new SharedEntity[0];
		}
		
		return this.filesToSubEntities (this.getPhysicalFileObject().listFiles(new DirectoryFileFilter()));
	}
	
	public SharedEntity[] getSubEntities () {
		if (!this.isDirectory ()) {
			return new SharedEntity[0];
		}
		
		return this.filesToSubEntities (this.getPhysicalFileObject().listFiles());
	}
	
	protected SharedEntity[] filesToSubEntities (File[] subfiles) {
		int subfileCount = subfiles.length;
		
		Arrays.sort(subfiles, CaseInsensitiveStringComparator.instance());
		
		SharedEntity[] sharedEntities = new SharedEntity[subfileCount];
		
		for (int i = 0; i < subfileCount; i++) {
			SharedEntity subEntity = this.getSubEntity (subfiles[i]);
			sharedEntities[i] = subEntity;
		}
		
		return sharedEntities;
	}

	protected SharedEntity getSubEntity(File f) {
		String reqpath = this.getRequestedPath ();
		if (this.isDirectory () && !reqpath.endsWith("/")) {
			reqpath = reqpath + "/";
		}
		String urlEncodedName = URL.encode(f.getName());
		reqpath = reqpath + urlEncodedName;
		
		SharedEntity ret = new SharedEntity (this.share, f, reqpath);
		
		
		return ret;
	}

	public String getShortName() {
		return this.getPhysicalFileObject().getName();
	}
}
