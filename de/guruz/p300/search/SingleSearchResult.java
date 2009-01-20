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
import java.io.StringWriter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.guruz.p300.shares.Share;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.utils.HumanReadableSize;
import de.guruz.p300.utils.IconChooser;

public class SingleSearchResult implements Comparable<SingleSearchResult> {

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Single search result. shareName = " + shareName + ", fileSize = " + fileSize + ", fileName = " + fileName;
	}

	private String shareName;
	private long fileSize;
	private String fileName;
	
	public SingleSearchResult(String theShareName, String theFileName, long theFileSize) {
		super();
		this.setShareName(theShareName);
		this.setFileName(theFileName);
		this.setFileSize(theFileSize);
	}
	
	public int compareTo(SingleSearchResult result) {
		if (result == null) {
			return 1;
		}
		int shareComparison = this.shareName.compareTo(result.getShareName());
		if (shareComparison == 0) {
			return this.getFile().compareTo(result.getFile());
		} else {
			return shareComparison;
		}
	}
	
	public File getFile() {
		String shareName = this.getShareName();
		if (shareName == null) {
			return null;
		}
		Share s = ShareManager.instance().getShare(this.getShareName());
		if (s == null) {
			return null;
		}
		File shareDirectory = ShareManager.instance().getShare(this.getShareName()).getLocation();
		File thisFile = new File(shareDirectory, this.getFileName());
		return thisFile;
	}
	
	public String getFileName() {
		return this.fileName;
	}

	private String getFileNameForHTTP() {
		String s = this.getFileName();
		if (s == null) {
			return "";
		}
		return s.replace (File.separatorChar, '/');
	}
	
	public long getFileSize() {
		return this.fileSize;
	}
	
	public String getShareName() {
		return this.shareName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public void setShareName(String shareName) {
		this.shareName = shareName;
	}
	
	public boolean isInValidShare() {
		return ShareManager.instance().isValidShareName(this.getShareName());
	}
	
	public boolean canRead() {
		File f = this.getFile();
		if (f == null) {
			return false;
		}
		return this.getFile().canRead();
	}
	
	public boolean isHidden() {
		File f = this.getFile();
		if (f == null) {
			return false;
		}
		return this.getFile().isHidden();
	}
	
	public String toHTML(String searchTerm) {		
		File f = this.getFile();
		if (f == null) {
			return "";
		}
		String fileShortName = f.getName();
		String fileFullName = "/shares/" + this.getShareName() + this.getFileNameForHTTP();
		if (f.isDirectory()) {
			fileFullName = fileFullName + '/';
			fileShortName = fileShortName + '/';
		}
		String icon = IconChooser.fileNameToHTMLImageTag(fileShortName);

		StringWriter sw = new StringWriter();
		sw.append("<span class='searchresultfile'>");
		sw.append(icon);
		sw.append("<a ");
		if (!f.isDirectory()) {
			sw.append("title=\""
					+ HumanReadableSize.get(f.length()) + "\" ");
		}

		String wordPattern = "";
		String term = "";
		if (Searcher.isRegexSearch(searchTerm)) {
			wordPattern = searchTerm.substring(6);
			term = fileShortName;
		} else {
			String[] words = searchTerm.split("\\s+");
			Locale myLocale = Locale.getDefault();
			for (int i = 0; i < words.length; i++) {
				words[i] = words[i].toLowerCase(myLocale);
				words[i] = Pattern.quote(words[i]);
				wordPattern += words[i];
				if (i + 1 < words.length) {
					wordPattern += "|";
				}
			}
			term = fileShortName.toLowerCase(Locale.getDefault());
		}
		Matcher m = Pattern.compile(wordPattern).matcher(term);
		String fileShortNameWithMatch = "";
		int pos = 0;
		while (m.find()) {
			fileShortNameWithMatch += fileShortName.substring(pos, m.start());
			fileShortNameWithMatch += "<em class='searchtermhighlight'>";
			fileShortNameWithMatch += fileShortName.substring(m.start(), m.end());
			fileShortNameWithMatch += "</em>";
			pos = m.end();
		}
		fileShortNameWithMatch += fileShortName.substring(pos);
		
		sw.append("href=\"" + fileFullName + "\">"
				+ fileShortNameWithMatch + "</a></span>");

		return sw.toString();
	}
	
	public String toHTML() {
		return this.toHTML("");
	}
	
}