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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

import de.guruz.p300.Configuration;
import de.guruz.p300.Constants;
import de.guruz.p300.MainDialog;
import de.guruz.p300.connections.IncomingTCP;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.hosts.allowing.UnallowedHosts;
import de.guruz.p300.http.ChunkedEncodingHelper;
import de.guruz.p300.http.HTTPHeaderReader;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.http.HTTPVersion;
import de.guruz.p300.logging.D;
import de.guruz.p300.logging.HTTPAccessLog;
import de.guruz.p300.requests.AllowMeRequest;
import de.guruz.p300.requests.Request;
import de.guruz.p300.sessions.Session;
import de.guruz.p300.sessions.SessionList;
import de.guruz.p300.utils.HTTP;

/**
 * This class handles incoming HTTP requests. With the help of a
 * SynchronousLogicalStreamConnection we can support any kind of transport
 * 
 * @author guruz
 */
public class RequestThread implements Runnable {
	public static final String X_P300_WE_ARE = "X-p300-We-Are";

	/**
	 * set to true for verbose output
	 */
	static final boolean debug = false;

	/**
	 * the content sent with OPTIONS or POST or whatever
	 */
	protected byte clientContent[];

	/**
	 * The HTTP verb we received or null if unknown or invalid
	 */
	public HTTPVerb verb;

	/**
	 * The http version
	 */
	public HTTPVersion version;

	/**
	 * The path/filename part of the current request
	 */
	public String path = "";

	/**
	 * local IP of the incoming socket
	 */
	String localIP = "unknown";

	int localPort = -1;

	/**
	 * Content length we send as header. May be -1 when not known, the HTTP
	 * connection then gets closed after the data has been sent
	 */
	long contentLength = -1;

	/**
	 * We are able to do a continued connection because the HTTP client supports
	 * it and we know contentLength
	 */
	boolean doContinuation;

	/**
	 * The request is not new but continuated
	 */
	boolean wasContinuated;

	/**
	 * Are we currently writing out a response using chunked encoding?
	 */
	boolean useChunkedEncoding;

	/**
	 * The remote ip of the socket
	 */
	String remoteIP = "-";

	/**
	 * The remote port of the socket
	 */
	int remotePort = -1;

	/**
	 * The request currently processing, e.g. "GET /bla HTTP/1.0"
	 */
	String requestLine = "";

	/**
	 * how many bytes were written as content with the current request
	 */
	long written;

	/**
	 * we already sent the status line to the client
	 */
	int sentStatus = -1;

	/**
	 * currently writing content/data and not headers
	 */
	boolean atContent;

	/**
	 * do not HTTP access log when closing since for example the FileRequest
	 * class does logging for itself
	 */
	boolean noLog;

	boolean haveLogged;

	// ByteArrayOutputStream bufferedWriteStream;

	// boolean useBufferedWrite;

	ArrayList<Class<?>> possibleHandlers = null;

	SynchronousLogicalStreamConnection connection = null;

	ChunkedEncodingHelper chunkedEncodingHelper = null;

	HTTPHeaderReader httpHeaderReader;

	public RequestThread(SynchronousLogicalStreamConnection c,
			ArrayList<Class<?>> ph) {
		this.connection = c;
		this.possibleHandlers = ph;
		this.httpHeaderReader = new HTTPHeaderReader();
	}

	public void run() {
		// MainDialog.listenThread.increaseActiveHTTPCount();

		try {
			Thread.currentThread().setName(
					"RequestThread From" + this.getRemoteIP() + ":"
							+ this.getRemotePort());
		} catch (Exception e) {
		}

		try {

			do {
				this.written = 0;
				this.sentStatus = -1;
				this.contentLength = -1;
				this.useChunkedEncoding = false;
				this.doContinuation = false;
				this.atContent = false;
				this.noLog = false;
				this.haveLogged = false;
				this.clientContent = null;
				this.httpHeaderReader.clear();
				this.requestLine = null;

				// initial timeout of 15 secs when new, 120 when continuated
				if (!this.wasContinuated) {
					this.connection.setTimeout(15 * 1000);
				} else {
					this.connection
							.setTimeout(Constants.LANVPN_HTTP_KEEPALIVE_TIMEOUT_MSEC);
				}

				String request = "unknown";

				// get the request string
				// request = this.readLine();

				// end of stream. this always happens when a continued
				// connection is timed-out by the browser
				// or also when a upload connection was closed
				// if (request == null) {
				// return;
				// }

				try {
					request = this.readLine();
				} catch (Exception e) {
					this.doContinuation = false;

					// e.printStackTrace();

					// throw new Exception (e);
					return;
				}

				try {
					
					
					
					
					this.requestLine = request;
					this.parseRequestLine(request);

					// this is the right place to check for it, because we
					// always first check
					// if we got a HTTP request anyway
					boolean allowed = MainDialog.getHostAllowanceManager().isIpAllowed(this.remoteIP);
					
					if (!allowed)
					{
						// check if the request is a proper allow me request
						// (implicit allow stuff, see de.guruz.p300.hosts.allowing
						allowed = AllowMeRequest.couldHandle(this.verb, this.path);
						UnallowedHosts.addIP(this.remoteIP);
					}
					
					// still not allowed?
					if (!allowed) {
						this.doContinuation = false;
						this.close(403,
								"Unallowed. Tell my admin to allow your IP "
										+ this.remoteIP);
						UnallowedHosts.addIP(this.remoteIP);
						return;
					}

					// timeout of 20 secs for the header per line
					this.connection.setTimeout(20 * 1000);

					this.httpHeaderReader.read(this.connection);

				} catch (Exception e) {
					this.close();
					e.printStackTrace();
					throw new Exception(e.getMessage());
				}

				// System.out.println ("verb = " + verb.toString());
				// System.out.println("path = " + path);

				if (this.hasClientContent()) {
					// timeout of 20
					this.connection.setTimeout(20 * 1000);

					// we currently only accept 16 kb of data. this should be
					// enough
					// for webDAV and http POST of settings
					// this should later be changed so that the request objects
					// can
					// decide if they want to accept.

					int l = (int) this.getClientContentLength();

					if (l > 16 * 1024) {
						throw new Exception("content posted too long, sorry");
					} else if (l == 0) {
						this.clientContent = new byte[0];
					} else {
						this.clientContent = this.readBytes(l);
					}
				} else {
					this.clientContent = new byte[0];
				}

				// while doing our request, we set the timeout to 120 secs
				this.connection.setTimeout(120 * 1000);

				@SuppressWarnings("unused")
				Request requestObject = null;

				requestObject = this.executeTheRequest();

			} while (this.doContinuation == true);

		} catch (Exception e) {
			this.doContinuation = false;
			System.out.println("Request was from " + this.getRemoteIP() + " ("
					+ this.requestLine + ")");
			e.printStackTrace();

		} finally {
			this.doContinuation = false;
			this.close();
			// MainDialog.listenThread.decreaseActiveHTTPCount();
		}
	}

	protected Request executeTheRequest() throws Exception {
		Request requestObject = null;
		java.lang.Class<?> RequestObjectClass = null;
		Class<?>[] couldHandleParamTypes = new Class[] { HTTPVerb.class,
				String.class };
		Object[] couldHandleParams = new Object[] { this.verb, this.path };

		for (int i = 0; i < this.possibleHandlers.size(); i++) {
			RequestObjectClass = this.possibleHandlers.get(i);
			Method couldHandle = null;

			try {
				couldHandle = RequestObjectClass.getDeclaredMethod(
						"couldHandle", couldHandleParamTypes);
				Boolean result = (Boolean) couldHandle.invoke(
						RequestObjectClass, couldHandleParams);
				if (result.booleanValue()) {
					requestObject = (Request) RequestObjectClass.newInstance();
					requestObject.setRequestThread(this);

					break;
				}
			} catch (Exception e) {
				couldHandle = null;
			}
		}

		if (requestObject == null) {
			this.close(404, "Not found");
		} else {
			requestObject.handle();
			this.addPossibleP300Host();
		}

		return requestObject;

	}

	/**
	 * Checks the referer. It may possibly be a p300 host. We may want to add it
	 * to our HTTP hosts.
	 * 
	 * This will not work in all cases since the http port can be changed
	 * 
	 */
	protected void addPossibleP300Host() {
		String referer = this.getHeader("Referer", "");
		String defaultHttpPort = String.valueOf(Configuration.instance()
				.getDefaultHTTPPort());

		if (referer.contains(':' + defaultHttpPort)) {
			try {
				URL url = new URL(referer);
				MainDialog.hostFinderThread.addPossibleP300Host(url.getHost(), url.getPort());
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public String getHeader(String n, String def) {
		return this.httpHeaderReader.getHeader(n, def);
	}

	public boolean hasClientContent() {
		boolean ret = (this.getClientContentLength() > 0);
		return ret;
	}

	public long getClientContentLength() {
		long len = Long.parseLong(this.getHeader("Content-Length", "0"));

		return len;
	}

	/**
	 * This method parses the request line from the client and throws an
	 * exception if needed
	 * 
	 * @param request
	 * @throws Exception
	 */
	protected void parseRequestLine(String request) throws Exception {
		if (RequestThread.debug) {
			System.out.println();
			System.out.println("REQUEST <".concat(request).concat(
					Character.toString('>')));
		}

		this.version = HTTPVersion.convert(request);
		this.verb = HTTPVerb.convert(request);

		int firstSpace = request.indexOf(' ');
		int lastSpace = request.lastIndexOf(' ');

		if ((firstSpace == -1) || (lastSpace == -1)
				|| (firstSpace == lastSpace)) {
			throw new Exception("No path in request line");
		}

		this.path = request.substring(firstSpace + 1, lastSpace);

		// FIXME: currently we just accept any host and treat it like it is ours
		if (this.path.startsWith("http://")) {
			URL url = new URL(this.path);
			this.path = url.getFile();
		}

		if (this.path.length() == 0) {
			throw new Exception("Empty path");
		}
	}

	/**
	 * The http client is able to handle continued connections
	 * http://www.io.com/~maus/HttpKeepAlive.html
	 * 
	 * @return
	 */
	protected boolean clientSupportsContinuation() {
		// when debugging we dont want this anyway, it would mix up our debug
		// log
		// if (debug)
		// return false;

		// we do not continate in that case
		if (!MainDialog.getHostAllowanceManager().isIpAllowed(
				this.getRemoteIP())) {
			return false;
		}

		String h = null;

		h = this.getHeader("Keep-Alive", null);
		if (h != null) {
			return true;
		}

		// for HTTP 1.1 the default is Keep Alive
		if (this.version == HTTPVersion.HTTP_1_1) {
			h = this.getHeader("Connection", "Keep-Alive").toLowerCase();
		} else {
			h = this.getHeader("Connection", "close").toLowerCase();
		}

		// return h.contains("Keep-Alive".toLowerCase());
		return !h.contains("close");
	}

	/**
	 * Begin sending content to the http client now. This function writes
	 * missing headers
	 * 
	 */
	public void httpContents() throws Exception {
		this.doContinuation = false;

		// Are we low on free slots for incoming connections?
		boolean disableContinuation = false;
		if (MainDialog.listenThread.isLowOnFreeIncomingSlots()) {
			disableContinuation = true;
			D
					.out("Low in free incoming connection slots, disabling keep-alive connections");
		}

		this.write("Server: p300\r\n");
		this.write("Date: " + HTTP.getHTTPDate() + "\r\n");

		if (this.contentLength != -1) {
			this.write("Content-Length: " + this.contentLength + "\r\n");

			if (this.clientSupportsContinuation() && !disableContinuation) {
				this.doContinuation = true;
				this.write("Connection: Keep-Alive\r\n");
			}
			this.write("\r\n");
		} else {
			if (this.clientSupportsChunkedEncoding()
					&& this.clientSupportsContinuation()
					&& !disableContinuation) {
				// System.out.println ("Using chunked encoding!");
				this.write("Transfer-Encoding: chunked\r\n");
				this.write("Connection: Keep-Alive\r\n");
				this.write("\r\n");
				this.useChunkedEncoding = true;
				this.doContinuation = true;
				this.chunkedEncodingHelper = new ChunkedEncodingHelper(
						this.connection);
			} else {
				this.useChunkedEncoding = false;
				this.write("Connection: close\r\n");
				this.write("\r\n");
			}

		}

		// commented out and moved to above, it would mess up the
		// chunked encoding
		// write("\n");

		this.atContent = true;
		this.written = 0;

		this.flush();
	}

	/**
	 * Send a http header
	 * 
	 * @param n
	 *            name of the header
	 * @param v
	 *            content
	 */
	public void httpHeader(String n, String v) throws Exception {
		this.write(n);
		this.write(": ");
		this.write(v);
		this.write("\r\n");
	}

	/**
	 * Send the status line
	 * 
	 * @param code
	 * @param msg
	 */
	public void httpStatus(int code, String msg) throws Exception {
		/*
		 * RFC 2616 requires that HTTP servers always begin their responses with
		 * the highest HTTP version that they claim to support.
		 */
		String s = String.format("HTTP/1.1 %d %s\r\n", code, msg);

		this.sentStatus = code;
		this.write(s);
	}

	/**
	 * close connection with a message and no redirection
	 */
	public void close(int code, String msg) throws Exception {
		this.close(code, msg, null);
	}

	/**
	 * close connection with a message and redirection
	 * 
	 * @param code
	 * @param msg
	 * @param location
	 */
	public void close(int code, String msg, String location) throws Exception {
		this.httpStatus(code, msg);

		if (location != null) {
			this.httpHeader("Location", location);
		}

		StringBuilder errorB = new StringBuilder();
		errorB.append("<html><body><h1>");
		errorB.append(code);
		errorB.append(' ');
		errorB.append(msg);
		errorB
				.append("</h1>Go to <a href='/'>main page</a><br>Go to <a href='http://p300.eu'>p300 page</a></body></html>");
		String error = errorB.toString();
		this.httpContentLength(error.getBytes().length);
		this.httpContents();
		this.write(error);
		this.flush();
		this.close();
		// System.out.println ("bla");
	}

	/**
	 * close the connection. Actually keeps the socket open when we are doing a
	 * continuation.
	 * 
	 */
	public void close() {
		if (RequestThread.debug) {
			System.out.println();
		}

		try {
			// we only flush the chunked encoding stuff when this is not a head
			// request
			if (this.useChunkedEncoding && !this.isHeadRequest()) {
				this.chunkedEncodingHelper.finish();
				this.haveWritten(this.chunkedEncodingHelper.getTotalSent());
				this.useChunkedEncoding = false;
			}

			this.flush();

			// bufferedWriteStream = null;

			if ((this.sentStatus != -1) && !this.noLog && !this.haveLogged) {
				HTTPAccessLog.out(this.getRemoteIP(), this.getRequestLine(),
						this.sentStatus, this.written);

				this.haveLogged = true;
			}

			if (this.doContinuation) {
				this.wasContinuated = true;
			} else {
				try {
					Thread.sleep(10);
					// outputStream.close();
					// inputStream.close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					this.doContinuation = false;
					// D.out("RequestThread: Closing connection "
					// + this.connection);
					this.connection.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * the request is authenticated, i.e. using the correct admin credentials
	 * 
	 * @return
	 */
	public boolean isAuthenticated() {
		// boolean authed = false;
		// String authHeader = getHeader("Authorization", null);
		String cookieHeader = this.getHeader("Cookie", null);

		// erstmal rausgemacht.
		/*
		 * if (authHeader != null) {
		 * 
		 * try { // D.out("Auth header is " + authHeader); String b64 =
		 * authHeader.substring(authHeader.indexOf(" ")) .trim(); String decoded
		 * = Base64.decode(b64); String valid = "admin:" +
		 * Configuration.instance().getAdminPassword(); //
		 * System.out.println("<" + decoded + ">"); // ystem.out.println("<" +
		 * valid + ">");
		 * 
		 * authed = (decoded.equals(valid));
		 * 
		 * if (authed) return true; } catch (Exception e) { e.printStackTrace();
		 * } }
		 */

		if (cookieHeader != null) {

			try {

				Session s = this.getSession();

				if (s == null) {
					return false;
				}

				return s.isAuthed();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public Session getSession() {
		try {
			String cookieHeader = this.getHeader("Cookie", null);
			String cookieName = SessionList.getCookieName(this.getHeader(
					"Host", null));
			int keyIndex = cookieHeader.indexOf(cookieName + '=');

			if (keyIndex == -1) {
				return null;
			}

			int valueIndex = keyIndex + cookieName.length() + 1;

			int semicolonIndex = cookieHeader.indexOf(";", valueIndex);

			// System.out.println("valueIdx=" + valueIndex + " semicolonIdx="
			// + semicolonIndex);

			String value = null;
			if (semicolonIndex == -1) {
				value = cookieHeader.substring(valueIndex);
			} else {
				value = cookieHeader.substring(valueIndex, semicolonIndex);
			}

			return SessionList.get(value);

			// System.out.println("cookie val received = " + value);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public String getLocalDisplay() {
		String displayname = Configuration.instance().getLocalDisplayName();

		if (displayname == null) {
			displayname = this.localIP + ':' + MainDialog.getCurrentHTTPPort();
		}

		return displayname;
	}

	public void httpContentLength(long l) {
		this.contentLength = l;
		// doContinuation = true;
		// System.out.print("length is " + l);
	}

	/**
	 * Sends the authentication header
	 * 
	 * @param realm
	 */
	public void httpAuth(String realm) throws Exception {
		this.httpHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
	}

	/**
	 * Reads a line.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String readLine() throws Exception {
		return this.connection.readLine();
	}

	public byte[] readBytes(int count) {
		try {
			return this.connection.readBytes(count);
		} catch (Exception e) {
			// FIXME?
			e.printStackTrace();
			return null;
		}
	}

	public void flush() {
		try {
			if (this.useChunkedEncoding) {
				this.chunkedEncodingHelper.flush();
			}

			this.connection.flush();
		} catch (Exception e) {
			// FIXME?
			// e.printStackTrace();
		}
	}

	public void write(String s) throws Exception {
		if (RequestThread.debug) {
			System.out.print(s);
		}

		byte buf[] = s.getBytes(Configuration.getDefaultEncoding());

		this.write(buf);

	}

	public void write(long l) throws Exception {
		String ls = Long.toString(l);
		this.write(ls);
	}

	public void write(byte buf[]) throws Exception {
		this.write(buf, buf.length);
	}

	public void write(byte buf[], int len) throws Exception {
		// we are at writing content but only have a HEAD request -> dont
		// write anything
		if (this.isHeadRequest() && this.atContent) {
			return;
		}

		if (this.useChunkedEncoding) {
			this.chunkedEncodingHelper.append(buf, len);

			// we do not do a real flush here because of nagles algorithm
			if (this.chunkedEncodingHelper.needsFlush()) {
				this.chunkedEncodingHelper.flush();
			}

		} else {
			this.connection.write(buf, len);
			this.haveWritten(len);
		}

	}

	public String getRemoteIP() {
		return this.remoteIP;
	}

	public String getRequestLine() {
		return this.requestLine;
	}

	public void haveWritten(long c) {
		// this is only for the logfile
		if (this.atContent) {
			this.written = this.written + c;
		}

		// this is for the global traffic stats
		MainDialog.listenThread.incOutgoingTraffic(c);
	}

	public void doNotLog() {
		this.noLog = true;
	}

	public boolean isHeadRequest() {
		return this.verb == HTTPVerb.HEAD;
	}

	public void httpSessionCookie(String id, String cookiePath)
			throws Exception {
		String cookieName = SessionList.getCookieName(this.getHeader("Host",
				null));

		if (cookiePath == null) {
			this.httpHeader("Set-Cookie", cookieName + '=' + id + cookiePath);
		} else {
			this.httpHeader("Set-Cookie", cookieName + '=' + id + "; Path="
					+ cookiePath);
		}
	}

	public void httpSessionCookie(String id) throws Exception {
		this.httpSessionCookie(id, null);
	}

	public void httpHeadersForDAV() throws Exception {
		this.httpHeader("DAV", "1");
		// httpHeader("DAV", "<http://apache.org/dav/propset/fs/1>");
		this.httpHeader("MS-Author-Via", "DAV");
		this.httpHeader("Allow", "OPTIONS,PROPFIND,GET,HEAD");

	}

	public void httpContentType(String ct) throws Exception {
		this.httpContentType(ct, "utf-8");
	}

	public void httpContentType(String ct, String enc) throws Exception {
		this.httpHeader("Content-Type", ct + "; charset=\"" + enc + "\"");
	}

	public void httpSendWeAreHeader() throws Exception {
		httpHeader(RequestThread.X_P300_WE_ARE, Configuration.instance()
				.getUniqueHash());
	}

	public void setLocalIP(String hostAddress) {
		this.localIP = hostAddress;
	}

	public void setLocalPort(int p) {
		this.localPort = p;
	}

	public int getLocalPort() {
		return this.localPort;
	}

	public void setRemoteIP(String hostAddress) {
		this.remoteIP = hostAddress;
	}

	public void setTrafficClass(byte tos) throws Exception {
		if (this.connection instanceof IncomingTCP) {
			IncomingTCP incomingTCP = (IncomingTCP) this.connection;
			incomingTCP.setTrafficClass(tos);
		}

	}

	public boolean clientSupportsChunkedEncoding() {
		return this.version == HTTPVersion.HTTP_1_1;
	}

	public int getRemotePort() {
		return this.remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public boolean isIndex() {
		return (this.path.equals("/"));
	}

	public String getLocalIP() {
		return this.localIP;
	}

	public byte[] getClientContent() {
		return this.clientContent;
	}

}
