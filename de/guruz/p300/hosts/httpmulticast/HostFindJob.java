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
package de.guruz.p300.hosts.httpmulticast;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.connections.ConnectionFactory;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.http.HTTPHeaderReader;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;
import de.guruz.p300.http.HTTPRequestWriter;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.http.TcpHTTPConnectionPool;
import de.guruz.p300.http.bodywriters.HTTPBodyWriter;
import de.guruz.p300.http.bodywriters.HTTPBodyWriterFactory;
import de.guruz.p300.http.responsebodyreaders.HTTPResponseBodyReader;
import de.guruz.p300.http.responsebodyreaders.HTTPResponseBodyReaderFactory;
import de.guruz.p300.logging.D;
import de.guruz.p300.threads.RequestThread;
import de.guruz.p300.utils.IP;

/**
 * A job created by the HostFinderThread that actually queries a host
 * 
 * @author guruz
 * 
 */
public class HostFindJob implements Runnable {
	protected HTTPMulticastHost m_multicastHost;

	protected String m_host;

	protected int m_port;

	protected HostFinderThread m_hostFinderThread;

	public HostFindJob(HTTPMulticastHost h, HostFinderThread hft) {
		m_multicastHost = h;
		m_host = m_multicastHost.getIP();
		m_port = m_multicastHost.getPort();
		m_hostFinderThread = hft;
	}

	public void run() {

		// D.out("Asking " + m_multicastHost.getHostPort()
		// + " for hosts");
		m_multicastHost.updateLastQueryTime();
		m_multicastHost.status = HTTPMulticastHost.HttpMulticastStatusType.USED;
		m_multicastHost.connectionStatus = "";

		if (IP.isOurIP(m_multicastHost.getIP())
				&& MainDialog.getCurrentHTTPPort() == m_multicastHost.getPort()) {
			m_multicastHost.status = HTTPMulticastHost.HttpMulticastStatusType.OURSELVES;
			return;
		}

		SynchronousLogicalStreamConnection con = null;

		try {

			con = TcpHTTPConnectionPool.acquireConnection(m_host, m_port);

			if (con == null) {
				InetAddress ia = InetAddress.getByName(m_host);

				if (IP.isOurIP(ia.getHostAddress())) {
					m_multicastHost.status = HTTPMulticastHost.HttpMulticastStatusType.OURSELVES;
					return;
				}

				con = ConnectionFactory.createOutgoingTCP(m_host, m_port,
						30 * 1000);
			}

			// System.out.println("Connected to " +
			// m_multicastHost.getHostPort()
			// );

			byte[] knownHosts = MainDialog.hostMap.writeOutKnownHosts()
					.getBytes("UTF-8");

			// sending the request incl. the hosts we know
			HTTPHeaders outgoingRequestHeaders = new HTTPHeaders();
			outgoingRequestHeaders.setHeader(RequestThread.X_P300_WE_ARE,
					Configuration.instance().getUniqueHash());
			HTTPBodyWriter bodyWriter = HTTPBodyWriterFactory.createWriter(con,
					knownHosts.length, outgoingRequestHeaders);
			HTTPRequestWriter requestWriter = new HTTPRequestWriter(con, m_host
					+ ":" + m_port, HTTPVerb.POST, m_multicastHost
					.getHostfinderPath(), outgoingRequestHeaders);
			requestWriter.write();
			WritableByteChannel requestBodyChannel = bodyWriter
					.getWritableByteChannel();
			requestBodyChannel.write(ByteBuffer.wrap(knownHosts));
			requestBodyChannel.close();

			// reading the response
			HTTPReplyLine replyLine = new HTTPReplyLine(con.readLine());
			if (replyLine.getNr() == 403)
				throw new UnallowedException(
						"We are not yet allowed by this host");
			if (!replyLine.isOK())
				throw new Exception("Invalid HTTP reply: " + replyLine);
			HTTPHeaders incomingReplyHeaders = HTTPHeaderReader
					.readHeaders(con);
			HTTPResponseBodyReader responseReader = HTTPResponseBodyReaderFactory
					.createReader(con, replyLine, incomingReplyHeaders);
			BufferedReader reader = new BufferedReader(Channels.newReader(
					responseReader.asReadableByteChannel(), "UTF-8"));

			String incomingXP300WeAreHeader = incomingReplyHeaders
					.getHeader(RequestThread.X_P300_WE_ARE);
			if (incomingXP300WeAreHeader != null
					&& incomingXP300WeAreHeader.length() > 0
					&& incomingXP300WeAreHeader.equals(Configuration.instance()
							.getUniqueHash())) {
				// woops. we are asking oursevles


				// throw new Exception("Asked ourself");

				while (reader.readLine() != null) {
				}
				reader.close();
				
				m_multicastHost.status = HTTPMulticastHost.HttpMulticastStatusType.OURSELVES;
				m_multicastHost.connectionStatus = "Ourselves";

				TcpHTTPConnectionPool.abortConnection(con);
				
				D.out ("Hostfinder: Not asking ourselves on " + m_host + ":" + m_port);
				
			} else {
				// we are properly asking another node
				String line = reader.readLine();
				while (line != null) {
					MainDialog.hostFinderThread.parseHTTPMulticastLine(
							line, m_multicastHost.getIP());
					
					//if (line.contains(" - "))
					//	D.out ("Outgoing req: from " + m_multicastHost.getIP() + " received " + line);
					
					line = reader.readLine();
				}
				reader.close();

				m_multicastHost.status = HTTPMulticastHost.HttpMulticastStatusType.OK;
				m_multicastHost.connectionStatus = "OK";
				if (con.isConnected() && responseReader.hasFinished()
						&& !responseReader.hasAborted())
					TcpHTTPConnectionPool.releaseConnection(con);

				m_hostFinderThread.reQueueHost(m_multicastHost);

				D.out("Hostfinder: Properly asked " + m_host + ":" + m_port);
			}

		} catch (UnallowedException e) {
			m_multicastHost.status = HTTPMulticastHost.HttpMulticastStatusType.UNALLOWED;
			m_multicastHost.connectionStatus = e.getLocalizedMessage();
			m_hostFinderThread.reQueueHost(m_multicastHost);
			D.out("Hostfinder: Could not query " + m_host + ":" + m_port + " ("
					+ e.getMessage() + ")");
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (java.net.NoRouteToHostException e) {
			m_hostFinderThread.reQueueHost(m_multicastHost);
			m_multicastHost.connectionStatus = e.getLocalizedMessage();
			D.out("Hostfinder: Could not connect to " + m_host + ":" + m_port
					+ " (" + e.getMessage() + ")");
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (java.net.ConnectException e) {
			m_hostFinderThread.reQueueHost(m_multicastHost);
			m_multicastHost.connectionStatus = e.getLocalizedMessage();
			D.out("Hostfinder: Could not connect to " + m_host + ":" + m_port
					+ " (" + e.getMessage() + ")");
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (java.nio.channels.UnresolvedAddressException e) {
			m_multicastHost.connectionStatus = e.getLocalizedMessage();
			D.out("Hostfinder: Could not connect to " + m_host + ":" + m_port
					+ " (Could not resolve address)");
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (UnknownHostException e) {
			m_multicastHost.connectionStatus = e.getLocalizedMessage();
			D.out("Hostfinder: Could not connect to " + m_host + ":" + m_port
					+ " (Unknown host)");
			TcpHTTPConnectionPool.abortConnection(con);

		} catch (IOException e) {
			m_hostFinderThread.reQueueHost(m_multicastHost);
			m_multicastHost.connectionStatus = e.getLocalizedMessage();
			D.out("Hostfinder: IO Exception when talking to " + m_host + ":"
					+ m_port + " (" + e.getMessage() + ")");
			// e.printStackTrace();
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (Exception e) {
			// e.printStackTrace();
			m_hostFinderThread.reQueueHost(m_multicastHost);
			m_multicastHost.connectionStatus = e.getLocalizedMessage();
			D.out("Hostfinder: Exception when talking to " + m_host + ":"
					+ m_port + " (" + e.getMessage() + ")");
			TcpHTTPConnectionPool.abortConnection(con);
			e.printStackTrace();
		}

		// return (m_multicastHost.status ==
		// HTTPMulticastHost.HttpMulticastStatusType.OK);

	}

	private class UnallowedException extends Exception {

		public UnallowedException(String s) {
			super(s);
		}
	};

}
