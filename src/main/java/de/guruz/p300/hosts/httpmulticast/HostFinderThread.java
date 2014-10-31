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
package de.guruz.p300.hosts.httpmulticast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.hosts.HostMap;
import de.guruz.p300.utils.IP;

/**
 * This class uses HTTP to check if a host is still up and also asks these hosts
 * for new hosts (yes, we have multicast but this is more reliable)
 * 
 * 
 */
public class HostFinderThread extends Thread {

	long timeLastCorreclyProcessedLine;

	DelayQueue<HTTPMulticastHost> hosts = new DelayQueue<HTTPMulticastHost>();
	List<HTTPMulticastHost> currentlyAskedHosts = new ArrayList<HTTPMulticastHost>();

	Executor hostFindExecutor = Executors.newFixedThreadPool(25);

	public void run() {
		try {
			addPossibleHostsFromConfig();

			// at the beginning, sleep 5 sec
			Thread.sleep(5 * 1000);
		} catch (Exception e) {
		}

		while (!MainDialog.requestedShutdown) {
			try {
				HTTPMulticastHost host = hosts.take();

				if (host != null) {
					synchronized (this) {
						currentlyAskedHosts.add(host);
						hostFindExecutor.execute(new HostFindJob(host, this));
					}
				}

				saveWorkingHostsToConfigIfNecessary();

			} catch (InterruptedException e) {
			} catch (java.lang.IllegalMonitorStateException e) {
			}

		}
	}

	public void addPossibleP300Host(String ip, String port) {
		addPossibleP300Host(ip + ':' + port);
	}

	public void addPossibleP300Host(String ip, int port) {
		addPossibleP300Host(ip + ':' + port);
	}

	/*
	 * If we do not know the host:port yet, add it to the hosts to query.
	 */
	public void addPossibleP300Host(String s) {
		s = toIpPort(s);

		synchronized (this) {
			for (HTTPMulticastHost h : currentlyAskedHosts)
				if (h.getHostPort().equals(s))
					return;

			for (HTTPMulticastHost h : hosts)
				if (h.getHostPort().equals(s))
					return;
		}
		
		String ip = IP.getHostFromHostPort(s);
		if (MainDialog.m_hostAllowanceManager.isIpAllowed(ip))
			hosts.add(new HTTPMulticastHost(s));
	}

	private String toIpPort(String hostAndPort) {
		// TODO
		return hostAndPort;
	}

	/*
	 * If we do not know the host:port yet, add it to the hosts to query. If we
	 * know the host already, try to enforce a quicker query.
	 */
	public void askImmediatly(String s) {
		s = toIpPort(s);
		synchronized (this) {
			for (HTTPMulticastHost h : currentlyAskedHosts)
				if (h.getHostPort().equals(s))
					return;

			//
			HTTPMulticastHost toBeQuicklyAskedHost = null;
			for (HTTPMulticastHost h : hosts) {
				if (h.getHostPort().equals(s)) {
					toBeQuicklyAskedHost = h;
					break;
				}
			}

			if (toBeQuicklyAskedHost != null) {
				// hack?
				toBeQuicklyAskedHost.resetLastQueryTime();
				hosts.remove(toBeQuicklyAskedHost);
				hosts.put(toBeQuicklyAskedHost);
				return;
			}

		}

		addPossibleP300Host(s);
	}

	public void reQueueHost(HTTPMulticastHost h) {
		synchronized (this) {
			currentlyAskedHosts.remove(h);
		}
		hosts.add(h);
	}

	private long lastSave = System.currentTimeMillis();

	private void saveWorkingHostsToConfigIfNecessary() {
		// save only every 60 secs
		if (lastSave + 60 * 1000 < System.currentTimeMillis()) {
			synchronized (this) {
				saveWorkingP300HostsToConfig(this.hosts);
				lastSave = System.currentTimeMillis();
			}
		}
	}

	private void saveWorkingP300HostsToConfig(Collection<HTTPMulticastHost> c) {
		String[] discoveredHosts = new String[c.size()];
		Iterator<HTTPMulticastHost> iterator = c.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			HTTPMulticastHost h = iterator.next();
			discoveredHosts[i] = h.getHostPort();
			// D.out("Saving " + h.getHostPort());
			i++;
		}

		Configuration.instance().setDiscoveredP300Hosts(discoveredHosts);
	}

	public void addPossibleHostsFromConfig() {
		Configuration conf = Configuration.instance();

		String bootstrapHostip = conf.getBootstrapP300Host();
		if (bootstrapHostip != null) {
			addPossibleP300Host(bootstrapHostip);
		}

		String hostips[] = conf.getDiscoveredP300Hosts();
		for (String element : hostips) {
			addPossibleP300Host(element);
		}
	}

	public void parseHTTPMulticastLine(String line, String remoteIp)
			throws Exception {
		// a reply looks like:
		// p300 0.1 hostinfo $hostid $hostdisplayname $HostIp $hostPort
		// $lastSeenThisMuchSecondsAgo\n

		// System.out.println ("Received HTTP multicast line: " + line);

		if (!line.startsWith("p300 0.1 hostinfo ")) {
			return;
		}

		String parts[] = line.split(" ", 9);

		if (parts.length < 8) {
			return;
		}

		String id = parts[3]; // host id
		String dn = parts[4]; // host displayname
		String ip = parts[5]; // host ip
		String po = parts[6]; // host port
		int poi = Integer.parseInt(po);
		String ls = parts[7]; // host last seen this-much-seconds-ago
		int lsi = Integer.parseInt(ls);

		// na na na :)
		if (ip.equals("127.0.0.1")) {
			return;
		}

		// special case: die ip ist die die wir fragen bzw von der wir DIREKT
		// gefragt werden
		boolean addAsHost = false;
		if (ip.equals("-")) {
			ip = remoteIp;
			addAsHost = true;
		}

		// host wird nur uebernommen falls IP erlaubt
		if (!MainDialog.getHostAllowanceManager().isIpAllowed(ip)) {
			return;
		}

		// wir uebernehmen uns selbst nicht :)
		if (Configuration.instance().getUniqueHash().equals(id)) {
			return;
		}

		HostMap hm = MainDialog.hostMap;
		Host h = hm.get(id);

		if ((h == null) && addAsHost) {
			// host was not found and we would add it (because it is the host we
			// are asking per http)
			h = hm.addHost(id);
			h.setDisplayName(dn);
			h.addIPAndPort(ip, poi, 0);
			hm.reSort();
			// } else if (h == null && !addAsHost) {
			// host was not found, but we would not add it, we only add it later
			// to ask per http
		} else if ((h != null) && addAsHost) {
			// host was found and we would add the IP (asking/getting asked
			// directly)
			h.addIPAndPort(ip, poi, 0);
		} else if ((h != null) && !addAsHost) {
			// host was found. just update the IP:Port if existing with the last
			// seen value
			h.possibleUpdateIPAndPort(ip, poi, lsi);
		}

		// well, we like the host too :)
		this.addPossibleP300Host(ip, po);
		this.timeLastCorreclyProcessedLine = System.currentTimeMillis();
	}
}
