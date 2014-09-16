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

import de.guruz.p300.Configuration;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.logging.D;
import de.guruz.p300.sessions.Session;
import de.guruz.p300.sessions.SessionList;
import de.guruz.p300.utils.HTTP;

/**
 * For logging in via the HTML form.
 * @author guruz
 *
 */
public class LoginRequest extends Request {

	
	// Create session & send cookie to requestThread
	// requestThread has to be set prior to the call!
	public void sendCookie() throws Exception {
//		System.err.println(">sendCookie");
		if (this.requestThread != null) {
		
			this.requestThread.httpStatus(302, "OK");

			if (!this.requestThread.isAuthenticated()) {
				Session s = SessionList.create();
				this.requestThread.httpSessionCookie (s.getID (), "/");
			}
	
			this.requestThread.httpHeader("Location", "/");
			this.requestThread.httpContents ();
			this.requestThread.close();
			
			//requestThread.close(302, "OK", "/");
		}
//		System.err.println("<sendCookie");
	}

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if ((rt != HTTPVerb.GET) && (rt != HTTPVerb.HEAD) && (rt != HTTPVerb.POST)) {
			return false;
		}
		
		if (!reqpath.startsWith("/login")) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void handle() throws Exception {
		byte cc[] = this.requestThread.getClientContent();
		
		//for (int i = 0; i < cc_len; i++) {
		//	System.out.print ((char) cc[i]);
		//	
		//}
		
		String user = HTTP.extractParameter(cc, "u");
		String pass = HTTP.extractParameter(cc, "p");
		
		//System.out.println ("u = " + user);
		//System.out.println ("p = " + pass);
		
		if ("admin".equals(user) && Configuration.instance().getAdminPassword().equals (pass)) {
			// credentials correct
			
			this.sendCookie();
			
			D.out("Login requested from " + this.requestThread.getRemoteIP() + " -> granted (correct password and user)");
			
		} else {
			// credentials not correct. print an error page
			this.requestThread.close(401, "Wrong credentials");
			D.out("Login requested from " + this.requestThread.getRemoteIP() + " -> NOT granted (incorrect password or user)");
			
		}
	}

}  
