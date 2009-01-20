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
package de.guruz.p300.search;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.logging.D;

public class IndexerThread extends Thread {
	public enum IntervalLevel {
		ALWAYS			(0, "Always"),
		TENMINUTES		(1000 * 60 * 10, "Every 10 minutes"),
		THIRTYMINUTES	(1000 * 60 * 30, "Every 30 minutes"),
		HOURLY			(1000 * 60 * 60, "Every hour"),
		TWOHOURLY		(1000 * 60 * 60 * 2, "Every 2 hours"),
		SIXHOURLY 		(1000 * 60 * 60 * 6, "Every 6 hours"),
		TWELVEHOURLY 	(1000 * 60 * 60 * 12, "Every 12 hours"),
		DAILY 			(1000 * 60 * 60 * 24, "Every day"),
		WEEKLY 			(1000 * 60 * 60 * 24 * 7, "Every week"),
		NEVER			(0, "Only run when changing shares");

		public final long intervalTime;
		private final String description;
		IntervalLevel(long it, String desc) {
			this.intervalTime = it;
			this.description = desc;
		}
		
		@Override
		public String toString() {
			if (this.description != null) {
				return this.description;
			} else {
				return "";
			}
		}
	};
	public static final IntervalLevel defaultInterval = IntervalLevel.DAILY;
	private static IntervalLevel indexInterval = IndexerThread.defaultInterval;
	public static boolean restartIndexer;
	public static boolean isIndexing;
	private static boolean firstRun = true;
	private static long firstRunSleepInterval = 80 * 1000;
	
	@Override
	public void run() {
		while (IndexerThread.indexInterval != IntervalLevel.NEVER) {
			IndexerThread.restartIndexer = false;
			int indexerSpeed = Configuration.instance().getIndexerSpeed();
			Indexer.setSpeedLevel(indexerSpeed);
			
			IndexerThread.firstRunSleep();
			
			IndexerThread.isIndexing = true;
			Indexer.indexAllShares();
			IndexerThread.isIndexing = false;
			
			try {
				if (!IndexerThread.restartIndexer) {
					Thread.sleep(IndexerThread.indexInterval.intervalTime);
				}
			} catch (InterruptedException ie) {
				D.out("IndexerThread sleep interrupted");
			}
		}
		if (IndexerThread.indexInterval == IntervalLevel.NEVER) {
			MainDialog.indexerThread = null;
		}
	}
	
	public static void setIndexInterval(IntervalLevel level) {
		IntervalLevel oldInterval = IndexerThread.indexInterval;
		IndexerThread.indexInterval = level;
		if (IndexerThread.startingUp(oldInterval)) {
			if (MainDialog.indexerThread == null) {
				MainDialog.indexerThread = new IndexerThread();
				MainDialog.indexerThread.start();
			}
		} else if (!IndexerThread.isIndexing && !IndexerThread.shuttingDown(oldInterval)) {
			IndexerThread.restartIndexer();
		}
	}
	
	public static void restartIndexer() {
		IndexerThread.restartIndexer = true;
		if (MainDialog.indexerThread != null) {
			MainDialog.indexerThread.interrupt();
		}
	}
	
	@Override
	public void start() {
		IntervalLevel level = Configuration.instance().getIndexerInterval();
		IndexerThread.indexInterval = level;
		if (IndexerThread.indexInterval == IntervalLevel.NEVER) {
			MainDialog.indexerThread = null;
		}
		this.setPriority(Thread.MIN_PRIORITY);
		super.start();
	}
	
	private static boolean startingUp(IntervalLevel oldLevel) {
		return (oldLevel == IntervalLevel.NEVER) && (IndexerThread.indexInterval != IntervalLevel.NEVER);
	}
	
	private static boolean shuttingDown(IntervalLevel oldLevel) {
		return (oldLevel != IntervalLevel.NEVER) && (IndexerThread.indexInterval == IntervalLevel.NEVER);
	}
	
	private static void firstRunSleep() {
		if (!IndexerThread.firstRun) {
			return;
		}
		IndexerThread.firstRun = false;
		try {
			Thread.sleep(IndexerThread.firstRunSleepInterval);
		} catch (InterruptedException e) {
			System.err.println("IndexerThread: First run sleep interrupted.");
		}
	}
	
}
