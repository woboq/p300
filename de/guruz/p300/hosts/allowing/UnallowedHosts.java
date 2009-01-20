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
package de.guruz.p300.hosts.allowing;

import java.util.ArrayList;

/**
 * This class stores IPs from hosts we do not allow but got packets from
 * 
 * note that this currently may contain IPs that we allow now
 */
public class UnallowedHosts {
	private static ArrayList<String> ips = new ArrayList<String>();
	
	private static final int maxSize = 50;
	
	public static void addIP (String ip) {
		synchronized (UnallowedHosts.ips) {
			int size = UnallowedHosts.ips.size();
			
			// check if already in
			for (int i = 0; i < size; i++) {
				if (UnallowedHosts.ips.get(i).equals(ip)) {
					return;
				}
			}
			
			// remove first element if maxSize is reached
			if (size >= UnallowedHosts.maxSize) {
				UnallowedHosts.ips.remove(0);
			}
			
			// append IP
			UnallowedHosts.ips.add(ip);
		}
	}
	
	public static String[] get () {
		synchronized (UnallowedHosts.ips) {
			//return (String[]) ips.toArray();
			int size = UnallowedHosts.ips.size();
			
			String ret[] = new String[size];
			for (int i = 0; i < size; i++) {
				ret[i] = UnallowedHosts.ips.get(i);
			}
			
			return ret;
		}
	}
}
