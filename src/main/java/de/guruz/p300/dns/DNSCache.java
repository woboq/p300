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
package de.guruz.p300.dns;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Our DNS cache. We can get these DNS results not only from standard NS but maybe also from Bonjour/MDNS
 * @author guruz
 *
 */
public class DNSCache {
	 static ExecutorService executor;
	 
	static {
		System.setProperty("networkaddress.cache.ttl", "200");
		
		executor = Executors.newSingleThreadExecutor();
	}
	
	protected static HashMap<String,CacheEntry> hashmap = new HashMap<String,CacheEntry>();
	
	public static String toHostname (String ip) {
		synchronized (DNSCache.class) {
			//D.out ("toHostname start");
			if (DNSCache.hashmap.containsKey(ip)) {
				CacheEntry ce = DNSCache.hashmap.get(ip);
				if (!ce.isStale()) {
					//D.out ("toHostname end (with return)");
					return ce.getHostName();
				}
			}
			
			LookupHelper lookupHelper = new LookupHelper (ip);
			executor.execute (lookupHelper);
			
			//D.out ("toHostname end");
			return null;
		}
	}

	/**
	 * Start the DNS lookup so we can get the name later
	 * @param ip
	 */
	public static void lookupForLater(String ip) {
		toHostname (ip);
	}
	
	public static void put (String ip, String hostname) {
		synchronized (DNSCache.class) {
			if (DNSCache.hashmap.containsKey(ip)) {
				CacheEntry ce = DNSCache.hashmap.get(ip);
				ce.setHostName (hostname);
			} else {
				CacheEntry ce = new CacheEntry (ip, hostname);
				DNSCache.hashmap.put(ip, ce);
			}
		}
	}
	
}
