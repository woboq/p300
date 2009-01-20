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
package de.guruz.experiments.filedb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FileDataBase2 {
	private static HashMap<String, Double> tokenDBDouble = new HashMap<String, Double>();
	private static HashMap<String, HashSet<String>> tokenDBHashSet = new HashMap<String, HashSet<String>>();
	private static HashSet<String> FileNameDB = new HashSet<String>();
	private static final Pattern fileNameSplitter = Pattern.compile("(\\p{M}|\\p{P}|\\p{Z}|\\p{C})+");
	private static FileWriter csvFileWriter;
	private static BufferedReader csvFileReader;
	private static OutputStreamWriter xmlFileWriter;
	
	// CHANGE HERE!
	// Controls if we're using token=>filename (0), filename list (1), token=>double (2), filename csv (3),
	// filename xml (4)
	private static final byte type = 4;
	//private static String csvFileName = "/home/tomcat/dev/java/p300.csv";
	//private static String xmlFileName = "/home/tomcat/dev/java/p300.xml.gz";
	private static String searchString = "Kiuas";
	private static String indexPath = "/home/tomcat";
	private static String csvFileName = "/tmp/p300.csv";
	private static String xmlFileName = "/tmp/p300.xml.gz";
//	private static String searchString = "Falco";
//	private static String indexPath = "/Users/guruz/Music";
	private static float factor;
	private static long minimum = 1;
	
	public static void main(String[] args) {
		if (FileDataBase2.type == 3) {
			try {
				FileDataBase2.csvFileWriter = new FileWriter(FileDataBase2.csvFileName);
			} catch (IOException ioe) {
				System.err.println("Error creating csvFileWriter: ".concat(ioe.getLocalizedMessage()));
			}
		}
		if (FileDataBase2.type == 4) {
			try {
				FileDataBase2.xmlFileWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(FileDataBase2.xmlFileName)), "UTF-8");
				FileDataBase2.xmlFileWriter.write("<FILES>\n");
			} catch (IOException ioe) {
				System.err.println("Error creating xmlFileWriter: " + ioe.getLocalizedMessage());
			}
		}
		Runtime r = Runtime.getRuntime();
		long startFreeMem = r.freeMemory();
		long startTotalMem = r.totalMemory();
		long startTimeStamp = System.currentTimeMillis();
		
		FileDataBase2.index(new File(FileDataBase2.indexPath));
		
		long indexEndTimeStamp = System.currentTimeMillis();
//		r.gc();
//		try {
//			Thread.sleep(10 * 1000);
//		} catch (InterruptedException ie) {
//			System.err.println("Sleep interrupted: ".concat(ie.getLocalizedMessage()));
//		}
		long endFreeMem = r.freeMemory();
		long endTotalMem = r.totalMemory();
		if (FileDataBase2.type == 3) {
			try {
				FileDataBase2.csvFileWriter.close();
				FileDataBase2.csvFileReader = new BufferedReader(new FileReader(FileDataBase2.csvFileName));
			} catch (IOException ioe) {
				System.err.println("Error closing csvFileWriter or creating csvFileReader: ".concat(ioe.getLocalizedMessage()));
			}
		}
		if (FileDataBase2.type == 4) {
			try {
				FileDataBase2.xmlFileWriter.write("</FILES>");
				FileDataBase2.xmlFileWriter.close();
			} catch (IOException ioe) {
				System.err.println("Error closing xmlFileWriter: " + ioe.getLocalizedMessage());
			}
		}
		long searchStartTimeStamp = System.currentTimeMillis();
		
		System.out.println(FileDataBase2.search(FileDataBase2.searchString));
		
		long endTimeStamp = System.currentTimeMillis();
		System.out.println("Index: ".concat(Long.toString(indexEndTimeStamp - startTimeStamp)).concat("ms, Search: ").concat(Long.toString(endTimeStamp - searchStartTimeStamp)).concat("ms"));
		DecimalFormat df = new DecimalFormat();
		long mem = (endTotalMem - endFreeMem) - (startTotalMem - startFreeMem);
		System.out.println("Used Mem: ".concat(df.format(mem)).concat(" Byte"));
		if (FileDataBase2.type == 3) {
			try {
				FileDataBase2.csvFileReader.close();
			} catch (IOException ioe) {
				System.err.println("Error closing csvFileReader: ".concat(ioe.getLocalizedMessage()));
			}
		}
	}
	
	public static void index(File f) {
		long startTime = System.currentTimeMillis();
		if (FileDataBase2.type == 0) {
			FileDataBase2.indexTokenHashSet(f.getAbsolutePath());
		} else if (FileDataBase2.type == 1) {
			FileDataBase2.indexFileName(f.getAbsolutePath());
		} else if (FileDataBase2.type == 2) {
			FileDataBase2.indexTokenDouble(f.getAbsolutePath());
		} else if (FileDataBase2.type == 3) {
			FileDataBase2.indexFiles2CSV(f.getAbsolutePath());
		} else {
			FileDataBase2.indexFiles2XML(f.getAbsolutePath());
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		try {
			long sleepTime = (long)(duration * FileDataBase2.factor);
			if (sleepTime < FileDataBase2.minimum) {
				sleepTime = FileDataBase2.minimum;
			}
//			System.err.println("Sleeping " + sleepTime);
			Thread.sleep(sleepTime);
		} catch (InterruptedException ie) {
			System.err.println("Couldn't sleep: " + ie.getLocalizedMessage());
		}
		
		File[] fileList = f.listFiles();
		if (fileList != null) {
			for (File subFile: fileList) {
				boolean doIndex = false;
				try {
					doIndex = subFile.getAbsolutePath().equals(subFile.getCanonicalPath());
				} catch (IOException ioe) {
					System.err.println("Error indexing file: ".concat(ioe.getLocalizedMessage()));
				}
				if (doIndex) {
					FileDataBase2.index(subFile);
				}
			}
		}
	}
	
	public static HashSet<String> search(String s) {
		if (FileDataBase2.type == 0) {
			return FileDataBase2.searchTokenDBHashSet(s);
		} else if (FileDataBase2.type == 1) {
			return FileDataBase2.searchFileName(s);
		} else if (FileDataBase2.type == 2) {
			return FileDataBase2.searchTokenDBDouble(s);
		} else if (FileDataBase2.type == 3) {
			return FileDataBase2.searchFiles2CSV(s);
		} else {
			return FileDataBase2.searchFiles2XML(s);
		}
	}
	
	private static String[] parseFileName(String fn) {
		return FileDataBase2.fileNameSplitter.split(fn);
	}
	
	private static void indexTokenDouble(String fn) {
		String[] tokens = FileDataBase2.parseFileName(fn);
		for (String token: tokens) {
			Double entry = Double.valueOf(Math.random());
			FileDataBase2.tokenDBDouble.put(token, entry);
		}
	}

	private static void indexTokenHashSet(String fn) {
		String[] tokens = FileDataBase2.parseFileName(fn);
		for (String token: tokens) {
			if (FileDataBase2.tokenDBHashSet.containsKey(token)) {
				HashSet<String> entry = FileDataBase2.tokenDBHashSet.get(token);
				entry.add(fn);
			} else {
				HashSet<String> entry = new HashSet<String>();
				entry.add(fn);
				FileDataBase2.tokenDBHashSet.put(token, entry);
			}
		}
	}

	private static void indexFileName(String fn) {
		FileDataBase2.FileNameDB.add(fn);
	}
	
	private static void indexFiles2CSV(String fn) {
		try {
			FileDataBase2.csvFileWriter.write(fn.concat("\n"));
		} catch (IOException ioe) {
			System.err.println("Error writing csvFileWriter: ".concat(ioe.getLocalizedMessage()));
		}
	}
	
	private static HashSet<String> searchTokenDBHashSet(String s) {
		return FileDataBase2.tokenDBHashSet.get(s);
	}
	
	private static HashSet<String> searchFileName(String s) {
		HashSet<String> result = new HashSet<String>();
		Iterator<String> fileNames = FileDataBase2.FileNameDB.iterator();
		while (fileNames.hasNext()) {
			String fileName = fileNames.next();
			if (fileName.contains(s)) {
				result.add(fileName);
			}
		}
		return result;
	}
	
	private static HashSet<String> searchTokenDBDouble(String s) {
		HashSet<String> result = new HashSet<String>();
		result.add(Double.toString(FileDataBase2.tokenDBDouble.get(s)));
		return result;
	}
	
	private static HashSet<String> searchFiles2CSV(String s) {
		HashSet<String> result = new HashSet<String>();
		try {
			String line = FileDataBase2.csvFileReader.readLine();
			while (line != null) {
				if (line.contains(s)) {
					result.add(line);
				}
				line = FileDataBase2.csvFileReader.readLine();
			}
		} catch (IOException ioe) {
			System.err.println("Error reading csvFileReader: ".concat(ioe.getLocalizedMessage()));
		}
		return result;
	}
	
	private static void indexFiles2XML(String fn) {
		try {
			char[] chars = fn.toCharArray();
			char[] newchars = new char[chars.length];
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if ((c != '\u0009') && (c != '\r') && (c != '\n') && ((c < '\u0020') || (c > '\uD7FF')) && ((c < '\uE000') || (c > '\uFFFD'))) {
					newchars[i] = '\u0000';
				} else {
					newchars[i] = chars[i];
				}
			}
			fn = String.valueOf(newchars);
			fn = fn.replace("\u0000", "");
			fn = fn.replace("&", "&amp;");
			fn = fn.replace("<", "&lt;");
			fn = fn.replace(">", "&gt;");
			fn = fn.replace("\"", "&quot;");
			fn = fn.replace("'", "&apos;");
			FileDataBase2.xmlFileWriter.write("  <FILE name=\"" + fn + "\" />\n");
		} catch (IOException ioe) {
			System.err.println("Error writing to xmlFileWriter: " + ioe.getLocalizedMessage());
		
		}
	}
	
	private static HashSet<String> searchFiles2XML(String s) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		HashSet<String> result = new HashSet<String>(); 
		try {
			SAXParser sp = spf.newSAXParser();
			GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(FileDataBase2.xmlFileName));
			InputSource is = new InputSource (new InputStreamReader (gzis, "UTF-8"));
			MyHandler mh = new MyHandler(s);
			sp.parse(is, mh);
			
			gzis.close();
			result = mh.gimmeResult();
		} catch (org.xml.sax.SAXParseException saxex) {
			System.err.println (saxex.toString() + " @ line " + saxex.getLineNumber());
		} catch (Exception e) {
			
			System.err.println("Unable to read XML file: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		return result;
	}
}

class MyHandler extends DefaultHandler {
	private HashSet<String> result = new HashSet<String>();
	private String searchString = "";
	
	public MyHandler(String searchFor) {
		this.searchString = searchFor;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("FILE") && (attributes.getLength() == 1) && attributes.getQName(0).equals("name")) {
			String fn = attributes.getValue(0);
			if (fn.contains(this.searchString)) {
				this.result.add(fn);
			}
		}
	}
	
	public HashSet<String> gimmeResult() {
		return this.result;
	}
}
