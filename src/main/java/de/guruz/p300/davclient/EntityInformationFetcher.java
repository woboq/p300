package de.guruz.p300.davclient;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.concurrent.Callable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.dirbrowser.RemoteEntity;
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

public class EntityInformationFetcher implements Callable<RemoteEntity> {

	String m_url;

	String m_hostname;
	int m_port;
	String m_path;

	public EntityInformationFetcher(String u) throws MalformedURLException {
		m_url = u;

		URL url = new URL(u);
		m_hostname = url.getHost();
		m_port = url.getPort();
		if (m_port == -1)
			m_port = 80;
		m_path = url.getPath();
		if (m_path == null || m_path.trim().length() == 0)
			m_path = "/";

	}

	public RemoteEntity call() throws Exception {
		SynchronousLogicalStreamConnection con = null;
		HTTPHeaders headers = null;
		String replyline = null;
		HTTPReplyLine hrl = null;
		HTTPHeaderReader hhr = null;
		HTTPResponseBodyReader rbr = null;
		RemoteEntity entity = null;

		try {

			con = TcpHTTPConnectionPool.acquireOrCreateConnection(m_hostname, m_port, 30*1000);

			headers = new HTTPHeaders();
			headers.setHeader("Depth", "0");
			new HTTPRequestWriter(con, m_hostname + ":" + m_port, HTTPVerb.PROPFIND, m_path, headers)
					.write();

			replyline = con.readLine();
			hrl = new HTTPReplyLine(replyline);
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
			if (doc == null)
				throw new Exception ("Parsing XML: Could not get XML");

			// parse here
			Node first =  DOMUtils.getFirstNontextChild(doc);
			if (first == null || !first.getNodeName().equals("DAV:multistatus"))
				throw new Exception(
						"Parsing WebDAV: No multistatus element");
			
			Node response = DOMUtils.getFirstNontextChild(first);
			if (response == null || !response.getNodeName().equals("DAV:response"))
				throw new Exception(
						"Parsing WebDAV: No response element");
			
			entity = DavParsingUtils.parseReponseNode(response);
		} catch (Exception e) {
			throw new Exception (e);
		} finally {
			if (con != null) {
				if (con.isConnected() && rbr.hasFinished() && !rbr.hasAborted())
					TcpHTTPConnectionPool.releaseConnection(con);
				else
					TcpHTTPConnectionPool.abortConnection(con);
			}
		}
		
		return entity;

	}

	public HostLocation getHostLocation() {
		return new HostLocation (m_hostname, m_port);
	}

}
