package de.guruz.p300.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.hosts.HostLocation;

public class HTTPHeadFetcher implements Callable<HTTPHeaders> {

	String m_url;

	String m_hostname;
	int m_port;
	String m_path;
	
	public HTTPHeadFetcher(String u) throws MalformedURLException {
		m_url = u;
		URL url = new URL(m_url);
		m_hostname = url.getHost();
		m_port = url.getPort();
		if (m_port == -1)
			m_port = 80;
		m_path = url.getPath();
		if (m_path == null || m_path.trim().length() == 0)
			m_path = "/";
	}

	public HTTPHeaders call() throws Exception {
		SynchronousLogicalStreamConnection con = null;
		HTTPHeaders headers = null;
		String replyline = null;
		HTTPReplyLine hrl = null;
		HTTPHeaderReader hhr = null;
		HTTPHeaders responseHeaders = null;

		try {

			con = TcpHTTPConnectionPool.acquireOrCreateConnection(m_hostname, m_port, 30*1000);

			new HTTPRequestWriter(con, m_hostname + ":" + m_port, HTTPVerb.HEAD, m_path, null)
					.write();

			replyline = con.readLine();
			hrl = new HTTPReplyLine(replyline);
			hhr = new HTTPHeaderReader();
			hhr.read(con);

			// check for http 200
			if (!hrl.isOK())
				throw new Exception("HTTP " + hrl.getNr() + ": \""
						+ hrl.getMsg() + "\"");
			
			responseHeaders = hhr.getHeaders();
			
			// FIXME: Read until end
		} catch (Exception e) {
			throw new Exception (e);
		} finally {
			if (con != null) {
				if (con.isConnected())
					TcpHTTPConnectionPool.releaseConnection(con);
				else
					TcpHTTPConnectionPool.abortConnection(con);
			}
		}
		
		return responseHeaders;
	}
	

	public HostLocation getHostLocation() {
		return new HostLocation (m_hostname, m_port);
	}
	
	public String getPath ()
	{
		return m_path;
	}


}
