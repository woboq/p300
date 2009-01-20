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
package de.guruz.p300.dirbrowser;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.davclient.DavParsingUtils;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.hosts.HostLocation;
import de.guruz.p300.http.HTTPHeaderReader;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;
import de.guruz.p300.http.HTTPRequestWriter;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.http.TcpHTTPConnectionPool;
import de.guruz.p300.http.responsebodyreaders.HTTPResponseBodyReader;
import de.guruz.p300.http.responsebodyreaders.HTTPResponseBodyReaderFactory;
import de.guruz.p300.utils.DOMUtils;

/**
 * Creates us a DavDirListing from a Host
 * 
 * @author guruz
 * 
 * 
 */
public class DavDirListingProvider extends DirListingProvider {

	protected static ExecutorService executor;

	static {
		executor = Executors.newCachedThreadPool();

	}

	protected static final class FetcherRunnable implements Runnable {

		private final RemoteDir d;

		private final DirListingReceiver dirListingReceiver;

		private final String hostname;

		private final int port;

		protected FetcherRunnable(RemoteDir d, DirListingReceiver dlr, Host host) {
			this.d = d;
			this.dirListingReceiver = dlr;
			HostLocation loc = host.getBestHostLocation();
			hostname = loc.getIp();
			port = loc.getPort();
			//D.out ("|-- Runnable created for " + d.getPath());
		}

		protected FetcherRunnable(RemoteDir d, DirListingReceiver dlr,
				String h, int p) {
			this.d = d;
			this.dirListingReceiver = dlr;

			hostname = h;
			port = p;
		}

		public void run() {
			// create a DavDirListing
			DirListing ddl = new DavDirListing();
			SynchronousLogicalStreamConnection con = null;
			HTTPHeaders headers = null;
			String replyline = null;
			HTTPReplyLine hrl = null;
			HTTPHeaderReader hhr = null;
			HTTPResponseBodyReader rbr = null;

			//D.out ("|-- Runnable running for " + d.getPath());
			
			try {
				con = TcpHTTPConnectionPool.acquireOrCreateConnection(hostname, port, 30*1000);

				headers = new HTTPHeaders();
				headers.setHeader("Depth", "1");
				new HTTPRequestWriter(con, hostname + ":" + port,
						HTTPVerb.PROPFIND, d.getPath(), headers).write();

				replyline = con.readLine();
				hrl = new HTTPReplyLine(replyline);
				// System.out.println(hrl.getVersion().toString());
				// System.out.println(hrl.getNr());
				// System.out.println(hrl.getMsg());
				hhr = new HTTPHeaderReader();
				hhr.read(con);

				// check for http 200
				if (!hrl.isOK())
					throw new Exception("HTTP " + hrl.getNr() + ": \""
							+ hrl.getMsg() + "\"");

				rbr = HTTPResponseBodyReaderFactory.createReader(con, hrl, hhr
						.getHeaders());

				InputStream is = Channels.newInputStream(rbr
						.asReadableByteChannel());

				Document doc = DOMUtils.documentFromInputStream(is);

				if (doc != null)
					parseResponseXmlTree(doc, ddl);
				else
					throw new Exception ("Could not fetch Dirlisting");
				

				// everything went fine, we can send the connection back to the
				// pool
				if (con.isConnected() && rbr.hasFinished() && !rbr.hasAborted())
					TcpHTTPConnectionPool.releaseConnection(con);
				else
					TcpHTTPConnectionPool.abortConnection(con);

				ddl.setError(null);
			} catch (Exception e) {
				// e.printStackTrace();
				ddl.setError(e.toString());
				e.printStackTrace();
				ddl.clear();
				
				try {
				TcpHTTPConnectionPool.abortConnection(con);
				} catch (Exception f) {
					f.printStackTrace();
				}
			} finally {
				// deliver to the cache
				dirListingReceiver.fetchDone(d, ddl);
			}
		}

		private void parseResponseXmlTree(Document doc, DirListing ddl)
				throws Exception {
			// start the dav stuff
			Node first = doc.getFirstChild();
			if (first == null || !first.getNodeName().equals("DAV:multistatus"))
				throw new Exception("Parsing WebDAV: No multistatus element");

			NodeList responses = doc.getElementsByTagNameNS("DAV:", "response");
			for (int i = 0; i < responses.getLength(); i++) {
				Node reponseNode = responses.item(i);
				parseEntry(ddl, reponseNode);
			}
		}

		private void parseEntry(DirListing ddl, Node reponseNode) {
			Element reponseElement = (Element) reponseNode;
			
			RemoteEntity re = DavParsingUtils.parseReponseNode(reponseNode);

			// this is the current item
			if (re.getPath().equals(d.getPath()))
				return;

			ddl.addEntity(re);

		}
	}

	public static void asyncFetch(final DirListingReceiver dlr,
			final RemoteDir d, final Host host) {
		Runnable r = new FetcherRunnable(d, dlr, host);

		executor.execute(r);
		//D.out ("|-- Runnable started for " + d.getPath());
	}

	public static void asyncFetch(final DirListingReceiver dlr,
			final RemoteDir d, final String hostname, final int port) {
		Runnable r = new FetcherRunnable(d, dlr, hostname, port);

		executor.execute(r);
	}

}
