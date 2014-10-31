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
package de.guruz.p300.genericdnssd;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;

import de.guruz.p300.hosts.DNSSDCallbackHook;
import de.guruz.p300.hosts.DNSSDHelperHook;

public abstract class GenericDNSSDHelper implements DNSSDHelperHook{
	protected int port = 0;
	
	public void registerOurselves(int p) {
		port = p;
	}
	
	public	DNSSDCallbackHook callback;
	public void setCallback(DNSSDCallbackHook cb) {
		callback = cb;	
	}

	
	public void shutdown() {
		
	}
	
	
	Vector<String> enabledDevices = new Vector<String>();
	public void setDeviceEnabled(String devname, boolean b) {
		// not yet initialized. we will be called again later anyway
		if (port == 0)
			return;
		
		if (b == true) {
			synchronized (enabledDevices) {
				Iterator<String> it = enabledDevices.iterator();
				while (it.hasNext()) {
					if (it.next().equals(devname))
						return;
					
				}
				
				// item not in list
				enabledDevices.add(devname);
				deviceNowEnabled (devname);
			}
		} else if (b == false) {
			synchronized (enabledDevices) {
				Iterator<String> it = enabledDevices.iterator();
				while (it.hasNext()) {
					if (it.next().equals(devname)) {
						it.remove();
					
						deviceNowDisabled (devname);
					}
					
				}
				
			}
		}
	}
	

	

	public abstract void deviceNowDisabled(String devname);
	public abstract void deviceNowEnabled(String devname);
	
	public String getHostName () {
		try {
			String hn = java.net.InetAddress.getLocalHost().getHostName();
			
			int pointIdx = hn.indexOf('.');
			
			if (pointIdx != -1)
				hn = hn.substring(0, pointIdx);
			
			return hn.replace(" ", "_");
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
			return "unknown";
		}
	}
	
	
}
