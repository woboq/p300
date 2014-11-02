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
package de.guruz.p300.jmdnsdnssd;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Iterator;
import java.util.Vector;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;


import de.guruz.p300.genericdnssd.GenericDNSSDHelper;
import de.guruz.p300.hosts.DNSSDHelperHook;

public class DNSSDHelper extends GenericDNSSDHelper implements DNSSDHelperHook {

	static Class test = null;
	static {
		// use at start to fail fast
		test = JmDNS.class;
	}
	
	
	/**
	 * close all jmdns instances
	 */
	public void shutdown() {
		// FIXME disabler nehmen?
		synchronized (registrationTuples) {
			Iterator<RegistrationTuple> it = registrationTuples.iterator();

			while (it.hasNext()) {
				RegistrationTuple rt = it.next();
				rt.getJmDNS().close();
			}
		}
	}

	/**
	 * start a DeviceDisabler
	 */
	public void deviceNowDisabled(String devname) {
		synchronized (registrationTuples) {
			Iterator<RegistrationTuple> it = registrationTuples.iterator();

			while (it.hasNext()) {
				RegistrationTuple rt = it.next();

				if (devname.equals(rt.getDisplayName()))
					new DeviceDisabler(rt, this).start();
			}

		}

	}

	class DeviceDisabler extends Thread {
		RegistrationTuple registrationTuple = null;
		GenericDNSSDHelper helper = null;

		public DeviceDisabler(RegistrationTuple rt, GenericDNSSDHelper h) {
			registrationTuple = rt;
			helper = h;
		}

		public void run() {
			try { 
			if (helper.callback != null)
				helper.callback.updateDNSSDStatus("[jmdns] unregistering for " + registrationTuple.getDisplayName());
			registrationTuple.getJmDNS().unregisterService(registrationTuple.getServiceInfo());
			registrationTuple.getJmDNS().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// //////////////

	public void deviceNowEnabled(String devname) {
		new DeviceEnabler(devname, this).start();
	}

	class DeviceEnabler extends Thread {
		String deviceName = null;

		GenericDNSSDHelper helper = null;

		public DeviceEnabler(String dn, GenericDNSSDHelper h) {
			deviceName = dn;
			helper = h;
		}

		public void run() {
			String hostname = "";
			JmDNS jmdns = null;

			try {
				NetworkInterface nif = NetworkInterface.getByName(deviceName);

				jmdns = new JmDNS(nif.getInetAddresses().nextElement());

				hostname = getHostName ();
				//hostname = jmdns.getHostName();
				//System.out.println ("hostname = " + jmdns.getHostName());
				//System.out.println ("localhost = " + jmdns.getLocalHost());
				
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			ServiceInfo si = new ServiceInfo("_webdav._tcp.local.", hostname, port, 0, 0, "path=/");
			try {
				jmdns.registerServiceType("_webdav._tcp");
				jmdns.registerService(si);

				RegistrationTuple rt = new RegistrationTuple(jmdns, si,
						deviceName);
				registrationTuples.add(rt);

				//jmdns.printServices();
				
				if (helper.callback != null)
					helper.callback.updateDNSSDStatus("[jmdns] Registered on "
							+ deviceName);
			} catch (IOException e) {
				e.printStackTrace();
				jmdns.close();
				if (helper.callback != null)
					helper.callback.updateDNSSDStatus("[jmdns] " + e.getMessage());
			}

		}
	}

	Vector<RegistrationTuple> registrationTuples = new Vector<RegistrationTuple>();

	class RegistrationTuple {
		JmDNS jmdns;

		ServiceInfo serviceInfo;

		String displayName;

		public RegistrationTuple(JmDNS j, ServiceInfo si, String dn) {
			jmdns = j;
			serviceInfo = si;
			displayName = dn;
		}

		public JmDNS getJmDNS() {
			return jmdns;
		}

		public ServiceInfo getServiceInfo() {
			return serviceInfo;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	/**
	 * Not possible with JMDNS
	 */
	public void lookupIP(String ip) {
		//JmDNS jmdns = new JmDNS ();
		//jmdns.getServiceInfo(arg0, arg1)
		
	}

}
