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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import de.guruz.p300.Constants;

/**
 * Represents a host that we can ask via HTTP if it is up and who it knows
 * 
 * @author guruz
 * 
 */
public class HTTPMulticastHost implements Delayed {
	protected String hostport;
	
	private long delayDisplacement = de.guruz.p300.utils.RandomGenerator.getInt(-20*1000, 40*1000);

	// used means we tried it but got an error, OK means we successfully tried
	// it.
	enum HttpMulticastStatusType {
		NEW, NOP300, USED, OK, UNALLOWED, OURSELVES
	};

	protected HttpMulticastStatusType status = HttpMulticastStatusType.NEW;

	long m_doNotAskUntil;

	public long lastQueryTime;

	protected String connectionStatus = "";

	public HTTPMulticastHost(String hip) {
		this.hostport = hip;
		m_doNotAskUntil = System.currentTimeMillis();
	}

	public void updateLastQueryTime() {
		this.lastQueryTime = System.currentTimeMillis();
	}

	public String getHostfinderPath() {
		return "/hostfinder/0.1/get";
	}

	public int getPort() {
		return Integer.parseInt((this.hostport.split(":"))[1]);
	}

	public String getIP() {
		return (this.hostport.split(":"))[0];
	}

	public String getHostPort() {
		return this.hostport;
	}

	public String lastQueryToString() {
		if (this.lastQueryTime == 0) {
			return "never";
		}

		long now = System.currentTimeMillis();

		long seconds = (now - this.lastQueryTime) / 1000;
		// return "" + seconds + " seconds ago";

		return de.guruz.p300.utils.HumanReadableTime.timeDifferenceAsString(
				seconds, 0);

		// return "?";
	}

	public String stateToString() {
		if (this.status == HttpMulticastStatusType.NEW) {
			return "Unknown";
		}

		if ((this.connectionStatus != null)
				&& (this.connectionStatus.length() > 0)) {
			return this.connectionStatus;
		}

		if (this.status == HttpMulticastStatusType.NOP300) {
			return "No p300";
		}

		return "?";
	}

	public long getDelay(TimeUnit tu) {
		long now = System.currentTimeMillis();

		switch (this.status) {
		case NEW:
			return 0; // ask immediatly
		case UNALLOWED:
			return tu.convert(delayDisplacement + (Constants.DISCOVERY_HTTP_HOSTFINDER_INTERVAL_MSEC / 2) - (now - lastQueryTime),
					TimeUnit.MILLISECONDS); // re-ask even faster
		case OK:
			return tu.convert(delayDisplacement + Constants.DISCOVERY_HTTP_HOSTFINDER_INTERVAL_MSEC - (now - lastQueryTime),
					TimeUnit.MILLISECONDS); // re-ask time if everything went ok last time
		case USED:
			return tu.convert(delayDisplacement + Constants.DISCOVERY_HTTP_HOSTFINDER_INTERVAL_MSEC*3 - (now - lastQueryTime),
					TimeUnit.MILLISECONDS); // ask after a longer time
		case NOP300:
			return tu.convert(999, TimeUnit.SECONDS); // ask never
		case OURSELVES:
			return tu.convert(999, TimeUnit.SECONDS); // ask never
		}

		return 0; // ask immediatly

	}

	public int compareTo(Delayed arg0) {
		// "Returns a negative integer, zero, or a positive integer as this
		// object is less than, equal to, or greater than the specified object."
		long myDelay = getDelay (TimeUnit.MILLISECONDS);
		long theirDelay = arg0.getDelay(TimeUnit.MILLISECONDS);
		
		if (myDelay < theirDelay)
			return -1;
		else if (myDelay > theirDelay)
			return 1;
		else
			return 0;
	}

	public void resetLastQueryTime() {
		lastQueryTime = 0;
		
	}
}
