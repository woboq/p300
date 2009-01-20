/**
 * 
 */
package de.guruz.p300.webdav.search.client;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.guruz.p300.Configuration;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.hosts.HostLocation;
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
import de.guruz.p300.threads.RequestThread;
import de.guruz.p300.utils.DOMUtils;

/**
 * Performs a search on a single host by sending the request document
 * Retrieves the result and sends it to the SearchResultCollector
 * @author tomcat
 *
 */
public class SingleHostSearchWorker implements Runnable {

	private static final String COULD_NOT_CONNECT_MSG = "Could not connect to host";
	private static final String COULD_NOT_SEND_QUERY_MSG = "Could not send query";
	private static final String COULD_NOT_READ_RESULT_MSG = "Could not read result";
	private static final String DAV_NAMESPACE = "DAV:";
	
	/**
	 * The host to search
	 */
	private Host myHost;
	
	/**
	 * The query to send
	 */
	private Document query;
	
	/**
	 * Create a new Worker that will search a single host
	 * Needs a list of collectors, a host to query, and a query to send
	 * @param collectors
	 * @param myHost
	 * @param query
	 */
	public SingleHostSearchWorker(List<SearchResultCollector> collectors,
			Host myHost, Document query) {
		super();
		this.collectors = collectors;
		this.myHost = myHost;
		this.query = query;
	}

	/**
	 * The list of result collectors to notify
	 */
	private List<SearchResultCollector> collectors;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
//		System.err.println("run()");
		if (!checkPreconditions()) {
			return;
		}
		HostLocation myHostLocation = myHost.getBestHostLocation();
		String hostIP = myHostLocation.getIp();
		int hostPort = myHostLocation.getPort();
//		Test with "nc -l -p 3333" on cmdline
//		String hostIP = "127.0.0.1";
//		int hostPort = 3333;
//		System.err.println("Connecting");
		SynchronousLogicalStreamConnection connection = getConnection(hostIP, hostPort);
//		System.err.println("Checking connection");
		if (connection == null) {
			sendErrorToCollectors(COULD_NOT_CONNECT_MSG);
			return;
		}
//		System.err.println("Sending query");
		String queryAsString = DOMUtils.prettyPrintXMLDocument(query);
		byte[] queryAsBytes;
		try {
			queryAsBytes = queryAsString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException unsupportedEncoding) {
			sendErrorToCollectors(COULD_NOT_SEND_QUERY_MSG + ": " + unsupportedEncoding.getLocalizedMessage());
			return;
		}
		try {
			sendQuery(connection, queryAsBytes, hostIP + ":" + hostPort);
		} catch (Exception ex) {
			sendErrorToCollectors(COULD_NOT_SEND_QUERY_MSG + ": " + ex.getLocalizedMessage());
			return;
		}
//		System.err.println("Receiving result");
		receiveResult(connection);
	}
	
	/**
	 * Receive a result from the host after sending the query
	 * @param connection
	 */
	private void receiveResult(SynchronousLogicalStreamConnection connection) {
//		System.err.println("receiveResult()");
		HTTPReplyLine replyLine;
//		System.err.println("Reading first reply line");
		try {
			replyLine = new HTTPReplyLine(connection.readLine());
		} catch (Exception ex) {
			sendErrorToCollectors(COULD_NOT_READ_RESULT_MSG + ": " + ex.getLocalizedMessage());
			return;
		}
//		System.err.println("Checking reply status code");
		if (replyLine.getNr() != 207) {
//			System.err.println("Status code is " + replyLine.getNr());
			sendErrorToCollectors(COULD_NOT_READ_RESULT_MSG + ": HTTP status code " + replyLine.getNr());
			return;
		}
//		System.err.println("Reading reply");
		HTTPHeaders replyHeaders;
		HTTPResponseBodyReader replyBody;
		try {
			replyHeaders = HTTPHeaderReader.readHeaders(connection);
			replyBody = HTTPResponseBodyReaderFactory.createReader(connection, replyLine, replyHeaders);
		} catch (Exception ex) {
			sendErrorToCollectors(COULD_NOT_READ_RESULT_MSG + ": " + ex.getLocalizedMessage());
			return;
		}
		BufferedReader reader = new BufferedReader(Channels.newReader(replyBody.asReadableByteChannel(), "UTF-8"));
		Document replyDocument = DOMUtils.documentFromInputSource((new InputSource(reader)));
//		System.err.println("Checking reply XML");
		if (replyDocument == null) {
			sendErrorToCollectors(COULD_NOT_READ_RESULT_MSG);
			return;
		}
		receiveResult(replyDocument);
	}

	/**
	 * Send a query given as bytestream to the given connection
	 * @param connection
	 * @param queryAsBytes
	 */
	private void sendQuery(SynchronousLogicalStreamConnection connection,
			byte[] queryAsBytes, String hostHeader) throws Exception {
		// Send headers
		HTTPHeaders queryHeaders = new HTTPHeaders();
		queryHeaders.setHeader(RequestThread.X_P300_WE_ARE, Configuration.instance().getUniqueHash());
		queryHeaders.setHeader("Content-Type", "application/xml; charset=\"utf-8\"");
		HTTPBodyWriter queryBody = HTTPBodyWriterFactory.createWriter(connection, queryAsBytes.length, queryHeaders);

		// Send request
		HTTPRequestWriter queryRequest = new HTTPRequestWriter(connection, hostHeader, HTTPVerb.SEARCH, "/", queryHeaders);
		queryRequest.write();

		// Send query body
		WritableByteChannel queryBodyChannel = queryBody.getWritableByteChannel();
		queryBodyChannel.write(ByteBuffer.wrap(queryAsBytes));
		queryBodyChannel.close();
	}

	/**
	 * Get a connection either from the pool or create a new one
	 * Target is the host of this worker
	 * @return
	 */
	private SynchronousLogicalStreamConnection getConnection(String hostIP, int hostPort) {
		SynchronousLogicalStreamConnection connection;
		try {
			connection = TcpHTTPConnectionPool.acquireOrCreateConnection(hostIP, hostPort, 30*1000);
			return connection;
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Check if the preconditions (host, collectors, query) are okay
	 * @return
	 */
	private boolean checkPreconditions() {
		if (myHost == null) {
			sendErrorToCollectors(COULD_NOT_CONNECT_MSG + ": Host specification wrong");
			return false;
		}
		HostLocation myHostLocation = myHost.getBestHostLocation();
		if (myHostLocation == null) {
			sendErrorToCollectors(COULD_NOT_CONNECT_MSG + ": Host location specification wrong");
			return false;
		}
		String ip = myHostLocation.getIp();
		int port = myHostLocation.getPort();
		if (ip == null || (port < 1 || port > 65535)) {
			sendErrorToCollectors(COULD_NOT_CONNECT_MSG + ": Host location specification wrong");
			return false;
		}
		if (collectors == null) {
			collectors = Collections.emptyList();
		}
		if (query == null) {
			sendErrorToCollectors(COULD_NOT_SEND_QUERY_MSG + ": Query specification wrong");
			return false;
		}
		return true;
	}

	/**
	 * Receive the result document from the host
	 * @param result
	 */
	private void receiveResult(Document result) {
//		System.err.println("receiveResult()");
		List<RemoteEntity> resultList = convertResultDocumentToRemoteEntityList(result);
		if (resultList.size() == 0) {
			sendNoResultToCollectors();
		} else {
			sendResultToCollectors(resultList);
		}
	}
	
	/**
	 * Convert a result DOM document to a list of RemoteEntity objects
	 * @param result
	 * @return
	 */
	private static List<RemoteEntity> convertResultDocumentToRemoteEntityList(Document result) {
//		System.err.println("convertResultDocumentToRemoteEntityList()");
//		System.err.println(DOMUtils.prettyPrintXMLDocument(result));
		List<RemoteEntity> resultList = new ArrayList<RemoteEntity>();
		if (!isValidResult(result)) {
			return Collections.emptyList();
		}
		NodeList responses = result.getElementsByTagNameNS(DAV_NAMESPACE, "response");
		for (int i = 0; i < responses.getLength(); i++) {
			Node response = responses.item(i);
			Node hrefNode = DOMUtils.getFirstNamedChildNS(response, DAV_NAMESPACE, "href");
			Node hrefTextNode = DOMUtils.getFirstTextChild(hrefNode);
			String hrefText = hrefTextNode.getNodeValue();
			Node propstatNode = DOMUtils.getFirstNamedChildNS(response, DAV_NAMESPACE, "propstat");
			Node propNode = DOMUtils.getFirstNamedChildNS(propstatNode, DAV_NAMESPACE, "prop");
			Node resourcetypeNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "resourcetype");
			RemoteEntity entity;
			if (isDirectory(resourcetypeNode)) {
				entity = new RemoteDir(hrefText);
			} else {
				Node getcontentlengthNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "getcontentlength");
				Node getcontentlengthTextNode = DOMUtils.getFirstTextChild(getcontentlengthNode);
				String getcontentlengthText = getcontentlengthTextNode.getNodeValue();
				long size = Long.parseLong(getcontentlengthText);
				
				entity = new RemoteFile(hrefText);
				entity.setSize(size);
			}
			resultList.add(entity);
		}
		return resultList;
	}
	
	/**
	 * Check if the given "resourcetype" Node specified a DAV collection
	 * @param resourcetypeNode
	 * @return
	 */
	private static boolean isDirectory(Node resourcetypeNode) {
//		System.err.println("isDirectory()");
		if (resourcetypeNode == null) {
			return false;
		}
		Node collectionNode = DOMUtils.getFirstNamedChildNS(resourcetypeNode, DAV_NAMESPACE, "collection");
		if (collectionNode == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check if the result is valid
	 * @param result
	 * @return
	 */
	private static boolean isValidResult(Document result) {
//		System.err.println("isValidResult()");
		if (result == null) {
			return false;
		}
		NodeList responses = result.getElementsByTagNameNS(DAV_NAMESPACE, "response");
		for (int i = 0; i < responses.getLength(); i++) {
			Node response = responses.item(i);
			if (response == null) {
				return false;
			}
			Node hrefNode = DOMUtils.getFirstNamedChildNS(response, DAV_NAMESPACE, "href");
			if (hrefNode == null) {
				return false;
			}
			Node hrefTextNode = DOMUtils.getFirstTextChild(hrefNode);
			if (hrefTextNode == null) {
				return false;
			}
			String hrefText = hrefTextNode.getNodeValue();
			if (hrefText == null || hrefText.length() == 0) {
				return false;
			}
			Node propstatNode = DOMUtils.getFirstNamedChildNS(response, DAV_NAMESPACE, "propstat");
			if (propstatNode == null) {
				return false;
			}
			Node propNode = DOMUtils.getFirstNamedChildNS(propstatNode, DAV_NAMESPACE, "prop");
			if (propNode == null) {
				return false;
			}
			Node displaynameNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "displayname");
			Node getcontentlengthNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "getcontentlength");
			if (displaynameNode == null || getcontentlengthNode == null) {
				return false;
			}
			Node displaynameTextNode = DOMUtils.getFirstTextChild(displaynameNode);
			Node getcontentlengthTextNode = DOMUtils.getFirstTextChild(getcontentlengthNode);
			if (displaynameTextNode == null || getcontentlengthTextNode == null) {
				return false;
			}
			String displaynameText = displaynameTextNode.getNodeValue();
			String getcontentlengthText = getcontentlengthTextNode.getNodeValue();
			if (displaynameText == null || displaynameText.length() == 0 || getcontentlengthText == null || getcontentlengthText.length() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Send the search result list to all collectors
	 * @param resultList
	 */
	private void sendResultToCollectors(List<RemoteEntity> resultList) {
//		System.err.println("sendResultToCollectors()");
		for (SearchResultCollector collector: collectors) {
			collector.newResult(myHost, resultList);
		}
	}
	
	/**
	 * Send a "no result" information to all search collectors
	 */
	private void sendNoResultToCollectors() {
		for (SearchResultCollector collector: collectors) {
			collector.noResult(myHost);
		}
	}
	
	/**
	 * Send an error information to all search collectors
	 * @param msg
	 */
	private void sendErrorToCollectors(String msg) {
//		System.err.println("sendErrorToCollectors()");
		for (SearchResultCollector collector: collectors) {
			collector.hasError(myHost, msg);
		}
	}
	
}