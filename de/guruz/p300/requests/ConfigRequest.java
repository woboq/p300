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

import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.allowing.HostAllowanceManager;
import de.guruz.p300.hosts.allowing.UnallowedHosts;
import de.guruz.p300.hosts.httpmulticast.HTTPMulticastHost;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.search.Indexer;
import de.guruz.p300.search.IndexerThread;
import de.guruz.p300.search.IndexerThread.IntervalLevel;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.threads.RequestThread;
import de.guruz.p300.utils.OsUtils;
import de.guruz.p300.utils.RandomGenerator;
import de.guruz.p300.utils.URL;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;
import de.guruz.p300.windowui.actions.AddHostAction;

/**
 * The configuration webpage
 * 
 * @author guruz
 * 
 */
public class ConfigRequest extends Request {
	String subconfig = "";

	boolean authenticated;

	Configuration conf;
	
	/**
	 * Protects against CSRF/XSRF
	 * http://en.wikipedia.org/wiki/Cross-site_request_forgery
	 */
	protected static String m_authKey = RandomGenerator.string();

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (!((rt == HTTPVerb.GET) || (rt == HTTPVerb.HEAD))) {
			return false;
		}

		if (!reqpath.startsWith("/config")) {
			return false;
		}

		return true;
	}

	public void shareConfig() throws Exception {
		if (this.subconfig.equals("/shares")
				|| this.subconfig.equals("/shares/")) {
			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpAuth("p300");
			this.requestThread.httpContents();

			Layouter layouter = new Layouter(this.requestThread);
			layouter.replaceTitle("Share configuration");
			layouter.replaceBasicStuff();

			this.requestThread.write(layouter.getBeforeMainDiv());

			this.requestThread
					.write("<form action='/config/shares/add' method='GET' accept-charset='UTF-8'><table border=2><tr><th>Name</th><th>Directory</th><th>Operations</th></tr>\n");

			String shares[] = ShareManager.instance().getShareNames();
			if (shares.length > 0) {
				for (String element : shares) {
					this.requestThread.write("<tr><td>" + element + "</td>");
					this.requestThread.write("<td>"
							+ ShareManager.instance().getShare(element)
									.getFileLocation() + "</td>");
					this.requestThread.write("<td>");
					this.requestThread.write("<a href=\"/shares/" + element
							+ "/\">visit</a>&nbsp;&nbsp;");
					this.requestThread
							.write("<a href=\"/config/shares/unshare?name="
									+ element + getAuthKeyAsUrlParamAtEnd () + "\">unshare</a>");
					this.requestThread.write("</td>");
					this.requestThread.write("</tr>");
				}

			}

			this.requestThread.write("<tr>");
			this.requestThread
					.write("<td><input type='text' name='name' value='' length='50'></td>");
			this.requestThread
					.write("<td><input type='text' name='dir' value='' length='100'></td>");
			this.requestThread
					.write("<td><input type='submit' name='submit' value='Add'></td>");
			
			writeAuthKeyFormField (this.requestThread);
			
			this.requestThread.write("</tr></table></form>");

			this.requestThread
					.write("<ul><li>Files outside of the sharedirs will not be shared, this includes symlinked directories and files</li>");
			this.requestThread
					.write("<li>The size of files (not directories!) is shown as a tooltip over the filenames</li>");
			this.requestThread.write("</ul>");

			this.requestThread.write(layouter.getAfterMainDiv());

			this.requestThread.flush();
			this.requestThread.close();
		} else if (this.subconfig.startsWith("/shares/add?")) {

			String name = URL.extractParameter(this.subconfig, "name");
			String dir = URL.extractParameter(this.subconfig, "dir");

			if ((name != null) && (dir != null)) {
				ShareManager.instance().addShare(name, dir);
			}

			if (MainDialog.indexerThread != null) {
				IndexerThread.restartIndexer();
			}

			this.requestThread.close(302, "OK", "/config/shares");

		} else if (this.subconfig.startsWith("/shares/unshare?")) {
			String name = URL.extractParameter(this.subconfig, "name");

			if (name != null) {
				ShareManager.instance().removeShare(name);
			}

			if (MainDialog.indexerThread != null) {
				IndexerThread.restartIndexer();
			}

			this.requestThread.close(302, "OK", "/config/shares");
		}
	}

	public void allowedHostsConfig() throws Exception {
		if (this.subconfig.equals("/allowedHosts")
				|| this.subconfig.equals("/allowedHosts/")) {

			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpAuth("p300");
			this.requestThread.httpContents();

			Layouter layouter = new Layouter(this.requestThread);
			layouter.replaceTitle("Allowed hosts configuration");
			layouter.replaceBasicStuff();
			this.requestThread.write(layouter.getBeforeMainDiv());

			if (!conf.isShowAdvancedOptions()) {
				this.requestThread
						.write("By default, all p300 hosts that were detected as local ");
				this.requestThread
						.write("are allowed. This is detected using multicast and broadcast.\n");
				this.requestThread
						.write("You have to <a href='/config/misc'>enable the advanced options</a> to change this.");
			} else {

				HostAllowanceManager hostAllowanceManager = MainDialog
						.getHostAllowanceManager();

				this.requestThread.write("<h3>Already allowed prefixes</h3>");

				this.requestThread
						.write("<form action='/config/allowedHosts/allow' method='GET' accept-charset='UTF-8'><table border=2><tr><th>IP Space</th><th>Operations</th></tr>\n");

				for (String element : hostAllowanceManager
						.getExplicitlyAllowedIps()) {
					this.requestThread.write("<tr><td>" + element + "</td>");
					this.requestThread.write("<td>");
					this.requestThread
							.write("<a href=\"/config/allowedHosts/unallow?host="
									+ element + getAuthKeyAsUrlParamAtEnd () + "\">unallow</a>");
					this.requestThread.write("</td>");
					this.requestThread.write("</tr>");
				}

				if (hostAllowanceManager.isImplicitAllowOn()) {
					for (String element : hostAllowanceManager
							.getImplicitlyAllowedIps()) {
						this.requestThread
								.write("<tr><td>" + element + "</td>");
						this.requestThread.write("<td>");
						this.requestThread.write("implicitly allowed");
						this.requestThread.write("</td>");
						this.requestThread.write("</tr>");
					}
				}

				this.requestThread.write("<tr>");
				this.requestThread
						.write("<td><input type='text' name='host' value='' length='50'></td>");
				this.requestThread
						.write("<td><input type='submit' name='submit' value='Allow'></td>");
				writeAuthKeyFormField (this.requestThread);
				this.requestThread.write("</tr></table></form>");

				this.requestThread.write("<ul>");

				if (hostAllowanceManager.isImplicitAllowOn()) {
					this.requestThread
							.write("<li>Hosts detected as LAN or VPN are <b>implicitly allowed</b> ");
				} else {
					this.requestThread
							.write("<li>Hosts detected as LAN or VPN are <b>not implicitly allowed</b> ");
				}
				// FIXME implicit toggle buton
				this.requestThread
						.write("(<a href=\"/config/allowedHosts/implicitAllow?toggle" + getAuthKeyAsUrlParamAtEnd() +" \">change</a>)");
				this.requestThread.write("</li>");

				this.requestThread
						.write("<li>Specify the IP like 192.168.0.1 for a host or like 192.168.0. to allow the whole subnet to connect</li>");
				this.requestThread
						.write("<li>Localhost connections (and connections from an own IP) are always allowed</li><li>Hostnames are not supported!</li>");
				this.requestThread
						.write("<li>Make sure port 4337 incoming TCP/UDP is allowed in your firewall</li>");
				this.requestThread
						.write("<li>If nothing seems to work, try <a href=\"/config/multicast/http\">manual discovery</a></li>");
				this.requestThread
						.write("<li>You can include the hostlist of p300 on your portal website for example with an iframe or AJAX request pointing <a href='/hostlistHTML'>here</a></li>");
				this.requestThread.write("</ul>");

				this.requestThread.write("<h3>Possible candidates</h3>");
				this.requestThread
						.write("<table border=2><tr><th>IP Space</th><th>Device</th><th>Operations</th></tr>\n");

				Enumeration<NetworkInterface> nifs = java.net.NetworkInterface
						.getNetworkInterfaces();

				while (nifs.hasMoreElements()) {
					NetworkInterface nif = nifs.nextElement();
					Enumeration<InetAddress> ips = nif.getInetAddresses();
					while (ips.hasMoreElements()) {
						InetAddress current_ip = ips.nextElement();
						if (current_ip instanceof Inet4Address) {
							String ip = current_ip.getHostAddress();

							if (de.guruz.p300.utils.IP.isLocalhostIP(ip)) {
								continue;
							}

							if (de.guruz.p300.utils.IP.isOurIP(ip)) {
								continue;
							}

							String ipr = ip.substring(0,
									ip.lastIndexOf('.') + 1);

							if (!MainDialog.getHostAllowanceManager()
									.isIpAllowed(ipr)) {
								this.requestThread.write("<tr><td>" + ipr
										+ "</td>");
								this.requestThread.write("<td>"
										+ nif.getDisplayName() + "</td>");
								this.requestThread.write("<td>");
								this.requestThread
										.write("<a href=\"/config/allowedHosts/allow?host="
												+ ipr + getAuthKeyAsUrlParamAtEnd () +"\">allow</a>");
								this.requestThread.write("</td>");
								this.requestThread.write("</tr>");
							}
						}
					}
				}

				String lastUnallowedReceived[] = UnallowedHosts.get();
				for (String current : lastUnallowedReceived) {
					if (!MainDialog.getHostAllowanceManager().isIpAllowed(
							current)) {
						this.requestThread
								.write("<tr><td>" + current + "</td>");
						this.requestThread.write("<td></td>");
						this.requestThread.write("<td>");
						this.requestThread
								.write("<a href=\"/config/allowedHosts/allow?host="
										+ current + getAuthKeyAsUrlParamAtEnd () + "\">allow</a>");
						this.requestThread.write("</td>");
						this.requestThread.write("</tr>");
					}
				}
			}

			this.requestThread.write(layouter.getAfterMainDiv());

			this.requestThread.flush();
			this.requestThread.close();

		} else if (this.subconfig.startsWith("/allowedHosts/allow?")) {
			String host = URL.extractParameter(this.subconfig, "host");

			if (host != null) {
				this.conf.setIpExplicitlyAllowed(host, true);

				de.guruz.p300.MainDialog.multicastListenThread.sendPingToAll();
			}

			this.requestThread.close(302, "OK", "/config/allowedHosts/");

		} else if (this.subconfig.startsWith("/allowedHosts/unallow?")) {
			String host = URL.extractParameter(this.subconfig, "host");

			if (host != null) {
				this.conf.setIpExplicitlyAllowed(host, false);
			}

			this.requestThread.close(302, "OK", "/config/allowedHosts/");
		} else if (this.subconfig.equals("/allowedHosts/implicitAllow?toggle")) {
			conf.setLocalNetworkIpsImplicitlyAllowed(!conf
					.isLocalNetworkIpsImplcitlyAllowed());

			this.requestThread.close(302, "OK", "/config/allowedHosts/");
		}
	}

	public void multicastConfig() throws Exception {
		if (this.subconfig.equals("/multicast")
				|| this.subconfig.equals("/multicast/")) {

			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpAuth("p300");
			this.requestThread.httpContents();

			Layouter layouter = new Layouter(this.requestThread);
			layouter.replaceTitle("Discovery of other p300 nodes");
			layouter.replaceBasicStuff();
			this.requestThread.write(layouter.getBeforeMainDiv());

			this.requestThread.write("<p><ul>");
			this.requestThread
					.write("<li>Discovery of other hosts should happen automatically</li>");
			if (!Configuration.instance().isLocalNetworkIpsImplcitlyAllowed()) {
				this.requestThread
						.write("<li>You need to <a href='/config/allowedHosts'>allow</a> the IPs that should be able to connect to you</li>");
			}
			this.requestThread.write("<li>Only IPv4 will be used</li>");

			this.requestThread
					.write("<li>The network device needs to have multicast enabled on the OS</li>");
			this.requestThread
					.write("<li>Make sure port 4337 incoming TCP/UDP is allowed in your firewall</li>");
			this.requestThread
					.write("<li>The multicast route has to be set on some OS</li>");

			this.requestThread
					.write("<li>If host discovery does not work because of local firewalls or broken VPN software, use ");
			this.requestThread
					.write("<a href=\"/config/multicast/http\">manual discovery</a></li>");

			this.requestThread.write("</ul></p>");

			this.requestThread.write(layouter.getAfterMainDiv());

			this.requestThread.flush();
			this.requestThread.close();

		} else if (this.subconfig.equals("/multicast/http")) {
			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpAuth("p300");
			this.requestThread.httpContents();

			Layouter layouter = new Layouter(this.requestThread);
			layouter.replaceTitle("Multicast configuration");
			layouter.replaceBasicStuff();
			this.requestThread.write(layouter.getBeforeMainDiv());

			this.requestThread.write("<p>");

			this.requestThread.write("<h2>Multicast over HTTP</h2>");

			// HTTPMulticastHost hosts[] = MainDialog.hostFinderThread
			// .getWorkingHosts();
			// this.printHTTPMulticastHosts("Working hosts", hosts);
			// hosts = MainDialog.hostFinderThread.getNonWorkingHosts();
			// this.printHTTPMulticastHosts("Nonworking hosts", hosts);

			this.requestThread.write("<ul>");
			this.requestThread
					.write("<li>Add a IP:Port combination or http:// URL of a host on your network that is already running p300");

			this.requestThread
					.write("<form action='/config/multicast/http' method='GET' accept-charset='UTF-8'>");
			this.requestThread
					.write("<input type='text' name='hostip' size='22' value=''>");
			this.requestThread
					.write("<input type='submit' name='submit' value='Add'></td>");
			writeAuthKeyFormField(this.requestThread);
			this.requestThread
			.write("</form>");

			this.requestThread.write("</li>");
			this.requestThread.write("</ul>");

			this.requestThread.write("</p>");

			this.requestThread.write(layouter.getAfterMainDiv());

			this.requestThread.flush();
			this.requestThread.close();
			return;

		} else if (this.subconfig.startsWith("/multicast/http?")) {
			String hostip = URL.extractParameter(this.subconfig, "hostip");
			// if (hostip == null)
			// hostip = "";
			// conf.setUserStyleSheet(stylesheet);

			if (hostip != null) {
				new AddHostAction(hostip).actionPerformed(new ActionEvent(this,
						0, null));
				// give ourselves some time so we can immediatly let the user
				// see the results
				Thread.sleep(1500);
			}

			this.requestThread.close(302, "OK", "/config/multicast/http");
			// give ourselves some time so we can immediatly let the user
			// see the results
			Thread.sleep(1500);
		}
	}

	protected void printHTTPMulticastHosts(String title,
			HTTPMulticastHost hosts[]) throws Exception {
		if (hosts.length == 0) {
			return;
		}

		this.requestThread.write("<h3>");
		this.requestThread.write(title);
		this.requestThread.write("</h3>");

		this.requestThread.write("<table border=\"1\">");
		this.requestThread.write("<tr>");

		this.requestThread.write("<td>");
		this.requestThread.write("<b>host</b>");
		this.requestThread.write("</td>");

		this.requestThread.write("<td>");
		this.requestThread.write("<b>last query</b>");
		this.requestThread.write("</td>");

		this.requestThread.write("<td>");
		this.requestThread.write("<b>state</b>");
		this.requestThread.write("</td>");

		this.requestThread.write("</tr>");

		java.util.Arrays.sort(hosts);

		for (HTTPMulticastHost current : hosts) {
			this.requestThread.write("<tr>");
			this.requestThread.write("<td>");
			this.requestThread.write(current.getHostPort());
			this.requestThread.write("</td>");
			this.requestThread.write("<td>");
			this.requestThread.write(current.lastQueryToString());
			this.requestThread.write("</td>");
			this.requestThread.write("<td>");
			this.requestThread.write(current.stateToString());
			this.requestThread.write("</td>");
			this.requestThread.write("</tr>\n");
		}

		this.requestThread.write("</table>\n");
	}

	public void miscConfig() throws Exception {
		if (this.subconfig.equals("/misc") || this.subconfig.equals("/misc/")) {

			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpAuth("p300");
			this.requestThread.httpContents();

			Layouter layouter = new Layouter(this.requestThread);
			layouter.replaceTitle("Misc configuration");
			layouter.replaceBasicStuff();
			this.requestThread.write(layouter.getBeforeMainDiv());

			this.requestThread
					.write("<form action='/config/misc/set' method='GET' name='miscconfigform' accept-charset='UTF-8'>");

			// local display name
			this.requestThread.write("<h3>Name of this p300 node</h3>");
			String displayName = this.conf.getLocalDisplayName();
			this.requestThread
					.write("<input type='text' name='displayName' size='100' value='"
							+ displayName + "'>");

			this.requestThread
					.write("<ul><li>Please note that other p300 nodes will only see this after they restart p300.</li></ul><br><br><br><hr>");

			// stylesheet
			if (this.conf.isShowAdvancedOptions()) {
				this.requestThread
						.write("<h3>User-defined stylesheet URL</h3>");
				String stylesheet = this.conf.getUserStyleSheet();
				this.requestThread
						.write("<input type='text' name='stylesheet' size='100' value='"
								+ stylesheet + "'>");

				this.requestThread
						.write("<ul><li>You can get the default stylesheet <a href='/layout.css'>here</a>. Test your custom stylesheet; Some attributes might not work exactly the way expected because of the default style.</li></ul><br><br><br><hr>");
			}

			// advanced options
			this.requestThread.write("<h3>Advanced options</h3>");
			boolean showAdvanced = this.conf.isShowAdvancedOptions();
			this.requestThread
					.write("<input type='checkbox' name='showAdvancedOptions' size='100' "
							+ (showAdvanced ? "checked" : "")
							+ " value=\"true\">Show advanced options</input>");

			this.requestThread.write("<br><br><br><hr>");

			// upload log
			if (this.conf.isShowAdvancedOptions()) {
				this.requestThread.write("<h3>HTTP upload log</h3>");
				String uploadLog = this.conf.getHTTPuploadLog();
				this.requestThread
						.write("<input type='text' name='uploadLog' size='100' value='"
								+ uploadLog + "'>");

				// access log
				this.requestThread.write("<h3>HTTP access log</h3>");
				String accessLog = this.conf.getHTTPaccessLog();
				this.requestThread
						.write("<input type='text' name='accessLog' size='100' value='"
								+ accessLog + "'>");

				// access log
				this.requestThread.write("<h3>HTTP error log</h3>");
				String errorLog = this.conf.getHTTPerrorLog();
				this.requestThread
						.write("<input type='text' name='errorLog' size='100' value='"
								+ errorLog + "'>");
				this.requestThread
						.write("<ul><li>%HOME% in filenames gets replaced with user home directory</li>");
				this.requestThread.write("<li>%HOME% is "
						+ System.getProperty("user.home") + "</li>");
				this.requestThread
						.write("<li>Changing this setting needs a restart of p300</li></ul>");

				this.requestThread.write("<br><br><br><hr>");
			}

			this.requestThread.write(this.showUnixBrowserConfig(this.conf));

			this.requestThread
					.write("<br><input type='submit' name='submit' value='Set'></td>");
			writeAuthKeyFormField (this.requestThread);
			this.requestThread.write("</form>");

			this.requestThread.write(layouter.getAfterMainDiv());

			this.requestThread.flush();
			this.requestThread.close();

		} else if (this.subconfig.startsWith("/misc/set?")) {
			String stylesheet = URL.extractParameter(this.subconfig,
					"stylesheet");
			if (stylesheet == null) {
				stylesheet = "";
			}
			this.conf.setUserStyleSheet(stylesheet);

			// showAdvancedOptions
			String showAdvancedOptions = URL.extractParameter(this.subconfig,
					"showAdvancedOptions");
			if (showAdvancedOptions != null) {
				this.conf.setShowAdvancedOptions(true);
			} else {
				this.conf.setShowAdvancedOptions(false);
			}

			String uploadLog = URL
					.extractParameter(this.subconfig, "uploadLog");
			if (uploadLog != null) {
				this.conf.setHTTPuploadLog(uploadLog);
			}

			String accessLog = URL
					.extractParameter(this.subconfig, "accessLog");
			if (accessLog != null) {
				this.conf.setHTTPaccessLog(accessLog);
			}

			String errorLog = URL.extractParameter(this.subconfig, "errorLog");
			if (errorLog != null) {
				this.conf.setHTTPerrorLog(errorLog);
			}

			String displayName = URL.extractParameter(this.subconfig,
					"displayName");
			if (displayName != null) {
				this.conf.setLocalDisplayName(displayName);
			}

			String iconType = URL.extractParameter(this.subconfig, "icontype");
			if (iconType != null) {
				this.conf.setIconType(Integer.parseInt(iconType));
			}

			String browserCommand = URL.extractParameter(this.subconfig,
					"unixBrowser");
			if (browserCommand != null)
				this.setUnixBrowser(browserCommand);

			this.requestThread.close(302, "OK", "/config/misc/");

		}
	}


	public void bandwidthConfig() throws Exception {
		if (this.subconfig.equals("/bandwidth")
				|| this.subconfig.equals("/bandwidth/")) {

			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpAuth("p300");
			this.requestThread.httpContents();

			Layouter layouter = new Layouter(this.requestThread);
			layouter.replaceTitle("Bandwidth configuration");
			layouter.replaceBasicStuff();
			this.requestThread.write(layouter.getBeforeMainDiv());

			this.requestThread
					.write("<form action='/config/bandwidth/set' method='GET' accept-charset='UTF-8'><table border=2><tr><th>Kind</th><th>Limit in KB/sec</th></tr>\n");

			this.requestThread.write("<h3>Limits</h3>");
			this.requestThread.write("<tr><td>Uploads</td>");
			this.requestThread.write("<td>");
			this.requestThread.write("<input type='text' name='out' value='"
					+ Configuration.instance().getOutputBWLimitInKB() + "'>");
			this.requestThread.write("</td>");
			this.requestThread.write("</tr>");

			this.requestThread.write("</table>");
			this.requestThread
					.write("<ul><li>0 means no limit</li><li>256 is the highest limit possible</li><li>Localhost connections are never limited</li></ul>");

			this.requestThread.write("<h3>Limit for which hosts?</h3>");
			this.requestThread
					.write("<table border='2'><tr><th>IP prefix</th><th>Limit?</th></tr>");
			List<String> hosts = MainDialog.getHostAllowanceManager()
					.getAllAllowedIps();
			String unlimited[] = this.conf.getUnlimitedIps();
			for (String element : hosts) {
				boolean hostUnlimited = false;
				for (String element0 : unlimited) {
					if (element.equals(element0)) {
						hostUnlimited = true;
					}
				}
				this.requestThread.write("<tr><td>" + element
						+ "</td><td><input type='checkbox' name='" + element
						+ "' value='1' "
						+ (!hostUnlimited ? "checked='checked'" : "")
						+ "/></td></tr>");
			}
			this.requestThread.write("</table>");
			this.requestThread
					.write("<ul><li>Longest address prefixes match first</li><li>Add an address to the allowed hosts list to fine-tune your configuration</li></ul>");

			// this.requestThread.write("<h3>Type of Service flag</h3>");
			// byte TOS = this.conf.getTOSFlag();
			// this.requestThread
			// .write("<input type='text' name='TOS' size='4' value='"
			// + TOS + "'>");
			//
			// this.requestThread
			// .write("<ul><li>0 = default, 4 = maximize throughput, 8 =
			// minimize delay</li>");
			// this.requestThread
			// .write("<li>Applies only to new downloads</li></ul>");

			this.requestThread.write("<br><br><br><hr>");
			writeAuthKeyFormField (this.requestThread);
			this.requestThread
					.write("<input type='submit' name='submit' value='Set'></form>");

			this.requestThread.write(layouter.getAfterMainDiv());

			this.requestThread.flush();
			this.requestThread.close();

		} else if (this.subconfig.startsWith("/bandwidth/set?")) {
			{
				// upload bw limit
				String bwOut = URL.extractParameter(this.subconfig, "out");
				int bwOutInt = 0;
				try {
					bwOutInt = Integer.parseInt(bwOut);
				} catch (Exception e) {
					bwOutInt = 0;
				}
				this.conf.setOutputBWLimitInKb(bwOutInt);
			}

			{
				// unlimited hosts
				ArrayList<String> unlimitedHosts = new ArrayList<String>();
				String hosts[] = this.conf.getExplicitlyAllowedIps();
				for (String element : hosts) {
					String param = URL
							.extractParameter(this.subconfig, element);
					// if (param != null && !param.equals(
					// / "1")) {
					if (param == null) {
						unlimitedHosts.add(element);
					}
				}
				this.conf.setUnlimitedIps(unlimitedHosts
						.toArray(new String[unlimitedHosts.size()]));
			}

			{
				// TOS flag
				String TOS = URL.extractParameter(this.subconfig, "TOS");
				int TOSint = 0;
				try {
					TOSint = Integer.parseInt(TOS);
				} catch (Exception e) {
					TOSint = 0;
				}
				this.conf.setTOSFlag((byte) TOSint);
			}

			this.requestThread.close(302, "OK", "/config/bandwidth/");
		}
	}

	public void downloadsConfig() throws Exception {
		if (this.subconfig.equals("/downloads")
				|| this.subconfig.equals("/downloads/")) {
			this.showDownloadsConfig();
		} else if (this.subconfig.startsWith("/downloads/set?")) {
			this.setDownloadsConfig();
		}
	}

	private void setDownloadsConfig() throws Exception {
		String finishedDownloadDir = URL.extractParameter(this.subconfig,
				"finishedDownloadDir");
		if (finishedDownloadDir != null)
			this.conf.setFinishedDownloadDir(finishedDownloadDir);

		this.requestThread.close(302, "OK", "/config/downloads/");
	}

	private void showDownloadsConfig() throws Exception {

		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpAuth("p300");
		this.requestThread.httpContents();

		Layouter layouter = new Layouter(this.requestThread);
		layouter.replaceTitle("Downloads configuration");
		layouter.replaceBasicStuff();
		this.requestThread.write(layouter.getBeforeMainDiv());
		this.requestThread
				.write("<form action=\"/config/downloads/set\" method=\"GET\" accept-charset=\"UTF-8\">\n");

		this.requestThread.write("<h3>Directory for finished downloads</h3>");
		String finishedDownloadDir = this.conf.getFinishedDownloadDir();
		this.requestThread
				.write("<input type='text' name='finishedDownloadDir' size='100' value='"
						+ finishedDownloadDir + "'>");
		this.requestThread
				.write("<ul><li>%HOME% in filenames gets replaced with user home directory</li>");
		this.requestThread.write("<li>%HOME% is "
				+ System.getProperty("user.home") + "</li>");
		this.requestThread
				.write("<li>Changing this setting needs a restart of p300</li></ul>");

		this.requestThread.write("<br><br><br><hr>");

		this.requestThread.write("  <input type=\"submit\" value=\"Set\">\n");
		writeAuthKeyFormField (this.requestThread);
		this.requestThread.write("</form>\n");

		this.requestThread.write(layouter.getAfterMainDiv());

		this.requestThread.flush();
		this.requestThread.close();

	}

	public void searchConfig() throws Exception {
		if (this.subconfig.equals("/search")
				|| this.subconfig.equals("/search/")) {
			this.showSearchConfig();
		} else if (this.subconfig.startsWith("/search/setSearch?")) {
			this.setSearchConfig();
		}
	}

	public void showSearchConfig() throws Exception {
		int currentIndexerSpeed = Configuration.instance().getIndexerSpeed();
		int availableSpeedTypes = Indexer.speedNames.length;

		IntervalLevel currentLevel = Configuration.instance()
				.getIndexerInterval();

		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpAuth("p300");
		this.requestThread.httpContents();

		Layouter layouter = new Layouter(this.requestThread);
		layouter.replaceTitle("Search configuration");
		layouter.replaceBasicStuff();
		this.requestThread.write(layouter.getBeforeMainDiv());

		this.requestThread
				.write("<form action=\"/config/search/setSearch\" method=\"GET\" accept-charset=\"UTF-8\">\n");

		if (this.conf.isShowAdvancedOptions()) {
			this.requestThread.write("<h1>Indexer speed</h1><br>\n");
			this.requestThread.write("  <p>\n");
			this.requestThread.write("    <select name=\"indexerSpeed\">\n");
			for (int i = 0; i < availableSpeedTypes; i++) {
				String selected = "";
				if (i == currentIndexerSpeed) {
					selected = " SELECTED";
				}
				String speedName = Indexer.speedNames[i];
				if (i == Indexer.defaultSpeedValue) {
					speedName += " (Default)";
				}
				this.requestThread.write("      <option value=\""
						+ String.valueOf(i) + '"' + selected + '>' + speedName
						+ "</option>\n");
			}
			this.requestThread.write("    </select>\n");
			this.requestThread.write("  </p>\n");
		}
		this.requestThread.write("<h1>Indexer interval</h1></br>\n");
		this.requestThread.write("  <p>\n");
		this.requestThread.write("    <select name=\"indexerInterval\">\n");
		for (IntervalLevel level : IntervalLevel.values()) {
			String selected = "";
			if (level == currentLevel) {
				selected = " SELECTED";
			}
			String intervalName = level.toString();
			if (level == IndexerThread.defaultInterval) {
				intervalName += " (Default)";
			}
			this.requestThread.write("      <option value=\"" + level.name()
					+ '"' + selected + '>' + intervalName + "</option>\n");
		}
		this.requestThread.write("    </select>");
		this.requestThread.write("  </p>\n");
		this.requestThread.write("  <br>\n");
		requestThread.write(" <p>\n");
		requestThread.write(" <ul>\n");
		requestThread
				.write(" <li>Indexer will always run when p300 is started</li>\n");
		// requestThread.write(" <li>Both settings will be applied
		// immediately.</li>\n");
		// requestThread.write(" <li>Indexer will finish before shutting down if
		// &quot;Only run when changing shares&quot; is selected.</li>\n");
		requestThread.write(" </ul>\n");
		requestThread.write(" </p>\n");
		requestThread.write(" <br>\n");
		this.requestThread.write("  <hr>\n");
		this.requestThread.write("  <input type=\"submit\" value=\"Set\">\n");
		writeAuthKeyFormField (this.requestThread);
		this.requestThread.write("</form>\n");

		this.requestThread.write(layouter.getAfterMainDiv());

		this.requestThread.flush();
		this.requestThread.close();
	}

	public void setSearchConfig() throws Exception {
		String newIndexerSpeedFromUserString = URL.extractParameter(
				this.subconfig, "indexerSpeed");
		if (newIndexerSpeedFromUserString != null) {
			int newIndexerSpeedFromUser;
			try {
				newIndexerSpeedFromUser = Integer
						.parseInt(newIndexerSpeedFromUserString);
			} catch (NumberFormatException nfe) {
				newIndexerSpeedFromUser = Configuration.instance()
						.getIndexerSpeed();
			}
			int newIndexerSpeed = Indexer.defaultSpeedValue;
			if ((newIndexerSpeedFromUser >= 0)
					&& (newIndexerSpeedFromUser < Indexer.speedNames.length)) {
				newIndexerSpeed = newIndexerSpeedFromUser;
			}

			Configuration.instance().setIndexerSpeed(newIndexerSpeed);
			Indexer.setSpeedLevel(newIndexerSpeed);
		}

		String newIndexerIntervalFromUserString = URL.extractParameter(
				this.subconfig, "indexerInterval");
		if (newIndexerIntervalFromUserString != null) {
			IntervalLevel newIndexerInterval;
			try {
				newIndexerInterval = Enum.valueOf(IntervalLevel.class,
						newIndexerIntervalFromUserString);
			} catch (IllegalArgumentException iae) {
				newIndexerInterval = Configuration.instance()
						.getIndexerInterval();
			}

			Configuration.instance().setIndexerInterval(newIndexerInterval);
			IndexerThread.setIndexInterval(newIndexerInterval);

		}

		this.requestThread.close(302, "OK", "/config/search/");
	}

	@Override
	public void handle() throws Exception {
		String path = this.requestThread.path;
		
	
		this.subconfig = path.substring(7);
		this.authenticated = this.requestThread.isAuthenticated();
		this.conf = Configuration.instance();

		if (!this.authenticated) {
			this.requestThread.close(401, "Unauthorized, not admin", "/");
		} else {
			if (path.contains("?"))
			{
				// check for CSRF/XSRF
				String suppliedAuthKey = URL.extractParameter(path, "authKey");
				if (! m_authKey.equals(suppliedAuthKey))
				{
					this.requestThread.close(401, "Unauthorized, wrong auth key.", "/");
					return;
				}
			}
			
			
			if (this.subconfig.length() <= 1) {
				this.requestThread.close(302, "OK", "/");
			} else if (this.subconfig.startsWith("/shares")) {
				this.shareConfig();
			} else if (this.subconfig.startsWith("/allowedHosts")) {
				this.allowedHostsConfig();
			} else if (this.subconfig.startsWith("/downloads")) {
				this.downloadsConfig();
			} else if (this.subconfig.startsWith("/multicast")) {
				this.multicastConfig();
			} else if (this.subconfig.startsWith("/bandwidth")) {
				this.bandwidthConfig();
			} else if (this.subconfig.startsWith("/misc")) {
				this.miscConfig();
			} else if (this.subconfig.startsWith("/search")) {
				this.searchConfig();
			} else if (this.subconfig.startsWith("/autoUpdater")) {
				this.autoUpdaterConfig();
			} else {
				this.requestThread.close(404, "Not found");
			}
		}

	}

	protected void autoUpdaterConfig() throws Exception {
		if (this.subconfig.equals("/autoUpdater")
				|| this.subconfig.equals("/autoUpdater/")) {
			this.showAutoUpdaterConfig();
		} else if (this.subconfig.startsWith("/autoUpdater/checkNow")) {

			Runnable r = new Runnable() {

				public void run() {
					de.guruz.p300.MainDialog.newVersionNotificationThread
							.doCheck();
				}
			};

			new Thread(r).start();

			Thread.sleep(10 * 1000);

			this.requestThread.close(302, "OK", "/config/autoUpdater/");

		} else if (this.subconfig.startsWith("/autoUpdater/set?")) {
			this.setAutoUpdaterConfig();
		}
	}

	private void setAutoUpdaterConfig() throws Exception {
		// try latest revision when starting?
		String runLatest = URL.extractParameter(this.subconfig, "runLatest");
		this.conf.setRunLatest((runLatest != null ? true : false));

		// do auto updates?
		String tryUpdateEveryWeek = URL.extractParameter(this.subconfig,
				"tryUpdateEveryWeek");
		this.conf.setTryUpdateEveryWeek(tryUpdateEveryWeek != null ? true
				: false);

		this.requestThread.close(302, "OK", "/config/autoUpdater/");
	}

	private void showAutoUpdaterConfig() throws Exception {
		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpAuth("p300");
		this.requestThread.httpContents();

		Layouter layouter = new Layouter(this.requestThread);
		layouter.replaceTitle("Auto Updater configuration");
		layouter.replaceBasicStuff();
		this.requestThread.write(layouter.getBeforeMainDiv());

		if (this.conf.isShowAdvancedOptions()) {
			this.requestThread
					.write("<form method='GET' action='/config/autoUpdater/set' accept-charset='UTF-8'>");

			boolean runLatest = Configuration.instance()
					.getAlwaysRunLatestVersion();
			boolean tryUpdateEveryWeek = Configuration.instance()
					.getTryUpdateEveryWeek();

			this.requestThread
					.write("<input type='checkbox' name='runLatest' value='yes' "
							+ (runLatest ? "checked" : "")
							+ ">Always run latest locally stored version<br>");
			this.requestThread
					.write("<input type='checkbox' name='tryUpdateEveryWeek' value='yes' "
							+ (tryUpdateEveryWeek ? "checked" : "")
							+ ">Try to update every week<br>");

			this.requestThread.write("<br><br><hr>");
			this.requestThread.write("<input type='submit' value='Set'");
			writeAuthKeyFormField (this.requestThread);
			this.requestThread.write("</form>");
		} else {
			this.requestThread
					.write("You have to <a href='/config/misc'>enable the advanced options</a> to change auto updater settings.");
		}

		this.requestThread.write("<br><br><hr>");

		long lastRetrieval = Configuration.instance()
				.getLastRevisionRetrieval();
		this.requestThread.write("<br>Last check: ");
		if (lastRetrieval == 0) {
			this.requestThread.write("never");
		} else {
			this.requestThread.write(new Date(lastRetrieval).toString());
		}
		this.requestThread.write("<br>");
		this.requestThread
				.write("<form method='GET' action='/config/autoUpdater/checkNow' accept-charset='UTF-8'>");
		this.requestThread.write("<input type='submit' value='Check now'");
		writeAuthKeyFormField (this.requestThread);
		this.requestThread.write("</form>");

		this.requestThread.write(layouter.getAfterMainDiv());
		this.requestThread.flush();
		this.requestThread.close();
	}

	private String showUnixBrowserConfig(Configuration conf) {
		if (OsUtils.isWindows() || OsUtils.isOSX()) {
			return "";
		}

		String unixBrowserValue = conf.getUnixBrowser();
		String defaultBrowser = BareBonesBrowserLaunch.findUnixBrowser();

		StringWriter w = new StringWriter();

		w.append("<h3>Unix browser command</h3>\n");
		w.append("<p>\n");
		w.append("  <input name='unixBrowser' value='" + unixBrowserValue
				+ "' onFocus=\"f(this, '" + defaultBrowser
				+ "');\" onBlur=\"b(this, '" + defaultBrowser + "');\"'>\n");
		w.append("  <input type='hidden' name='unixBrowserDefault' value='"
				+ defaultBrowser + "'>\n");
		w.append("</p>\n");
		w.append("<hr>\n");

		return w.toString();
	}

	private void setUnixBrowser(String browserCommand) {
		String defaultBrowser = BareBonesBrowserLaunch.findUnixBrowser();
		if ((browserCommand == null)
				|| browserCommand.matches("\\s*")
				|| ((defaultBrowser != null) && browserCommand
						.equals(defaultBrowser))) {
			browserCommand = "";
		}

		Configuration.instance().setUnixBrowser(browserCommand);
	}
	
	private void writeAuthKeyFormField(RequestThread requestThread) throws Exception {
		requestThread.write("<input type='hidden' name='authKey' value='"+ m_authKey+"' />");	
	}
	
	private String getAuthKeyAsUrlParamAtEnd() throws Exception {
		return ("&authKey="+ URL.encode(m_authKey));	
	}


}
