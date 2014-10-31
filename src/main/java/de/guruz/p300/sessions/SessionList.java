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
package de.guruz.p300.sessions;

import java.util.HashMap;
import java.util.prefs.Preferences;

import de.guruz.p300.Configuration;

public class SessionList {
	private static HashMap<String, Session> map = new HashMap<String, Session>();

	/**
	 * load all sessions on creation
	 */
	static {
		SessionList.loadSessions();
	}

	/**
	 * Add a session with an id
	 * @param id
	 * @param authed
	 * @param createdOn
	 * @return
	 */
	protected static Session add(String id, boolean authed, long createdOn) {
		Session s = new Session(id, true, createdOn);
		SessionList.map.put(id, s);

		return s;
	}
	
	/**
	 * Add a session and let it pick the id for itself
	 * @param authed
	 * @param createdOn
	 * @return
	 */
	protected static Session add(boolean authed, long createdOn) {
		Session s = new Session(true, createdOn);
		SessionList.map.put(s.getID(), s);

		return s;
	}

	/**
	 * Create a new session with a random ID
	 * @return
	 */
	public static Session create() {
		long now = System.currentTimeMillis();
		Session s = SessionList.add(true, now);
		SessionList.saveSessions();

		return s;
	}

	/** Get a session with a specific id
	 * 
	 * @param i
	 * @return
	 */
	public static Session get(String i) {
		return SessionList.map.get(i);
	}

	/**
	 * Remove a session from the map
	 * @param s
	 */
	public static void remove(Session s) {
		SessionList.map.remove(s);
		
		SessionList.saveSessions ();
	}

	/**
	 * We base the cookie name on the host header
	 * 
	 * @param hostHeader
	 * @return
	 */
	public static String getCookieName(String hostHeader) {
		if (hostHeader == null) {
			return "p300.session";
		}

		String n = de.guruz.p300.utils.Base64.encode(hostHeader);
		n = n.replace('/', '_');
		n = n.replace('+', '-');

		return "p300.session." + n;
	}
	

	/**
	 * Load the sessions from the file system into the map
	 *
	 */
	public static void loadSessions() {
		// load and add each session
		try {
			Preferences p = Configuration.instance().getSessionStore();
			long now = System.currentTimeMillis();


			String[] sessionIDs = p.childrenNames();
			
			for (String currentID : sessionIDs) {
				long createdOn = p.node(currentID).getLong("createdOn", now);
				boolean authed = p.node(currentID).getBoolean ("authed", false);
				// session creation is not longer than 3 days ago and we are authed
				if (((createdOn > now) || (now - createdOn < 1000*60*60*24*3)) && authed) {
					SessionList.add (currentID, true, createdOn);
				}
				
			}

		} catch (Exception e) {

		}
	}

	/**
	 * Save our session map to the file system
	 *
	 */
	public static void saveSessions() {
		Preferences p = Configuration.instance().getSessionStore();

		String[] sessionIDs = null;
		//sessionIDs = (String[]) map.keySet().toArray();
		sessionIDs = SessionList.map.keySet().toArray(new String[SessionList.map.size()]);
		
		for (String currentID : sessionIDs) {
			Session currentSession = SessionList.get(currentID);
			
			p.node(currentID).putLong("createdOn", currentSession.getCreatedOn ());
			p.node(currentID).putBoolean("authed", currentSession.isAuthed ());

		}
	}

}
