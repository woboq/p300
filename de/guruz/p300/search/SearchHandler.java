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

import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class SearchHandler extends DefaultHandler {
	private String currentShareName;
	private HashSet<SingleSearchResult> searchResult = new HashSet<SingleSearchResult>();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("FILEINDEX")) {
			this.handleFileIndex(attributes);
		} else if (qName.equals("SHARE")) {
			this.handleShare(attributes);
		} else if (qName.equals("FILE")) {
			this.handleFile(attributes);
		} else {
			System.err.println("Unknown element found in XML database");
		}
		
		super.startElement(uri, localName, qName, attributes);
	}

	private HashSet<SingleSearchResult> getSearchResultAsHashSet() {
		return this.searchResult;
	}
	
	public SingleSearchResult[] getSearchResult() {
		return this.getSearchResultAsHashSet().toArray(new SingleSearchResult[0]);
	}
	
	protected SearchHandler() {
	}
	
	private void handleFileIndex(Attributes att) throws SAXException {
		String versionString = att.getValue("version");
		if ((versionString != null) && !versionString.equals("0.1")) {
			throw new UnknownVersionException(versionString);
		}
	}
	
	private void handleShare(Attributes att) {
		String shareName = att.getValue("name");
		if (shareName != null) {
			this.setCurrentShareName(shareName);
		}
	}
	
	private void handleFile(Attributes att) {
		String fileName = att.getValue("name");
		String fileSizeString = att.getValue("size");
		
		long fileSize = 0;
		if (fileSizeString != null) {
			try {
				fileSize = Long.valueOf(fileSizeString);
			} catch (NumberFormatException nfe) {
				fileSize = 0;
			}
		}
		
		if (fileName != null) {
			this.handleFile(fileName, fileSize);
		}
	}
	
	protected abstract void handleFile(String name, long size);

	private void setCurrentShareName(String currentShareName) {
		this.currentShareName = currentShareName;
	}

	protected String getCurrentShareName() {
		return this.currentShareName;
	}
	
	private void addSearchResult(SingleSearchResult single) {
		this.getSearchResultAsHashSet().add(single);
	}
	
	protected void addSearchResult(String name, long size) {
		SingleSearchResult single = new SingleSearchResult(this.getCurrentShareName(), name, size);
		this.addSearchResult(single);
	}

}
