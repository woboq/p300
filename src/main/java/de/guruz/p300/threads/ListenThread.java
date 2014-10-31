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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.guruz.p300.Configuration;
import de.guruz.p300.Constants;
import de.guruz.p300.MainDialog;
import de.guruz.p300.connections.ConnectionFactory;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.logging.D;
import de.guruz.p300.requests.AboutRequest;
import de.guruz.p300.requests.AllowMeRequest;
import de.guruz.p300.requests.ConfigRequest;
import de.guruz.p300.requests.DAVRequest;
import de.guruz.p300.requests.FileRequest;
import de.guruz.p300.requests.FwdRequest;
import de.guruz.p300.requests.HostFinderRequest;
import de.guruz.p300.requests.HostlistHTMLRequest;
import de.guruz.p300.requests.IndexRequest;
import de.guruz.p300.requests.LanMessageRequest;
import de.guruz.p300.requests.LoginRequest;
import de.guruz.p300.requests.LogoutRequest;
import de.guruz.p300.requests.OneTimeRequest;
import de.guruz.p300.requests.OptionsRequest;
import de.guruz.p300.requests.SearchRequest;
import de.guruz.p300.requests.StaticInternalFileRequest;
import de.guruz.p300.requests.UnknownRequest;
import de.guruz.p300.requests.WebDAVSearchRequest;

/**
 * The listen thread takes care of incoming HTTP LAN requests
 * 
 * @author guruz
 */
public class ListenThread extends Thread implements RejectedExecutionHandler {
	ServerSocketChannel serverSocketChannel;
	int port;
	

	private ThreadPoolExecutor threadPoolExecutor;
	//private LinkedBlockingQueue<Runnable> threadPool = new LinkedBlockingQueue<Runnable> (); 
	private Queue<Runnable> threadPool = new SynchronousQueue<Runnable> (true);
	
	//ExecutorService requestExecutorService;
	
	public ListenThread () throws Exception {
			this.port = Configuration.instance().getDefaultHTTPPort();
			
			for (int i = this.port; i < this.port+10; i++) {
				try {
					this.serverSocketChannel = ServerSocketChannel.open();
					this.serverSocketChannel.socket().bind(new InetSocketAddress (i));

					break;
				} catch (Exception e) {
					D.out("WARNING: could not listen on HTTP port " + i + ", trying the next one");
					this.serverSocketChannel = null;
				}
			}
			
			if (this.serverSocketChannel == null) {
				throw new Exception ("Could not bind a socket");
			}
			
			this.port = this.serverSocketChannel.socket().getLocalPort();
	}
	
	@Override
	public void run() {
		try {
			this.setName ("ListenThread");
		} catch (Exception e) {
		}
		
		D.out("HTTP Server / Webinterface started on http://127.0.0.1:" + this.port);

		ArrayList<Class<?>> possibleHandlers = new ArrayList<Class<?>>();
		// the first are probably the most used
		possibleHandlers.add(HostFinderRequest.class);
		possibleHandlers.add(AllowMeRequest.class);
		possibleHandlers.add(IndexRequest.class);

		possibleHandlers.add(DAVRequest.class);
		possibleHandlers.add(FileRequest.class);
		possibleHandlers.add(ConfigRequest.class);
		possibleHandlers.add(LanMessageRequest.class);
		
		possibleHandlers.add(LoginRequest.class);
		possibleHandlers.add(LogoutRequest.class);
		possibleHandlers.add(OneTimeRequest.class);
		possibleHandlers.add(OptionsRequest.class);
		possibleHandlers.add(HostlistHTMLRequest.class);
		
		// also often used, but takes the longest processing time ;)
		possibleHandlers.add(StaticInternalFileRequest.class);
		possibleHandlers.add(AboutRequest.class);
		possibleHandlers.add(SearchRequest.class);
		possibleHandlers.add(WebDAVSearchRequest.class);
		possibleHandlers.add(FwdRequest.class);
		
		
		// if nothing matches
		possibleHandlers.add(UnknownRequest.class);
		

		threadPoolExecutor = new ThreadPoolExecutor(10,
				Constants.MAX_INCOMING_HTTP_CONNECTIONS, // FIXME
                25,
                TimeUnit.SECONDS,
                (BlockingQueue<Runnable>) threadPool,
                this);
		
	
		
		while (true) {
			try {
				// Pass the socket to a new thread so that it can be dealt with
				// while we can go and get ready to accept another connection.
				//this.serverSocketChannel.socket().setSoTimeout(30 * 1000);
				SocketChannel socketChannel = this.serverSocketChannel.accept();

				SynchronousLogicalStreamConnection incomingTCP = ConnectionFactory.createIncomingTCP(socketChannel);
				
				RequestThread requestThread = new RequestThread(incomingTCP, possibleHandlers);
				requestThread.setLocalPort (port);
				requestThread.setLocalIP (socketChannel.socket().getLocalAddress().getHostAddress());
				requestThread.setRemoteIP (socketChannel.socket().getInetAddress().getHostAddress());
				requestThread.setRemotePort(socketChannel.socket().getPort());
					
				//DNSCache.lookupForLater(requestThread.getRemoteIP());
					
				threadPoolExecutor.execute(requestThread);
				
				//requestExecutorService.submit(requestThread);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
//	/**
//	 * Count of active HTTP connections
//	 * @author guruz
//	 * @see #decreaseActiveHTTPCount()
//	 * @see #getActiveHTTPCount()
//	 * @see #increaseActiveUploadCount()
//	 * @see #decreaseActiveUploadCount()
//	 * @see #getActiveUploadCount()
//	 * @see #increaseActiveHTTPCount()
//	 * @see #activeUploadCount
//	 */
//	private int activeHTTPCount;

//	/**
//	 * Increase the number of open HTTP connections 
//	 * @author guruz
//	 * @see #decreaseActiveHTTPCount()
//	 * @see #getActiveHTTPCount()
//	 * @see #increaseActiveUploadCount()
//	 * @see #decreaseActiveUploadCount()
//	 * @see #getActiveUploadCount()
//	 * @see #activeHTTPCount
//	 * @see #activeUploadCount
//	 */
//	public void increaseActiveHTTPCount() {
//		synchronized (this) {
//			this.activeHTTPCount++;
//			//System.err.println("(inc) HTTP connections: " + activeHTTPCount);
//		}
//	}
//
//	/**
//	 * Decrease the number of open HTTP connections
//	 * @author guruz
//	 * @see #getActiveHTTPCount()
//	 * @see #increaseActiveUploadCount()
//	 * @see #decreaseActiveUploadCount()
//	 * @see #getActiveUploadCount()
//	 * @see #activeHTTPCount
//	 * @see #activeUploadCount
//	 * @see #increaseActiveHTTPCount()
//	 */
//	public void decreaseActiveHTTPCount() {
//		synchronized (this) {
//			this.activeHTTPCount--;
//			//System.err.println("(dec) HTTP connections: " + activeHTTPCount);
//		}
//	}

	/**
	 * Return the number of open HTTP connections
	 * @return 0 to inf: Number of open HTTP connections
	 * @author guruz
	 * @see #increaseActiveUploadCount()
	 * @see #decreaseActiveUploadCount()
	 * @see #getActiveUploadCount()
	 * @see #activeHTTPCount
	 * @see #activeUploadCount
	 * @see #increaseActiveHTTPCount()
	 * @see #decreaseActiveHTTPCount()
	 */
//	public synchronized int getActiveHTTPCount() {
//		//return this.activeHTTPCount;
//		return threadPoolExecutor.getActiveCount();
//	
//	}
	
	
	/**
	 * Number of active uploads
	 * @author guruz
	 * @see #increaseActiveUploadCount()
	 * @see #decreaseActiveUploadCount()
	 * @see #getActiveUploadCount()
	 * @see #activeHTTPCount
	 * @see #increaseActiveHTTPCount()
	 * @see #decreaseActiveHTTPCount()
	 * @see #getActiveHTTPCount()
	 * @see #printUploadConnections()
	 * @see #lastActiveUploadCount
	 */
	private int activeUploadCount;

	/**
	 * Increase the number of active uploads
	 * @author guruz
	 * @see #decreaseActiveUploadCount()
	 * @see #getActiveUploadCount()
	 * @see #activeHTTPCount
	 * @see #activeUploadCount
	 * @see #increaseActiveHTTPCount()
	 * @see #decreaseActiveHTTPCount()
	 * @see #getActiveHTTPCount()
	 * @see #activeUploadCount
	 * @see #printUploadConnections()
	 * @see #lastActiveUploadCount
	 */
	public void increaseActiveUploadCount() {
		synchronized (this) {
			this.activeUploadCount++;
			 //System.err.println("Upload connections: " + activeUploadCount);
		}
	}

	/**
	 * Decrease the number of active uploads
	 * @author guruz
	 * @see #getActiveUploadCount()
	 * @see #activeHTTPCount
	 * @see #activeUploadCount
	 * @see #increaseActiveHTTPCount()
	 * @see #decreaseActiveHTTPCount()
	 * @see #getActiveHTTPCount()
	 * @see #activeUploadCount
	 * @see #increaseActiveUploadCount()
	 * @see #printUploadConnections()
	 * @see #lastActiveUploadCount
	 */
	public void decreaseActiveUploadCount() {
		synchronized (this) {
			this.activeUploadCount--;
			 //System.err.println("Upload connections: " + activeUploadCount);
		}
	}

	/**
	 * Return the number of active uploads
	 * @return 0 to inf: Number of active uploads
	 * @author guruz
	 * @see #activeHTTPCount
	 * @see #activeUploadCount
	 * @see #increaseActiveHTTPCount()
	 * @see #decreaseActiveHTTPCount()
	 * @see #getActiveHTTPCount()
	 * @see #activeUploadCount
	 * @see #increaseActiveUploadCount()
	 * @see #decreaseActiveUploadCount()
	 * @see #printUploadConnections()
	 * @see #lastActiveUploadCount
	 */
	public synchronized int getActiveUploadCount() {
		return this.activeUploadCount;
	}	
	
	

	// No one needs this
	/**
	 * @author guruz
	 */
	public void stopListening() {
	}

	/**
	 * Last number of active uploads
	 * @author guruz
	 * @see #printUploadConnections()
	 * @see #activeHTTPCount
	 * @see #activeUploadCount
	 * @see #increaseActiveHTTPCount()
	 * @see #decreaseActiveHTTPCount()
	 * @see #getActiveHTTPCount()
	 * @see #activeUploadCount
	 * @see #increaseActiveUploadCount()
	 * @see #decreaseActiveUploadCount()
	 * @see #getActiveUploadCount()
	 */
	int lastActiveUploadCount;

	/**
	 * Print number of current uploads if it has changed
	 * and update {@link #lastActiveUploadCount}
	 * @author guruz 
	 * @see #lastActiveUploadCount
	 * @see #activeHTTPCount
	 * @see #activeUploadCount
	 * @see #increaseActiveHTTPCount()
	 * @see #decreaseActiveHTTPCount()
	 * @see #getActiveHTTPCount()
	 * @see #activeUploadCount
	 * @see #increaseActiveUploadCount()
	 * @see #decreaseActiveUploadCount()
	 * @see #getActiveUploadCount()
	 */
	public void printUploadConnections() {
		int uploads = this.getActiveUploadCount();
		if (this.lastActiveUploadCount != uploads ) {
			D.out("Uploads = " + uploads);
			
			this.lastActiveUploadCount = uploads;
		}
	}
	
	
	/**
	 * Total incoming traffic since startup
	 * @author guruz
	 * @see #outgoingTraffic
	 * @see #incIncomingTraffic(long)
	 * @see #incOutgoingTraffic(long)
	 */
	public long incomingTraffic;

	/**
	 * Total outgoing traffic since startup
	 * @author guruz
	 * @see #incomingTraffic
	 * @see #incIncomingTraffic(long)
	 * @see #incOutgoingTraffic(long)
	 */
	public long outgoingTraffic;
	/**
	 * Increase incoming traffic by x
	 * @param x 
	 * @author guruz
	 * @see #incomingTraffic
	 * @see #outgoingTraffic
	 * @see #incOutgoingTraffic(long)
	 */
	public void incIncomingTraffic (long x) {
		this.incomingTraffic = this.incomingTraffic + x;
	}
	
	/**
	 * Increase outgoing traffic by x
	 * @param x
	 * @author guruz
	 * @see #incomingTraffic
	 * @see #outgoingTraffic
	 * @see #incIncomingTraffic(long)
	 */
	public void incOutgoingTraffic (long x) {
		this.outgoingTraffic = this.outgoingTraffic + x;
	}
	
	/**
	 * Return a list of all URLs pointing to current instance
	 * Like "http://<myip>:<myport>/"
	 * @return A list of URL strings
	 * @author guruz
	 */
	public String[] getKnownLocalURLs () {
		Vector<String> v = new Vector<String>();
		
		try {
			int port = MainDialog.getCurrentHTTPPort();
			Enumeration<NetworkInterface> nifs = java.net.NetworkInterface
					.getNetworkInterfaces();
			
			while (nifs.hasMoreElements()) {
				NetworkInterface nif = nifs.nextElement();
				Enumeration<InetAddress> ips = nif.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress current_ip = ips.nextElement();
					if (current_ip instanceof Inet4Address)
					{
						v.add ("http://" + current_ip.getHostAddress() + ':' + port);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] ret = new String[v.size()];
		for (int i = 0; i < v.size(); i++) {
			ret[i] = v.get(i);
		}
		
		return ret;
	}

	public void rejectedExecution(Runnable r, ThreadPoolExecutor tpe) {
//		if ((r instanceof RequestThread) && (tpe == this.threadPoolExecutor)) {
			RequestThread rt = (RequestThread) r;
			D.out(
				"Too many connections (" + Constants.MAX_INCOMING_HTTP_CONNECTIONS+ "), ignored "
						+ rt.getRemoteIP());
		
			try {
				rt.close();
			} catch (Exception e) {
			
			}
//		}
	}

	public int getPort() {
		return port;
	}

	public boolean isLowOnFreeIncomingSlots() {
		return (threadPoolExecutor.getActiveCount() > 0.75 * Constants.MAX_INCOMING_HTTP_CONNECTIONS);
	}


}
