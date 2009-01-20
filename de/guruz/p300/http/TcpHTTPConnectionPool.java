package de.guruz.p300.http;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.guruz.p300.connections.ConnectionFactory;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;

public class TcpHTTPConnectionPool {
	protected static class PoolEntry {
		final long m_expiresOn;

		final SynchronousLogicalStreamConnection m_connection;

		public PoolEntry(SynchronousLogicalStreamConnection c) {
			super();
			m_expiresOn = System.currentTimeMillis() + 30 * 1000;
			m_connection = c;
		}

		public boolean checkIfStillUsable() {
			if (m_connection.isConnected() == false) {
				//D.out(m_connection + " not usable: isConnected is false");
				return false;
			}

			if (m_connection.isDataAvailable() == true) {
				//D.out(m_connection + " not usable: isDataAvailable is true (which probably is the EOF)");
				return false;
			}
			
			if (m_connection.isConnected() == false) {
				//D.out(m_connection + " not usable: isConnected is false");
				return false;
			}

			return true;
		}

		public SynchronousLogicalStreamConnection getConnection() {
			return m_connection;
		}

		public void expireConnection() {
			try {
				m_connection.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// "The head of the queue is that element that has been on the queue the
	// longest time. The tail of the queue is that element that has been on the
	// queue the shortest time."
	protected static HashMap<String, ConcurrentLinkedQueue<PoolEntry>> m_pool = new HashMap<String, ConcurrentLinkedQueue<PoolEntry>>();

	protected static int acquireCount = 0;
	protected static int releaseCount = 0;
	protected static int abortCount = 0;

	public static SynchronousLogicalStreamConnection acquireOrCreateConnection(
			String host, int port, int timeout) throws Exception {
		SynchronousLogicalStreamConnection c = acquireConnection(host, port, timeout);
		
		if (c == null)
		{
			c = ConnectionFactory.createOutgoingTCP(host, port,
					timeout);
		}
		
		return c;
	}
	
	public static SynchronousLogicalStreamConnection acquireConnection(
			String host, int port) throws Exception {
		return acquireConnection(host, port, 60 * 1000);
	}

	public static SynchronousLogicalStreamConnection acquireConnection(
			String host, int port, int msectimeout) throws Exception {

		SynchronousLogicalStreamConnection retval = null;
		PoolEntry pe = null;
		ConcurrentLinkedQueue<PoolEntry> queue = null;
		acquireCount++;
		// FIXME better OO
		String key = host + ":" + port;

		while (retval == null) {
			synchronized (TcpHTTPConnectionPool.class) {
				queue = m_pool.get(key);
				if (queue == null) {
					// FIXME should be a normal queue
					queue = new ConcurrentLinkedQueue<PoolEntry>();
					m_pool.put(key, queue);
				}

				pe = queue.poll();
			}

			if (pe == null) {
				break;
			} else if (pe.checkIfStillUsable()) {
				retval = pe.getConnection();
				displayStats("REUSING", queue);
				break;
			} else {
				pe.expireConnection();
			}
		}

		if (retval == null)
			displayStats("NEW", queue);

		return retval;
	}

	public static void releaseConnection(SynchronousLogicalStreamConnection c) {
		synchronized (TcpHTTPConnectionPool.class) {
			releaseCount++;

			// FIXME better OO
			String key = c.getKey();

			// queue always exists :)
			ConcurrentLinkedQueue<PoolEntry> queue = m_pool.get(key);
			queue.add(new PoolEntry(c));

			displayStats("RELEASE", queue);
		}
	}

	public static void abortConnection(SynchronousLogicalStreamConnection c) {
		if (c == null)
			return;

		synchronized (TcpHTTPConnectionPool.class) {
			abortCount++;

			c.close();

			String key = c.getKey();

			displayStats("ABORT", m_pool.get(key));
		}
	}

	private static void displayStats(String msg,
			ConcurrentLinkedQueue<PoolEntry> queue) {
		synchronized (TcpHTTPConnectionPool.class) {
			//D.out("TcpHTTPConnectionPool size=" + queue.size() + " acquire="
			//		+ acquireCount + " release=" + releaseCount + " abort="
			//		+ abortCount + " - " + msg);
		}

	}
}
