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
package de.guruz.p300.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class BandwidthQuotaQueue {
	BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();

	// ReentrantLock lock = new ReentrantLock (true);

	Semaphore waiterSemaphore = new Semaphore(1, true);

	public void refreshQuotaTo(long bytes) {
		queue.clear();
		// FIXME cast?
		try {
			queue.put(new Integer((int) bytes));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getQuotaToUse(int want) {
		waiterSemaphore.acquireUninterruptibly();

		try {
			// take element from the queue, add the remaining back to the queue
			Integer q = queue.take();

			if (q > want) {
				queue.put(new Integer(q - want));
				// had more than wanted
				return want;
			} else if (q <= want) {
				// had less than wanted
				return q;
			};
			// will not happen
			return 0;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} finally {
			waiterSemaphore.release();
		}
	}

	public int remaining() {
		waiterSemaphore.acquireUninterruptibly();
		try {
			int ret = 0;
			
			for (Integer i : queue) {
				ret += i;
			}
			
			return ret;
		} finally {
			waiterSemaphore.release();
		}

	}
}
