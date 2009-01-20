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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.guruz.p300.Constants;

/**
 * This class currently represents a LAN/VPN host we discovered
 * 
 * @author guruz
 * 
 */
public class Host extends Object implements Comparable<Host> {
	public enum HostStateType {
		NEW, OFFLINE, ONLINE
	}
	// String ip = "";
	String displayname = "";
	String uuid = "";

	// int port;
	int revision;;

	public HostStateType state = HostStateType.NEW;

	private static final int seemsOnlineThresholdsecs = (int) (Constants.DISCOVERY_HOST_SEEMS_ONLINE_TIMEOUT_MSEC / 1000);

	ArrayList<HostLocation> locations = new ArrayList<HostLocation>();

	public Host(String id) {
		this.uuid = id;
	}

	/**
	 * If not already in, add the IP and port to our known locations
	 */
	public void addIPAndPort(String h, int p, int lastSeenSecond) {

		//D.out(getDisplayName() + " seen on " + h);
		
		synchronized (this.locations) {
			HostLocation hl = this.getLocation(h, p);

			if (hl == null) {
				long now = System.currentTimeMillis();
				long absoluteSeenValue = now - (lastSeenSecond * 1000);

				hl = new HostLocation(h, p, absoluteSeenValue);

				this.locations.add(0, hl);

			} else {
				// ip:port is already in, just update the last seen
				this.possibleUpdateIPAndPort(h, p, lastSeenSecond);
			}
		}
	}

	public int compareTo(Host arg0) {
		return this.getDisplayName().toLowerCase().compareTo(
				arg0.getDisplayName().toLowerCase());
	}

	public HostLocation getBestHostLocation() {
		HostLocation ret = null;

		synchronized (this.locations) {
			Iterator<HostLocation> it = this.locations.iterator();

			while (it.hasNext()) {
				HostLocation hl = it.next();

				if (ret == null) {
					ret = hl;
				} else {
					if (hl.getLastSeenInSecondsAgo() < ret
							.getLastSeenInSecondsAgo()) {
						ret = hl;
					}
				}
			}
		}

		return ret;
	}

	private HostLocation getBestMatchingThisIP(String remoteIP) {
		HostLocation[] locations = this.getLocations();

		if (locations.length == 0) {
			return null;
		}

		HostLocation hl = locations[0];
		int maxCommon = 0;

		for (HostLocation currentHostLocation : locations) {
			if (!currentHostLocation
					.seenInLastXSeconds(Host.seemsOnlineThresholdsecs)) {
				continue;
			}

			int currentCommon = de.guruz.p300.utils.Strings.commonStart(
					currentHostLocation.getIp(), remoteIP);
			if (currentCommon > maxCommon) {
				maxCommon = currentCommon;
				hl = currentHostLocation;
			}
		}

		return hl;
	}

	public String getDisplayName() {
		if (this.displayname.length() > 0) {
			return this.displayname;
		}

		return this.toHostPort();
	}

	protected HostLocation getLocation(String i, int p) {
		synchronized (this.locations) {
			Iterator<HostLocation> it = this.locations.iterator();

			while (it.hasNext()) {
				HostLocation hl = it.next();

				if (i.equals(hl.getIp()) && (p == hl.getPort())) {
					return hl;
				}
			}
		}

		return null;
	}

	public HostLocation[] getLocations() {
		synchronized (this.locations) {
			return this.locations.toArray(new HostLocation[this.locations.size()]);
		}
	}

	public List<HostLocation> getLocationsAsList() {
		return Collections.unmodifiableList(this.locations);
	}

	public int getSVNRevision() {
		return this.revision;
	}

	public String getUUID() {
		return this.uuid;
	}

	public boolean hasIp(String remoteIP) {
		HostLocation[] locs = getLocations();
		
		for (HostLocation loc : locs)
		{
			if (loc.getIp().equals(remoteIP))
				return true;
		}
		
		return false;
	}

	/**
	 * if we know this IP and port then update the last seen
	 * 
	 * @param i
	 * @param po
	 * @param lsi
	 */
	public void possibleUpdateIPAndPort(String i, int po, int lsi) {
		// if (getDisplayName ().equals("merau")) {
		// System.err.println ("IP time update " + i + ":" + po + " " + lsi);
		// System.err.flush();
		// }

		HostLocation hl = this.getLocation(i, po);

		if (hl != null) {
			hl.updateLastSeenFromSomeoneElse(lsi);
		}
	}

	/**
	 * check if any location we know seems online
	 * 
	 * @return
	 */
	public boolean seemsOnline() {
		// return true;

		synchronized (this.locations) {
			Iterator<HostLocation> it = this.locations.iterator();

			while (it.hasNext()) {
				HostLocation hl = it.next();

				if (hl.seenInLastXSeconds(Host.seemsOnlineThresholdsecs)) {
					return true;
				}
			}
		}

		return false;

	}

	/**
	 * Note: after setting this you need to call MainDialog.hostMap.reSort ();
	 * 
	 * @param dn
	 */
	public void setDisplayName(String dn) {
		this.displayname = dn;
	}

	public void setSVNRevision(String r) {
		this.revision = Integer.parseInt(r);
	}

	public void setUUID(String id) {
		this.uuid = id;
	}

	public String toHostnameBestMatchingThisIP(String remoteIP) {
		String ip = this.getBestMatchingThisIP(remoteIP).getIp();
		return de.guruz.p300.dns.DNSCache.toHostname(ip);
	}

	/**
	 * returns the IP:Port pair that was last seen
	 * 
	 * @return
	 */
	public String toHostPort() {
		HostLocation ret = getBestHostLocation();

		if (ret == null) {
			return "0.0.0.0:0";
		}

		return ret.toHostPort();
	}

	@Override
	public String toString() {
		return this.getDisplayName();
	}

	public String toURL() {
		return "http://" + this.toHostPort();
	}

	/**
	 * Return the URL that at best matches the calling the IP given from the
	 * caller, comparing the characters from the beginning
	 * 
	 * @param remoteIP
	 * @return
	 */
	public String toURLBestMatchingThisIP(String remoteIP) {
		HostLocation hl = this.getBestMatchingThisIP(remoteIP);

		if (hl == null) {
			return this.toURL();
		}

		return "http://" + hl.getIp() + ':' + hl.getPort();

	}

}
