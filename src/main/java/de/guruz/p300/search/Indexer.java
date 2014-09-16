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
package de.guruz.p300.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import de.guruz.p300.logging.D;
import de.guruz.p300.search.dataextractor.FileNameObserver;
import de.guruz.p300.search.dataextractor.FileSizeObserver;
import de.guruz.p300.search.dataextractor.IndexerObservable;
import de.guruz.p300.shares.Share;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.utils.FileUtils;

public class Indexer {
	private static final File tempDataBaseFile = new File(System.getProperty("user.home") + "/.p300/fileindex_temp.xml.gz");
	private static OutputStreamWriter indexFile;
	// After indexing a file, Indexer will sleep "sleepFactor * timeToIndex(file)" in milliseconds
	// default 0
	private static long sleepFactor;
	// This is the minimum time to sleep in milliseconds after indexing a file
	// default 1
	private static long minimumSleep = 1;
	private static IndexerObservable myObservable = new IndexerObservable();
	private static FileNameObserver fno;
	private static FileSizeObserver fso;
	public static int lastIndexFileCount = -1;
	public static long lastIndexAllSize = -1;
	private static String xmlVersion = "0.1";
	public static String[] speedNames = {"African Swallow", "Cheetah", "Llama", "Turtle"};
	public static int defaultSpeedValue = 1;
	
	// Retrieve all shares and index all files
	public static void indexAllShares() {
		D.out("Indexing all shares...");
		Indexer.addObservers();
		Indexer.openFile();
		Indexer.writeIndexBeginToFile();
		String[] shareNames = ShareManager.instance().getShareNames();
		if (shareNames != null) {
			for (String shareName: shareNames) {
				Share currentShare = ShareManager.instance().getShare(shareName);
				if (currentShare == null) {
					continue;
				}
				File shareDirectory = currentShare.getLocation();
				if (shareDirectory == null) {
					continue;
				}
				Indexer.writeShareStart(shareName);
				//	FileNameObserver needs to cut filename paths at shareDirectory to save space
				if (Indexer.fno != null) {
					Indexer.fno.shareLocation = shareDirectory.getAbsolutePath();
				}
				Indexer.indexFileOrDirectory(shareDirectory);
				Indexer.writeShareEnd(shareName);
			}
		}
		if (!IndexerThread.restartIndexer) {
			Indexer.indexStats();
			Indexer.writeIndexEndToFile();
			D.out("Done indexing all shares");
		}
		Indexer.closeFile();
		Indexer.delObservers();
	}
	
	// Index a file or a directory recursively. Sleep some time after saving a file
	private static void indexFileOrDirectory(File f) {
//		System.err.println(IndexerThread.restartIndexer);
		long startWrite = System.currentTimeMillis();
		Indexer.writeFileToIndex(f);
		long endWrite = System.currentTimeMillis();
		long duration = endWrite - startWrite;
		Indexer.sleep(duration);
		File[] contents = f.listFiles();
		if ((contents != null) && !IndexerThread.restartIndexer) {
			for (File f2: contents) {
				if (Indexer.isRealFile(f2)) {
					Indexer.indexFileOrDirectory(f2);
				}
			}
		}
	}
	
	private static void writeFileToIndex(File f) {
		if (f == null) {
			return;
		}
		Indexer.myObservable.setChanged();
		Indexer.myObservable.notifyObservers(f);
		
		String xmlString = "    <FILE";
		for (String observerString: Indexer.myObservable.xmlStringList) {
			xmlString += ' ' + observerString;
		}
		xmlString += " />\n";
		
		Indexer.writeStringToFile(xmlString);
	}
	
	// Sleeps current Thread for duration * sleepFactor, but at least minimumSleep milliseconds
	private static void sleep(long duration) {
		long sleepTime = duration * Indexer.sleepFactor;
		if (sleepTime < Indexer.minimumSleep) {
			sleepTime = Indexer.minimumSleep;
		}
		try {
//			System.err.println("Sleeping for " + sleepTime + "ms");
			Thread.sleep(sleepTime);
		} catch (InterruptedException ie) {
			System.err.println("Indexer sleep interrupted. Growl.");
		}
	}
	
	// Open the temp file index
	private static void openFile() {
		if (Indexer.indexFile != null) {
			return;
		}
		try {
			Indexer.tempDataBaseFile.createNewFile();
		} catch (IOException ioe) {
			System.err.println("Can't create temp file index: " + ioe.getLocalizedMessage());
		}
		try {
			Indexer.indexFile = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(Indexer.tempDataBaseFile)), "UTF-8");
		} catch (FileNotFoundException ioe) {
			System.err.println("Temp file index impossible to write");
		} catch (IOException ioe) {
			System.err.println("I/O error while creating compressed file stream: " + ioe.getLocalizedMessage());
		}
		if (Indexer.indexFile == null) {
			System.err.println("Couldn't open temp file index");
		}
	}
	
	// Close the temp file index
	private static void closeFile() {
		if (Indexer.indexFile != null) {
			try {
				Indexer.indexFile.close();
			} catch (IOException ioe) {
				System.err.println("Can't close temp file index: " + ioe.getLocalizedMessage());
			}
		}
		Indexer.indexFile = null;
		if (!IndexerThread.restartIndexer) {
			Indexer.pushIndexLive();
		} else {
			Indexer.deleteTempIndex();
		}
	}
	
	// Move the temp file index to be the live file index
	private static void pushIndexLive() {
		Common.dataBaseFile.delete();
		//boolean renameSuccess = Indexer.tempDataBaseFile.renameTo(Common.dataBaseFile);
		boolean renameSuccess = FileUtils.renameTo (Indexer.tempDataBaseFile, Common.dataBaseFile);
		if (!renameSuccess) {
			System.err.println("Couldn't move temp index to live index");
		}
	}

	private static void indexStats() {
		if ((Indexer.fno != null) && (Indexer.fso != null)) {
			Indexer.lastIndexFileCount = Indexer.fno.fileCount;
			Indexer.lastIndexAllSize = Indexer.fso.sizeCount;
		} else {
			Indexer.lastIndexFileCount = -1;
			Indexer.lastIndexAllSize = -1;
		}
	}
	
	private static void writeStringToFile(String s) {
		if (Indexer.indexFile == null) {
			return;
		}
		try {
			Indexer.indexFile.write(s);
		} catch (IOException ioe) {
			System.err.println("Couldn't write to temp index file");
		}
	}
	
	private static void writeIndexBeginToFile() {
		Indexer.writeStringToFile("<FILEINDEX version=\"" + Indexer.xmlVersion + "\">\n");
	}
	
	private static void writeIndexEndToFile() {
		Indexer.writeStringToFile("</FILEINDEX>\n");
	}
	
	// Check if the OS & Java think a specific file is at the same place & if it is a file/dir
	// This way we can weed out symlinks & devices
	private static boolean isRealFile(File f) {
		if ((f != null) && (f.isFile() || f.isDirectory())) {
			try {
				String absPath = f.getAbsolutePath();
				String conPath = f.getCanonicalPath();
				return absPath.equals(conPath);
			} catch (IOException ioe) {
				System.err.println("Couldn't traverse directory: " + ioe.getLocalizedMessage());
				return false;
			}
		} else {
			return false;
		}
	}
		
	// Set indexer speed level from 0 to 3
	public static void setSpeedLevel(int level) {
		if (level == 0) {
			// African Swallow
			Indexer.sleepFactor = 0;
			Indexer.minimumSleep = 0;
		} else if (level == 1) {
			// Cheetah
			Indexer.sleepFactor = 0;
			Indexer.minimumSleep = 1;
		} else if (level == 2) {
			// Llama
			Indexer.sleepFactor = 10;
			Indexer.minimumSleep = 10;
		} else if (level == 3) {
			// Turtle
			Indexer.sleepFactor = 100;
			Indexer.minimumSleep = 100;
		} else {
			// default = Cheetah
			Indexer.sleepFactor = 0;
			Indexer.minimumSleep = 1;
		}
	}
	
	private static void addObservers() {
		if (Indexer.myObservable == null) {
			return;
		}
		
		Indexer.fno = new FileNameObserver();
		Indexer.fso = new FileSizeObserver();
		
		Indexer.myObservable.addObserver(Indexer.fno);
		Indexer.myObservable.addObserver(Indexer.fso);
	}
	
	private static void delObservers() {
		if (Indexer.myObservable == null) {
			return;
		}
		
		Indexer.myObservable.deleteObservers();
		
		Indexer.fno = null;
		Indexer.fso = null;
	}
	
	private static void writeShareStart(String shareName) {
		String encodedShareName = Common.encodeForXMLAttribute(shareName);
		String xmlString = "  <SHARE name=\"" + encodedShareName + "\">\n";
		Indexer.writeStringToFile(xmlString);
	}
	
	private static void writeShareEnd(String shareName) {
		String xmlString = "  </SHARE>\n";
		Indexer.writeStringToFile(xmlString);
	}
	
	private static void deleteTempIndex() {
		if (Indexer.tempDataBaseFile != null) {
			Indexer.tempDataBaseFile.delete();
		}
	}
}