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
package de.guruz.p300.requests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the keys for one-time-login
 * 
 * @author guruz
 *
 */
public class OneTimeManager {
	private static final long keyDuration = 30 * 1000; // 30 seconds

	private static Map<String, Long> oneTimeURLs = new HashMap<String, Long>();

	public static void register(String url) {
		synchronized (OneTimeManager.oneTimeURLs) {
			OneTimeManager.oneTimeURLs.put(url, System.currentTimeMillis());
			// }
		}
	}

	public static void unregister(String url) {

		synchronized (OneTimeManager.oneTimeURLs) {
			OneTimeManager.oneTimeURLs.remove(url);
		}
	}

	public static boolean isRegistered(String url) {
		OneTimeManager.cleanUp();
		boolean ck = false;
		synchronized (OneTimeManager.oneTimeURLs) {
			ck = OneTimeManager.oneTimeURLs.containsKey(url);
		}

		return ck;
	}

	private static void cleanUp() {
		synchronized (OneTimeManager.oneTimeURLs) {
			Iterator<String> it = OneTimeManager.oneTimeURLs.keySet().iterator();

			while (it.hasNext()) {
				String url = it.next();
				Long timeObject = OneTimeManager.oneTimeURLs.get(url);
				if (timeObject != null) {
					long time = timeObject;
					if (time + OneTimeManager.keyDuration < System.currentTimeMillis()) {
						it.remove();
					}
				} else {
					it.remove();
				}
			}
		}
	}

}