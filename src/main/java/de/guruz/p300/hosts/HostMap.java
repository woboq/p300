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
package de.guruz.p300.hosts;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.logging.D;
import de.guruz.p300.search.Indexer;

/**
 * Here we store all discovered hosts
 * @author guruz
 *
 */
public class HostMap implements Observer, DNSSDCallbackHook {

	ArrayList<Host> ht = new ArrayList<Host>();

	public HostMap() {
		this.loadDNSSDHelper();

	}

	public Host get(String id) {
		Host current = null;
		for (int i = 0; i < this.ht.size(); i++) {
			current = this.ht.get(i);

			if (id.equals(current.getUUID())) {
				return current;
			}
		}

		return null;
	}

	public Host addHost(String id) {
		Host host = new Host(id);
		synchronized (this.ht) {
			this.ht.add(host);
		}
		return host;
	}

	public void reSort() {
		synchronized (this.ht) {
			Collections.sort(this.ht);
		}
	}

	public Host[] getHosts() {
		synchronized (this.ht) {
			int count = this.ht.size();
			Host ret[] = new Host[count];

			for (int i = 0; i < count; i++) {
				ret[i] = this.ht.get(i);
			}

			return ret;
		}
	}

	public boolean isEmpty() {
		return (this.ht.isEmpty());
	}

	public String lastKnownOnlineHosts = "";

	public void printKnownOnlineHosts() {
		String hostsString = "";

		Host[] hosts = this.getHosts();

		for (Host h : hosts) {
			if (h.seemsOnline()) {
				if (hostsString.length() > 1) {
					hostsString = hostsString + ", ";
				}
				hostsString = hostsString + h.getDisplayName();

			}
		}

		if (this.lastKnownOnlineHosts.equals(hostsString)) {
			return;
		}

		D.out("hosts online = {" + hostsString + '}');
		this.lastKnownOnlineHosts = hostsString;
	}

	public int count() {
		return this.ht.size();
	}

	// called from a HostFinderRequest thread
	public String writeOutKnownHosts() throws Exception {
		// a reply looks like:
		// p300 0.1 hostinfo $hostid $hostdisplayname $HostIp $hostPort
		// $lastSeenThisMuchSecondsAgo\n

		// ByteBuffer bb = ByteBuffer.allocate(100);
		StringBuffer sb = new StringBuffer();

		// write ourselves
		Configuration conf = Configuration.instance();
		sb.append("p300 0.1 hostinfo ");
		sb.append(conf.getUniqueHash());
		sb.append(' ');
		sb.append(conf.getLocalDisplayName());
		sb.append(' ');
		sb.append('-'); // special case for the IP, this is us!
		sb.append(' ');
		sb.append(String.valueOf(MainDialog.getCurrentHTTPPort()));
		sb.append(' ');
		sb.append('0');
		// split works better with a blank before a newline
		sb.append(" \n");

		// write all hosts
		Host hosts[] = this.getHosts();
		for (Host current : hosts) {
			if (!current.seemsOnline()) {
				continue;
			}

			HostLocation locations[] = current.getLocations();

			for (HostLocation element0 : locations) {
				sb.append("p300 0.1 hostinfo ");
				sb.append(current.getUUID());
				sb.append(' ');
				sb.append(current.getDisplayName());
				sb.append(' ');

				sb.append(element0.getIp());
				sb.append(' ');
				sb.append(String.valueOf(element0.getPort()));
				sb.append(' ');
				sb.append(String.valueOf(element0.getLastSeenInSecondsAgo()));
				// split works better with a blank before a newline
				sb.append(" \n");

			}
		}

		sb.append(getSearchIndexInfoString());

		return sb.toString();
	}
	
	/**
	 * Generate a String that contains the searchindex information
	 * This can be sent out to requesting hosts
	 * @return Can be an empty String if there is no indexer data available
	 */
	private String getSearchIndexInfoString() {
		int lastIndexFileCount = Indexer.lastIndexFileCount;
		long lastIndexAllSize = Indexer.lastIndexAllSize;
		if (lastIndexFileCount < 0 || lastIndexAllSize < 0) {
			return "";
		}
		
		StringWriter writer = new StringWriter();
		
		writer.append("p300 0.1 searchindex ");
		Configuration configuration = Configuration.instance();
		writer.append(configuration.getUniqueHash());
		writer.append(" ");
		writer.append(Integer.toString(lastIndexFileCount));
		writer.append(" ");
		writer.append(Long.toString(lastIndexAllSize));
		writer.append(" \n");
		
		return writer.toString();
	}

	/**
	 * not used right now
	 * 
	 * @param h
	 */
	public void DNSSDDiscovered(Host h) {
	}

	public void loadDNSSDHelper() {
		// if (!MainDialog.isOSX())
		// return;

		try {
			Class<?> c = null;
			Object o = null;

			// try to use the installed bonjour

			try {
				String className = "de.guruz.p300.applednssd.DNSSDHelper";
				c = Class.forName(className);
				o = c.newInstance();
				this.dnssdHelperHook = (DNSSDHelperHook) o;
			} catch (Throwable t) {
				// t.printStackTrace();
			}

			// try to use jmdns if bonjour is not here
			if (o == null) {
				try {
					// in cwd
					File jmdnsJarFile = new File(System.getProperty("user.dir"));
					jmdnsJarFile = new File(jmdnsJarFile.getParentFile(),
							"jmdns.jar");
					URL jmdnsJarURL = jmdnsJarFile.toURI().toURL();

					// in $HOME/.p300
					File jmdnsJarURLDotP300File = new File(Configuration
							.configDirFileName("jmdns.jar"));
					URL jmdnsJarURLDotP300URL = jmdnsJarURLDotP300File.toURI().toURL();

					URLClassLoader ucl = new URLClassLoader(new URL[] {
							jmdnsJarURL, jmdnsJarURLDotP300URL }, ClassLoader
							.getSystemClassLoader());

					String className = "de.guruz.p300.jmdnsdnssd.DNSSDHelper";
					c = Class.forName(className, true, ucl);
					o = c.newInstance();
					this.dnssdHelperHook = (DNSSDHelperHook) o;
				} catch (Throwable t) {
					// t.printStackTrace();
				}
			}

			// do we have anything at least? =)

			if (this.dnssdHelperHook != null) {
				this.dnssdHelperHook.registerOurselves(MainDialog
						.getCurrentHTTPPort());
				this.dnssdHelperHook.setCallback(this);
			} else {
				D.out("Zeroconf: No appropriate class found");
				// D.out(System.getProperty("java.class.path"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	DNSSDHelperHook dnssdHelperHook;

	public void updateDNSSDStatus(String s) {
		D.out("Zeroconf: " + s);
	}

	public void update(Observable arg0, Object arg1) {
		try {
			this.printKnownOnlineHosts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		if (this.dnssdHelperHook != null) {
			this.dnssdHelperHook.shutdown();
		}

	}

	public void setDeviceEnabled(String name, boolean b) {
		if (this.dnssdHelperHook != null) {
			this.dnssdHelperHook.setDeviceEnabled(name, b);
		}

	}

	public void lookupIPResult(String ip, String hostname) {
		de.guruz.p300.dns.DNSCache.put(ip, hostname);

	}

}
