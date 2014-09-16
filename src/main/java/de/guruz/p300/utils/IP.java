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
package de.guruz.p300.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Helper functions for IPs
 * @author guruz
 *
 */
public class IP {
	public static boolean isOurIP(String ip) {
		try {

			Enumeration<NetworkInterface> nifs = java.net.NetworkInterface
					.getNetworkInterfaces();

			while (nifs.hasMoreElements()) {
				NetworkInterface nif = nifs.nextElement();
				Enumeration<InetAddress> ips = nif.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress current_ip = ips.nextElement();
					// System.err.println (current_ip);
					if (current_ip.getHostAddress().equals(ip)) {
						return true;
					}

				}
			}
		} catch (Exception e) {
			// do nothing
		}

		return false;
	}

	public static boolean isLocalhostIP(String ip) {
		// FIXME why the hell is this 0:0:0:0:0:0:0:1%0 when connecting to ::1
		// 4337 ?!
		if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")
				|| ip.equals("0:0:0:0:0:0:0:1%0")) {
			return true;
		}

		return false;
	}

	public static String[] getIPsForDevice(String dn) {
		try {
			Vector<String> list = new Vector<String>();
			NetworkInterface nif = NetworkInterface.getByName(dn);
			Enumeration<InetAddress> ips = nif.getInetAddresses();

			while (ips.hasMoreElements()) {
				InetAddress current_ip = ips.nextElement();

				if ((current_ip instanceof Inet4Address)
						|| (current_ip instanceof Inet4Address)) {
					//System.out.println(nif.getDisplayName() + "->"
					//		+ current_ip.getHostAddress());
					list.add(current_ip.getHostAddress());
				}
			}

			if (list.size() > 0) {
				String ret[] = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					ret[i] = list.get(i);
					//System.out.println("ret: " + ret[i]);
				}

				return ret;
			}

		} catch (Exception e) {

		}

		return new String[0];
	}
	
	public static boolean matchesIPorHostnamePort (String s)
	{
		return s.matches("^[a-zA-Z0-9\\.\\-]+:[0-9]+$");
	}
	
	public static int getPortFromHostPort (String hostport)
	{
		return Integer.parseInt((hostport.split(":"))[1]);
	}
	
	public static String getHostFromHostPort (String hostport)
	{
		return (hostport.split(":"))[0];
	}	
	
}
