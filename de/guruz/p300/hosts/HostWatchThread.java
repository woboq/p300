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

import java.util.Observer;

import de.guruz.p300.MainDialog;

/**
 * this thread watches the (LAN) hosts we have and notifies an observer if
 * the online/offline status of a host changed
 * @author guruz
 *
 */
public class HostWatchThread extends Thread {
	public void setHostMap(HostMap hm) {
		this.hostMap = hm;
	}

	protected HostMap hostMap = null;

	protected Object sleeper = new Object();
	
	protected HostWatchObserver observable = new HostWatchObserver ();
	
	
	@Override
	public void run() {
		try {
			this.setName ("HostWatchThread");
		} catch (Exception e) {
		}
		
		while (true) {
			synchronized (this.sleeper) {
				try {
					this.sleeper.wait(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (this.hostMap != null) {
				// tell all hosts 
				Host[] hosts = MainDialog.hostMap.getHosts();

				for (Host h : hosts) {
					// check the state of the hosts, if it is inconsistent with
			    	// the seemsOnline method then update it and notify our observers
			    	if (h.state == Host.HostStateType.NEW) {
			    		this.observable.setChanged ();
			    		
			    		if (h.seemsOnline ()) {
							h.state = Host.HostStateType.ONLINE;
						} else {
							h.state = Host.HostStateType.OFFLINE;
						}
			    		
			    		// we here notify with the host, it will be new
			    		this.observable.notifyObservers(h);
			    		
			    	} else if (h.state == Host.HostStateType.ONLINE) {
			    		if (!h.seemsOnline()) {
			    			this.observable.setChanged ();
			    			h.state = Host.HostStateType.OFFLINE;
			    			this.observable.notifyObservers();
			    		}
			    	} else if (h.state == Host.HostStateType.OFFLINE) {
			    		if (h.seemsOnline()) {
			    			this.observable.setChanged ();
			    			h.state = Host.HostStateType.ONLINE;
			    			this.observable.notifyObservers();
			    		}
			    	}
			    }
			    this.observable.clearChanged ();

			}
			
			synchronized (this.sleeper) {
				try {
					this.sleeper.wait(7000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void addObserver(Observer item) {
		this.observable.addObserver(item);
	}
	

}
