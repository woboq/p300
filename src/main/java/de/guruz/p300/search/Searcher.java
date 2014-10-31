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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.guruz.p300.Configuration;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.utils.HumanReadableTime;

public class Searcher {
	private static SingleSearchResult[] emptySsrArray = new SingleSearchResult[0];

	public static SingleSearchResult[] search(String s) {
		SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			System.err.println("Can't create XML parser: "
					+ e.getLocalizedMessage());
			return null;
		}
		InputSource xmlFile = Searcher.openFile();
		if (xmlFile == null) {
			System.err.println("Couldn't open/read database file");
			return null;
		}

		SearchHandler searcher;
		if (Searcher.isRegexSearch(s)) {
			s = s.substring(6);
			searcher = Searcher.getRegexSearcher(s);
		} else {
			searcher = Searcher.getTextSearcher(s);
		}
		if (searcher == null) {
			System.err.println("Couldn't find a suitable SearchHandler");
			return null;
		}

		try {
			parser.parse(xmlFile, searcher);
		} catch (IOException ioe) {
			System.err.println("Can't read database file: "
					+ ioe.getLocalizedMessage());
		} catch (UnknownVersionException uve) {
			System.err.println("Unknown version of XML database:"
					+ uve.getLocalizedMessage());
		} catch (SAXException se) {
			System.err.println("Can't parse database file: "
					+ se.getLocalizedMessage());
		}

		Searcher.closeFile(xmlFile);

		SingleSearchResult[] res = searcher.getSearchResult();
		HashSet<SingleSearchResult> okayRes = new HashSet<SingleSearchResult>();
		for (SingleSearchResult ssr : res) {
			if ((s != null) && Searcher.resultOkay(ssr)) {
				okayRes.add(ssr);
			}
		}

		return okayRes.toArray(Searcher.emptySsrArray);
	}

	private static SearchHandler getTextSearcher(String s) {
		String[] words = s.split("\\s+");
		return new TextSearchHandler(words);
	}

	private static SearchHandler getRegexSearcher(String s) {
		Pattern searchPattern = Pattern.compile(s);
		return new RegexSearchHandler(searchPattern);
	}

	public static boolean isRegexSearch(String s) {
		return s.startsWith("REGEX ");
	}

	private static InputSource openFile() {
		File xmlFile = Searcher.getXMLFile();
		if (!xmlFile.canRead()) {
			System.err.println("Can't read from database file");
			return null;
		}
		InputSource is = null;
		try {
			FileInputStream fis = new FileInputStream(xmlFile);
			GZIPInputStream zis = new GZIPInputStream(fis);
			is = new InputSource(new InputStreamReader(zis, "UTF-8"));
		} catch (IOException ioe) {
			System.err.println("Can't open database file: "
					+ ioe.getLocalizedMessage());
		}
		return is;
	}

	private static File getXMLFile() {
		return Common.dataBaseFile;
	}

	private static void closeFile(InputSource stream) {
		try {
			stream.getCharacterStream().close();
		} catch (IOException ioe) {
			System.err.println("Can't close database file: "
					+ ioe.getLocalizedMessage());
		}
	}

	public static File[] searchFiles(String s) {
		SingleSearchResult[] result = Searcher.search(s);
		if (result == null) {
			return null;
		}
		HashSet<File> files = new HashSet<File>();
		for (SingleSearchResult single : result) {
			files.add(single.getFile());
		}

		return files.toArray(new File[0]);
	}

	public static long getIndexMTime() {
		return Searcher.getXMLFile().lastModified();
	}

	public static String getHumanReadableIndexAge() {
		long now = System.currentTimeMillis();
		long time = Searcher.getIndexMTime();
		if (time == 0)
			return "?";
		else
			return HumanReadableTime.timeDifferenceAsString(0, now - time);
	}

	private static boolean resultOkay(SingleSearchResult res) {
		boolean isInValidShare = res.isInValidShare();
		if (!isInValidShare) {
			return false;
		}
		boolean canRead = res.canRead();
		if (!canRead) {
			return false;
		}
		boolean isUnshareable;
		try {
			isUnshareable = Configuration.instance().isUnshareableFile(
					new File(ShareManager.instance().getShare(
							res.getShareName()).getFileLocation(), res
							.getFileName()).getCanonicalPath());
		} catch (IOException ioe) {
			isUnshareable = true;
		}
		if (isUnshareable) {
			return false;
		}
		boolean isHidden = res.isHidden();
		if (isHidden) {
			return false;
		}
		return true;
	}

}