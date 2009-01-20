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

import java.util.Timer;
import java.util.TimerTask;

import de.guruz.p300.MainDialog;
import de.guruz.p300.logging.D;

/**
 * This thread gets called on shutdown of the JVM
 * 
 * @author guruz
 * 
 */
public class ShutdownThread extends Thread {
	public static boolean shuttingDown = false;
	
	@Override
	public void run() {
		try {
			this.setName("ShutdownThread");
		} catch (Exception e) {
		}
		
		shuttingDown = true;

		MainDialog.requestedShutdown = true;

		D.out("Shutdown requested");
		System.out.flush();
		System.err.flush();

		// halt in 10 seconds
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Forceful exit");
				Runtime.getRuntime().halt(0);
			}
		}, 10 * 1000, 60 * 1000);

		if (MainDialog.listenThread != null) {
			MainDialog.listenThread.stopListening();
		}

		if (MainDialog.lockFile != null) {
			MainDialog.lockFile.unlock();
		}

		if (MainDialog.hostMap != null) {
			MainDialog.hostMap.shutdown();
		}
		
		// unsafe blabla
		
		if (MainDialog.hostFinderThread != null)
		{
			MainDialog.hostFinderThread.stop();
		}
		
		if (MainDialog.multicastListenThread != null)
		{
			MainDialog.multicastListenThread.stop();
		}
		
		if (MainDialog.indexerThread != null)
		{
			MainDialog.indexerThread.stop();
		}
		
		if (MainDialog.updaterThread != null)
		{
			MainDialog.updaterThread.stop();
		}
		
		if (MainDialog.hostWatchThread != null)
		{
			MainDialog.hostWatchThread.stop();
		}
		
		if (MainDialog.listenThread != null)
		{
			MainDialog.listenThread.stop();
		}
		
		if (MainDialog.newVersionNotificationThread != null)
		{
			MainDialog.newVersionNotificationThread.stop();
		}
		
		if (MainDialog.bandwidthThread != null)
		{
			MainDialog.bandwidthThread.stop();
		}

	}
}
