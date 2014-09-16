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

package de.guruz.p300;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

public class P300ProxySelector extends ProxySelector {

	@Override
	public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2) {
		oldDefault.connectFailed(arg0, arg1, arg2);
	}

	@Override
	/**
	 * When the URI seems to be a p300 instance, we try direct first, else we try the system default
	 * first.
	 * 
	 * Note that port may be totally undefined, even for http://p300.eu/ as URL. I thought we
	 * would get 80
	 */
	public List<Proxy> select(URI u) {
		List<Proxy> defaultProxies = oldDefault.select(u);
		
		if (u.getPort() >= 4337 && u.getPort() <= 4344) {
			// add at beginning
			defaultProxies.add(0, Proxy.NO_PROXY);
		} else {
			// add at end
			defaultProxies.add(Proxy.NO_PROXY);
		}
		
		return defaultProxies;
	}
	
	private static ProxySelector oldDefault = null;

	public static void useIt() {
		oldDefault = ProxySelector.getDefault();
		setDefault (new P300ProxySelector ());
	}

}
