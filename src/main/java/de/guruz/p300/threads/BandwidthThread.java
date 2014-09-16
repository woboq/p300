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

import java.util.Iterator;
import java.util.Vector;

import de.guruz.p300.Configuration;
import de.guruz.p300.requests.FileRequest;

/**
 * Manages the bandwidth quota (the number of bytes allowed to be sent) for a time fraction
 * @author guruz
 *
 */
public class BandwidthThread extends Thread {
	long quota;
	boolean noLimit = true;
	
	int threadCount;
	
	Object sleeper = new Object ();
	
	//Semaphore waiterSemaphore = new Semaphore (1, true);
	
	/**
	 * How often per second we donate quota
	 */
	static final int hz = 3;
	
	@Override
	public void run() {
		try {
			this.setPriority(Thread.MAX_PRIORITY);
			this.setName ("BandwidthThread");
		} catch (Exception e) {
		}
		
		// cycle through
		while (true) {
//			System.err.println(">bwt.run");
			try {
	
								
				synchronized (this.sleeper) {
					//System.out.println ("threadCount = " + threadCount);
					
					//System.out.println ("Sleep begin with threadCount = " + threadCount);
					if (this.threadCount == 0) {
						// keine threads registriert, halbe minute warten
						this.sleeper.wait(30*1000);
					} else {
						// nur 1/hz minute schlafen
						this.sleeper.wait (1000 / BandwidthThread.hz);
					}
						
					//System.out.println ("Sleep end with threadCount = " + threadCount);
				}
				
				// continue if we have nothing to limit anyway
				if (this.threadCount == 0) {
					continue;
				}

				
				synchronized (this.registeredThreads) {	
					
					/** 
					 * 0. step: check if we need this anyway
					 */

					

					
					
					// byte count of quota that was not used in the last cycle
					long excessiveQuota = 0;
					
					// number of threads that did (not) use quota in the last cycle
					int threadsThatDidNotUseAllQuota = 0;
					int threadsThatDidUseAllQuota = 0;
					
					// quota that can be spent per cycle in bytes
					long quotaPerCycle = (Configuration.instance().getOutputBWLimitInKB() * 1024) / BandwidthThread.hz;
					
					// do we have a limit?
					boolean noLimit = false;
					if (quotaPerCycle == 0) {
						noLimit = true;
					}
					
					// short-cut path
					if (noLimit) {
						Iterator<FileRequest> i = this.registeredThreads.iterator();
						while (i.hasNext()) {
							FileRequest r = i.next();
							r.setNoLimit (true);
						}
						synchronized (this) {
							this.notifyAll ();
						}
						continue;
					}
					
					
					/**
					 * 1st step, retrieve what the threads used in the last step and give them the current quota
					 */ 
					// how much will each thread get
					int threadsWithLimit = 0;
					Iterator<FileRequest> i = this.registeredThreads.iterator();
					while (i.hasNext()) {
						FileRequest r = i.next();
						
						if (r.isUnlimitedThread()) {
							continue;
						}
						
						threadsWithLimit++;
					}
					
					// should not happen?
					if (threadsWithLimit == 0) {
						threadsWithLimit = 1;
					}
					
					int quotaPerThread = (int) (quotaPerCycle / threadsWithLimit);
					
					i = this.registeredThreads.iterator();
					while (i.hasNext()) {
						FileRequest r = i.next();
						
						if (r.isUnlimitedThread()) {
							continue;
						}
						
						// we have a limit folks ;)
						r.setNoLimit (false);
						
						if (r.usedAllQuota()) {
							threadsThatDidUseAllQuota++;
						} else {
							// did not use all quota in the last step
							int quotaNotUsed = r.getQuotaNotUsed ();
//							System.err.println(r + " has eq " + quotaNotUsed);
							excessiveQuota = excessiveQuota + quotaNotUsed;
							threadsThatDidNotUseAllQuota++;
						} 
//						System.err.println("ttduaq: " + threadsThatDidUseAllQuota + '/' + registeredThreads.size());
//						System.err.println("eq: " + excessiveQuota);
						
						// set the quota for this thread
//						System.err.println(r + " gets " + quotaPerThread);
						r.setCurrentQuota(quotaPerThread);
					}
					

					/**
					 * 	2nd step, give the threads that did use their quota a part of the excessive quota
					 *  FIXME: optimize the way gtk-gnutella does: 
					 *  The more quota a thread used, the more additional quota he will get
					 */
					
					if ((threadsThatDidNotUseAllQuota > 0) && (threadsThatDidUseAllQuota > 0)) {
						// how much to give each thread
						int excessiveQuotaPerThread = (int) (excessiveQuota / threadsThatDidUseAllQuota);
						i = this.registeredThreads.iterator();
						while (i.hasNext()) {
							FileRequest r = i.next();
							
							if (r.isUnlimitedThread()) {
								continue;
							}

							if (r.usedAllQuota()) {
								r.setCurrentExcessiveQuota(excessiveQuotaPerThread);
							}
						}
					}
					
					// 3 step, notify all threads
					synchronized (this) {
						this.notifyAll ();
					}
					
					synchronized (this.sleeper) {
						this.sleeper.notifyAll ();
					}
					
					/**
					 * move the last item of the registered threads to the first item so we cycle them through.
					 * theoretically this is not needed, but we do this anyway currently (have to think this through)
					 */ 
					
					// because of exceptions, the size may be 0
					if (this.registeredThreads.size() > 0) {
						FileRequest last = this.registeredThreads.lastElement ();
						this.registeredThreads.remove(last);
						this.registeredThreads.insertElementAt(last, 0);
					}
					
					this.registeredThreads.notifyAll();
				}
				
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		
//			System.err.println("<bwt.run");
		}
		
	}

	/**
	 * A list of threads that want their bandwidth limited
	 */
	Vector<FileRequest> registeredThreads = new Vector<FileRequest>();

	public void unregister(FileRequest request) {
//		System.err.println(">bwt.unregister: " + request);
		synchronized (this.registeredThreads) {
			this.registeredThreads.remove(request);
			//if (!request.isUnlimitedThread()) {
				this.threadCount--;
			//}
		}
//		System.err.println("<bwt.unregister");
		
	}


	public void register(FileRequest request) {
//		System.err.println(">bwt.register: " + request);
		synchronized (this.registeredThreads) {
			if (!this.registeredThreads.contains(request)) {
				this.registeredThreads.add(request);
				//if (!request.isUnlimitedThread()) {
					this.threadCount++;
				//}
			}
			
		}	
		
		synchronized (this.sleeper) {
			this.sleeper.notifyAll();
		}
//		System.err.println("<bwt.register");
	
	}
}
