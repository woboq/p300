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
package de.guruz.p300.requests;

import java.io.StringWriter;
import java.util.Arrays;

import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.search.Searcher;
import de.guruz.p300.search.SingleSearchResult;
import de.guruz.p300.utils.HTTP;
import de.guruz.p300.utils.XML;

public class SearchRequest extends Request {
	
	@Override
	public void handle() throws Exception {
		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpContentType("text/html");

		Layouter layouter = new Layouter (this.requestThread); 
		String search = this.handleSearch(layouter);

		this.requestThread.httpContents ();
		this.requestThread.write(layouter.getBeforeMainDiv());
		this.requestThread.flush();
		this.requestThread.write(search);
		this.requestThread.write (layouter.getAfterMainDiv());
		this.requestThread.flush();
		this.requestThread.close();
	}

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (rt != HTTPVerb.GET) {
			return false;
		}
		
		return (reqpath.startsWith ("/search"));
	}
	
	private String handleSearch(Layouter l) {
		String searchString = this.getSearchString();
		l.replaceTitle(this.requestThread.getLocalDisplay() + " - Search for &quot;" + XML.encode(searchString) + "&quot;");
		l.replaceSearchString(XML.encode(searchString));
		l.replaceBasicStuff ();
		SingleSearchResult[] result = null;
		if ((searchString != null) && !searchString.equals("")) {
			result = Searcher.search(searchString);
		} 
		if (result == null)
			result = new SingleSearchResult[0];
		
		return this.showResults(result, searchString);
	}
	
	private String getSearchString() {
		if (this.requestThread == null) {
			return "";
		}
		String reqpath = this.requestThread.path;
		if (reqpath == null) {
			return "";
		}
		String searchString = HTTP.extractParameter(reqpath, "searchString");
		if (searchString == null) {
			return "";
		} else {
			return searchString;
		}	
	}
	
	private String showResults(SingleSearchResult[] results, String searchTerm) {
		StringWriter w = new StringWriter();
		
		Arrays.sort(results);
		String currentShare = null;
		
		
		
		w.append("<p>");
		w.append("When using <a href='http://p300.eu/'>p300</a> instead of the webinterface, you can search all hosts at the same time!");
		w.append("</p>");
		
		w.append("<pre class='searchresultblock'>");
		
		for (SingleSearchResult aResult: results) {
			if ((currentShare == null) || !currentShare.equals(aResult.getShareName())) {
				w.append("\n");
				currentShare = aResult.getShareName();
				String shareHeaderHTML = this.showShare(currentShare);
				w.append(shareHeaderHTML);
			}
			w.append("  " + aResult.toHTML(searchTerm) + "\n");
		}
		w.append("  <p>\n");
		w.append("    " + FileRequest.getFileCountHTML(results.length) + "\n");
		w.append("    " + this.getIndexAgeHTML() + "\n");	
		w.append("  </p>\n");
		w.append("</pre>\n");
		
		return w.toString();
	}
	
	private String showShare(String shareName) {
		return "<span class='searchresultshare'><img src='/folder.gif' class='icon'><a href=\"/shares/"+ shareName + "/\">" + shareName + "</a></span>\n";
	}
	
	private String getIndexAgeHTML() {
		return "<i>Index is " + Searcher.getHumanReadableIndexAge() + " old</i>";
	}
	
}
