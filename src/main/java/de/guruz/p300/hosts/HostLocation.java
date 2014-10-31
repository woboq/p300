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

/**
 * The location of a LAN host
 * @author guruz
 *
 */
public class HostLocation {
	private String ip;
	private int port;
	private long lastSeen;
	
	public HostLocation(String ip, int port, long lastSeen) {
		super();
		this.ip = ip;
		this.port = port;
		this.lastSeen = lastSeen;
	}
	
	public HostLocation(String ip, int port) {
		this(ip, port, System.currentTimeMillis());
	}

	public String getIp() {
		return this.ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public long getLastSeen() {
		return this.lastSeen;
	}
	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}
	public int getPort() {
		return this.port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public long getLastSeenInSecondsAgo () {
		if (this.lastSeen == 0) {
			return 0;
		}
		
		return (System.currentTimeMillis() - this.getLastSeen ()) / 1000;
	}
	
	// called when someone else says us that he/she saw the host
	public void updateLastSeenFromSomeoneElse(int secs) {
		// sanity check: if i is larger than 120 sec we dont want the host anyway
		if (secs > 120) {
			return;
		}
		
		long now = System.currentTimeMillis();
		long someone_else_seen = now - (1000*secs);
		
		
		// we saw the host a short time ago
		if (this.getLastSeen () > someone_else_seen) {
			return;
		}
		
		this.setLastSeen (someone_else_seen);

			
	}

	public boolean seenInLastXSeconds(int seemsOnlineThresholdsecs) {
		long now = System.currentTimeMillis();
		return ((now - this.getLastSeen ()) / 1000 < seemsOnlineThresholdsecs);
	}
	
	public String toHostPort () {
		return this.getIp () + ':' + this.getPort ();
	}
	
	public boolean equals (String h, int p) {
		return (h.equals(this.getIp()) && p == this.getPort());
	}

	public String toHttpUrl() {
		return "http://" + this.toHostPort();
	}
	
}
