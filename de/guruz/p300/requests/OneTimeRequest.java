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

import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.logging.D;

/**
 * Request for logging in using a one-time-key
 * @author guruz
 *
 */
public class OneTimeRequest extends Request {

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (rt != HTTPVerb.GET) {
			return false;
		}

		return (reqpath.startsWith("/onetime/"));
	}

	@Override
	public void handle() {
		try {
			if (this.requestThread.isAuthenticated()) {
				D.out(
						"Login requested from " + this.requestThread.getRemoteIP()
								+ " -> granted (already logged in)");
				this.requestThread.close(302, "OK", "/");
				return;

			}

			String possibleKey = this.requestThread.path.substring("/onetime/"
					.length());

			if (OneTimeManager.isRegistered(possibleKey)) {
				// System.err.println("isRegistered: true");
				LoginRequest lr = new LoginRequest();
				lr.setRequestThread(this.requestThread);
				// lr.handle();
				lr.sendCookie();
				OneTimeManager.unregister(possibleKey);

				D.out(
						"Login requested from " + this.requestThread.getRemoteIP()
								+ " -> granted (correct one-time key)");

			} else {
				try {
					this.requestThread.close(403, "No key");
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			System.err.println("Couldn't process OneTimeRequest: "
					+ (e.getMessage()));
			e.printStackTrace();

			try {

				this.requestThread.close(500, "Server error");
			} catch (Exception e1) {
			}
		}
	}
}