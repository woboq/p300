package de.guruz.p300.hosts.allowing;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
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
import de.guruz.p300.threads.RequestThread;

/**
 * The goal of this host job is just to fire an http request with an allow
 * cookie to make the host possibly implicitly allow us
 * 
 * @author guruz
 *
 */
public class GetMeAllowedByHttpJob implements Runnable {
	private static final ExecutorService m_executor = Executors
			.newCachedThreadPool();

	private GetMeAllowedHelperQueueEntry m_queueEntry;

	public GetMeAllowedByHttpJob(GetMeAllowedHelperQueueEntry qe) {
		m_queueEntry = qe;
	}

	public static void getMeAllowedHttp(GetMeAllowedHelperQueueEntry qe) {
		m_executor.execute(new GetMeAllowedByHttpJob(qe));
	}

	public void run() {
		//D.out("HERE sending a HTTP request for allowing "
		//		+ m_queueEntry.getRemoteIp());

		SynchronousLogicalStreamConnection con = null;
		String ip = m_queueEntry.getRemoteIp();
		int port = m_queueEntry.getRemotePort();
		String path = GetMeAllowedHelper.getHttpPathForCookie(m_queueEntry.getRemoteCookie());

		// exceptions will just be ignored
		try {

			con = TcpHTTPConnectionPool.acquireOrCreateConnection(ip, port, 30*1000);



			// sending the request incl. the hosts we know
			HTTPHeaders outgoingRequestHeaders = new HTTPHeaders();
			outgoingRequestHeaders.setHeader(RequestThread.X_P300_WE_ARE,
					Configuration.instance().getUniqueHash());
			HTTPBodyWriter bodyWriter = HTTPBodyWriterFactory.createWriter(con,
					0, outgoingRequestHeaders);
			HTTPRequestWriter requestWriter = new HTTPRequestWriter(con, ip
					+ ":" + port, HTTPVerb.GET, path, outgoingRequestHeaders);
			requestWriter.write();
			WritableByteChannel requestBodyChannel = bodyWriter
					.getWritableByteChannel();
			requestBodyChannel.close();

			// reading the response
			// just read to the end
			HTTPReplyLine replyLine = new HTTPReplyLine(con.readLine());
			if (!replyLine.isOK())
				throw new Exception("Invalid HTTP reply: " + replyLine);
			HTTPHeaders incomingReplyHeaders = HTTPHeaderReader
					.readHeaders(con);
			HTTPResponseBodyReader responseReader = HTTPResponseBodyReaderFactory
					.createReader(con, replyLine, incomingReplyHeaders);
			BufferedReader reader = new BufferedReader(Channels.newReader(
					responseReader.asReadableByteChannel(), "UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
			}
			reader.close();

			// give the connection back to the pool
			if (con.isConnected() && responseReader.hasFinished()
					&& !responseReader.hasAborted())
				TcpHTTPConnectionPool.releaseConnection(con);

			//D.out("Sent a HTTP request for allowing "
			//		+ m_queueEntry.getRemoteIp() + "(" + replyLine + ")");
			
			if (MainDialog.getHostAllowanceManager().isIpAllowed(ip))
				MainDialog.hostFinderThread.addPossibleP300Host(ip , port);
			
		} catch (java.net.NoRouteToHostException e) {
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (java.net.ConnectException e) {
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (java.nio.channels.UnresolvedAddressException e) {
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (UnknownHostException e) {
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (IOException e) {
			TcpHTTPConnectionPool.abortConnection(con);
		} catch (Exception e) {
			TcpHTTPConnectionPool.abortConnection(con);
			e.printStackTrace();
		}

	}
}
