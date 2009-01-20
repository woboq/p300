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

import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.http.HTTPVerb;

/**
 * The about page
 * 
 * @author guruz
 * 
 */
public class HostlistHTMLRequest extends Request {

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (!((rt == HTTPVerb.GET) || (rt == HTTPVerb.HEAD))) {
			return false;
		}

		if (!reqpath.equals("/hostlistHTML")) {
			return false;
		}

		return true;
	}

	@Override
	public void handle() throws Exception {
		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpContentType("text/html");
		this.requestThread.httpContents();

		String dn = this.requestThread.getLocalDisplay();
		boolean meWasPrinted = false;
		String ret = "";

		Host[] hosts = MainDialog.hostMap.getHosts();

		for (Host h : hosts) {
			if (h.seemsOnline()) {
				String h_dn = h.getDisplayName();

				if ((dn.toLowerCase().compareTo(h_dn.toLowerCase()) <= 0)
						&& !meWasPrinted) {
					meWasPrinted = true;
					ret = ret + myLink();
				}

				String url = h.toURLBestMatchingThisIP(this.requestThread
						.getRemoteIP());
				String hostname = h
						.toHostnameBestMatchingThisIP(this.requestThread
								.getRemoteIP());
				ret = ret
						+ ("<a target='_top' href=\"" + url + "/\" title=\"" + hostname
								+ "\">" + h.getDisplayName() + "</a><br>");

			}
		}

		if (!meWasPrinted) {
			ret = ret + myLink();
		}

		this.requestThread.write("<div id='p300_hosts_" +  dn + "'>");
		this.requestThread.write(ret);
		this.requestThread.write("</div>");
		
		this.requestThread.flush();
		this.requestThread.close();

	}

	private String myLink() {
		return "<a href=\"http://" + this.requestThread.getLocalIP() + ":"
				+ this.requestThread.getLocalPort() + "\">"
				+ this.requestThread.getLocalDisplay() + "</a><br>";
	}

}
