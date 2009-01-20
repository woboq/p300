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
package de.guruz.p300.threads;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.logging.D;

/**
 * This thread checks for updates from the p300 webpage
 * 
 * @author guruz
 *
 */
public class NewVersionNotificationThread extends Thread {
	String notificationURL = "http://p300.eu/releases/current_revision";

	@Override
	public void run() {
		try {
			this.setPriority(Thread.MIN_PRIORITY);
			this.setName ("NewVersionNotificationThread");
		} catch (Exception e) {
		}
		
		while (true) {
			try {

				// initially sleep 3 minutes
				Thread.sleep(1000*60*3);

				long lastRetrieval = Configuration.instance().getLastRevisionRetrieval ();
					
				// 1 week minus 1 hour
				if (System.currentTimeMillis() - lastRetrieval > (1000*60*60*24*7 - 1000*60*60)) {
					doCheck ();
				}
				
				int minutes = de.guruz.p300.utils.RandomGenerator.getInt(0, 30);
				Thread.sleep(minutes * 60 * 1000);

			} catch (Exception e) {
				// e.printStackTrace();
				D.out (e.getLocalizedMessage());
			}

		}

	}
	
	public synchronized void doCheck () {
		try {
			URL url = new URL(this.notificationURL);
			int rev = 0;
			URLConnection urlcon = url.openConnection();
			urlcon.setConnectTimeout(15 * 1000); // 15 secs
			urlcon.setDoOutput(false);
			urlcon.setDoInput(true);
			urlcon.setAllowUserInteraction(false);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlcon.getInputStream()));
			String line = reader.readLine();
			while (line != null) {

				try {
					rev = Integer.parseInt(line);
					break;
				} catch (Exception e) {

				}

				line = reader.readLine();
			}
			try {
				reader.close();
			} catch (Exception e) {

			}
			Configuration.instance().updateHighestSeenSVNRevision(rev);
			Configuration.instance().updateLastRevisionRetrieval();
			// run the UpdaterThread
			MainDialog.updaterThread.go(rev);
		} catch (Exception e) {
			// TODO: handle exception
			D.out(e.toString());
		}		
	}
	
}
