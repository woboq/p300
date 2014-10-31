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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.Resources;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.threads.RequestThread;

/**
 * This class does the layouting for us
 * 
 * @author guruz
 * 
 */
public class Layouter {
	static final String baselayoutfile = "de/guruz/p300/requests/static/baselayout.html";

	static final String markerMainDiv = "<!--@p300.maindiv@-->";

	static final String markerTitle = "<!--@p300.title@-->";

	static final String markerGoToIndex = "<!--@p300.gotoindex@-->";

	static final String markerLeftNetworkList = "<!--@p300.leftnetworklist@-->";

	static final String markerLeftShareList = "<!--@p300.leftsharelist@-->";

	static final String markerUserStyleSheet = "<!--@p300.userstylesheet@-->";

	static final String markerTheConfig = "<!--@p300.theconfig@-->";

	static final String markerThisIsWebinterfaceMessage = "<!--@p300.thisiswebinterfacemessage@-->";

	static final String markerSearchString = "@p300.searchstring@";

	protected String result = "";

	protected RequestThread requestThread;

	public Layouter(RequestThread rt) throws Exception {
		InputStream fileStream = Resources.getResourceAsStream (Layouter.baselayoutfile);

		if (fileStream == null) {
			throw new Exception("Cannot find baselayout.html as resource");
		}

		BufferedReader br = new BufferedReader(
				new InputStreamReader(fileStream));

		String line = br.readLine();

		while (line != null) {
			this.result = this.result.concat(line);
			this.result = this.result.concat("\n");
			line = br.readLine();
		}

		this.requestThread = rt;
	}

	public void replace(String key, String value) {
		// System.err.println(">replace: " + key + ", " + value);
		this.result = this.result.replace(key, value);
	}

	public String getBeforeMainDiv() {
		return this.result.substring(0, this.result
				.indexOf(Layouter.markerMainDiv));
	}

	public String getAfterMainDiv() {
		return this.result.substring(this.result
				.indexOf(Layouter.markerMainDiv)
				+ Layouter.markerMainDiv.length());
	}

	public void replaceTitle(String t) {
		this.replace(Layouter.markerTitle, t);
	}

	public void replaceBasicStuff() {
		Configuration conf = Configuration.instance();

		String stylesheet = conf.getUserStyleSheet();

		if (stylesheet.length() > 0) {
			String stylesheetString = "<link rel=\"stylesheet\" type=\"text/css\" href=\""
					+ stylesheet + "\">";
			this.replace(Layouter.markerUserStyleSheet, stylesheetString);
		} else {
			this.replace(Layouter.markerUserStyleSheet, "");
		}

		this.replaceThisIsWebinterfaceMessage();
		this.replaceTheConfig();
		this.replaceTheNetwork();
		// If it's a search, this has already been replaced.
		// If it's not a search, we can safely replace this.
		this.replaceSearchString("Search...");
		this.replaceGoToIndex();
	}

	private void replaceThisIsWebinterfaceMessage() {
		if (!this.requestThread.isAuthenticated()) {
			this
					.replace(
							Layouter.markerThisIsWebinterfaceMessage,
							"<div id='thisiswebinterfacemessage'><p>This is only the webinterface. You should <b><a href='http://p300.eu/download/'>get p300</a></b> for a better user experience :)</p></div>");
		}
	}

	// FIXME: change the logic. the index should tell the layouter that it
	// should do a index, not the
	// other way round
	private void replaceGoToIndex() {
		if (this.requestThread.isIndex()) {
			this.replace(Layouter.markerGoToIndex, "");
		} else {
			this
					.replace(
							Layouter.markerGoToIndex,
							"<p>&nbsp;&nbsp;<img class='icon' src='/back.gif'><a href='/' style=''>index</a><hr></p>");
		}
	}

	public void replaceMainDiv(String r) {
		this.replace(Layouter.markerMainDiv, r);

		// System.err.println ("Layout length after main div replacement is now
		// " + result.length());
	}

	protected void replaceTheNetwork() {
		String dn = this.requestThread.getLocalDisplay();
		String ret = "";

		boolean meWasPrinted = false;

		Host[] hosts = MainDialog.hostMap.getHosts();

		for (Host h : hosts) {
			if (h.seemsOnline()) {
				String h_dn = h.getDisplayName();

				if ((dn.toLowerCase().compareTo(h_dn.toLowerCase()) <= 0)
						&& !meWasPrinted) {
					meWasPrinted = true;
					ret = ret + this.getLocalNameAndShares();
				}

				int rev = h.getSVNRevision();
				String url = h.toURLBestMatchingThisIP(this.requestThread
						.getRemoteIP());
				String hostname = h
						.toHostnameBestMatchingThisIP(this.requestThread
								.getRemoteIP());
				ret = ret
						+ ("&nbsp;&nbsp;<img src='/comp_blue.gif' class='icon'><a href=\""
								+ url
								+ "/\" title=\"revision "
								+ rev
								+ " on "
								+ hostname + "\">" + h.getDisplayName() + "</a><br>");

			}
		}

		if (!meWasPrinted) {
			ret = ret + this.getLocalNameAndShares();
		}

		// return ret;
		this.replace("<!--@p300.thenetwork@-->", ret);
	}

	protected void replaceTheConfig() {
		StringBuilder sb = new StringBuilder();

		if (this.requestThread.isAuthenticated()) {
			sb.append("<hr><p>Configuration:<br>");
			sb
					.append("&nbsp;&nbsp;<a href=\"/config/autoUpdater\">Auto Updater</a><br>");
			sb
					.append("&nbsp;&nbsp;<a href=\"/config/allowedHosts\">Allowed Hosts</a><br>");

			sb
					.append("&nbsp;&nbsp;<a href=\"/config/bandwidth\">Bandwidth</a><br>");
			sb
			.append("&nbsp;&nbsp;<a href=\"/config/multicast\">Discovery</a><br>");
			sb
					.append("&nbsp;&nbsp;<a href=\"/config/downloads\">Downloads</a><br>");

			sb.append("&nbsp;&nbsp;<a href=\"/config/misc\">Misc</a><br>");
			sb.append("&nbsp;&nbsp;<a href=\"/config/search\">Search</a><br>");
			sb.append("&nbsp;&nbsp;<a href=\"/config/shares\">Shares</a><br>");
			sb
					.append("</p><hr><p>&nbsp;&nbsp<a href=\"/logout\">Logout</a></p>");
		} else {
			sb.append("<hr>");
			sb
					.append("<p>Login:<br><form action=\"/login\" method=\"POST\" enctype=\"application/x-www-form-urlencoded\" accept-charset=\"UTF-8\">");

			sb.append("&nbsp;&nbsp;Username:<br>");
			sb
					.append("&nbsp;&nbsp;<input style=\"width:100px;\" type=\"text\" value=\"admin\" name=\"u\"><br>");
			sb.append("&nbsp;&nbsp;Password:<br>");
			sb
					.append("&nbsp;&nbsp;<input style=\"width:100px;\" type=\"password\" value=\"\" name=\"p\"><br>");

			sb.append("&nbsp;&nbsp;<input type=\"submit\" value=\"Login\">");
			sb.append("</form></p>");
		}

		this.replace(Layouter.markerTheConfig, sb.toString());
	}

	protected String openedShare = null;

	public void setOpenedShare(String sn) {
		this.openedShare = sn;
	}

	protected String getLocalNameAndShares() {
		String ret = "";

		ret = ret + "&nbsp;&nbsp;<img src='/comp_blue.gif' class='icon'><b>";
		ret = ret + "<a href='/'>";
		ret = ret + this.requestThread.getLocalDisplay() + "</a></b><br>";

		String shares[] = ShareManager.instance().getShareNames();

		for (String element : shares) {
			String shareDir = Configuration.instance().getDirFromShareName(
					element);
			File shareDirFile = new File(shareDir);
			if (!shareDirFile.exists())
				continue;

			ret = ret.concat("&nbsp;&nbsp;&nbsp;&nbsp;<img src='");

			if ((this.openedShare == null) || !element.equals(this.openedShare)) {
				ret = ret.concat("/folder.gif");
			} else {
				ret = ret.concat("/folder_open.gif");
			}

			ret = ret.concat("' class='icon'><a href=\"/shares/" + element
					+ "/\">");
			ret = ret.concat(element);
			ret = ret.concat("</a><br>\n");
		}

		return ret;
	}

	public void replaceSearchString(String searchString) {
		if (searchString == null) {
			searchString = "Search...";
		}
		this.replace(Layouter.markerSearchString, searchString);
	}

}
