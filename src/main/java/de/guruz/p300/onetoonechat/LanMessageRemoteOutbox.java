package de.guruz.p300.onetoonechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.guruz.p300.Configuration;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
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
import de.guruz.p300.onetoonechat.ui.UiMessageRouter;
import de.guruz.p300.requests.LanMessageRequest;
import de.guruz.p300.threads.RequestThread;

public class LanMessageRemoteOutbox {

	protected ExecutorService m_executor = Executors.newCachedThreadPool();

	public void route(final Message m) {
		Runnable r = new Runnable() {

			public void run() {
				// send remotely, if succeeded tell this the UI
				

				SynchronousLogicalStreamConnection con = null;
				HostLocation hl = m.getTo().getBestHostLocation();
				String host = hl.getIp();
				int port = hl.getPort();

				try {

					con = TcpHTTPConnectionPool.acquireOrCreateConnection(host, port, 30*1000);


					String xml = m.serializeToXML();
					byte xmlBytes[] = xml.getBytes("UTF-8");

					// sending the request incl. the hosts we know
					HTTPHeaders outgoingRequestHeaders = new HTTPHeaders();
					outgoingRequestHeaders.setHeader(
							RequestThread.X_P300_WE_ARE, Configuration
									.instance().getUniqueHash());
					HTTPBodyWriter bodyWriter = HTTPBodyWriterFactory
							.createWriter(con, xmlBytes.length,
									outgoingRequestHeaders);
					HTTPRequestWriter requestWriter = new HTTPRequestWriter(
							con, host + ":" + port, HTTPVerb.POST,
							LanMessageRequest.CHATPATH, outgoingRequestHeaders);
					requestWriter.write();
					WritableByteChannel requestBodyChannel = bodyWriter
							.getWritableByteChannel();
					requestBodyChannel.write(ByteBuffer.wrap(xmlBytes));
					requestBodyChannel.close();

					// reading the response
					HTTPReplyLine replyLine = new HTTPReplyLine(con.readLine());
					if (replyLine.getNr() == 403) {
						// FIXME route error msg to ui
						m_uiMessageRouter.route(new ErrorMessage("We are not allowed by this host", m));
						throw new Exception(
								"We are not yet allowed by this host");
					} else if (!replyLine.isOK()) {
						m_uiMessageRouter.route(new ErrorMessage("Error when sending message ("+replyLine.getMsg() +")", m));
						throw new Exception("Invalid HTTP reply: " + replyLine);
					}

					HTTPHeaders incomingReplyHeaders = HTTPHeaderReader
							.readHeaders(con);
					HTTPResponseBodyReader responseReader = HTTPResponseBodyReaderFactory
							.createReader(con, replyLine, incomingReplyHeaders);
					BufferedReader reader = new BufferedReader(Channels
							.newReader(responseReader.asReadableByteChannel(),
									"UTF-8"));
					// read possible stuff
					while (reader.read() != -1)
						;

					if (con.isConnected() && responseReader.hasFinished()
							&& !responseReader.hasAborted()) {
						TcpHTTPConnectionPool.releaseConnection(con);
					}

					if (m_uiMessageRouter != null) {
						// route to UI
						m_uiMessageRouter.route(m);
					}

				} catch (java.net.NoRouteToHostException e) {
					m_uiMessageRouter.route(new ErrorMessage("No Route to Host (" + e.getMessage() + ")", m));
					TcpHTTPConnectionPool.abortConnection(con);
				} catch (java.net.ConnectException e) {
					m_uiMessageRouter.route(new ErrorMessage("Connect Fault (" + e.getMessage() + ")", m));
					TcpHTTPConnectionPool.abortConnection(con);
				} catch (IOException e) {
					m_uiMessageRouter.route(new ErrorMessage("IO Fault (" + e.getMessage() + ")", m));
					 e.printStackTrace();
					TcpHTTPConnectionPool.abortConnection(con);
				} catch (Exception e) {
					// e.printStackTrace();
					m_uiMessageRouter.route(new ErrorMessage("Exception (" + e.getMessage() + ")", m));
					TcpHTTPConnectionPool.abortConnection(con);
					e.printStackTrace();
				}

			}

		};

		m_executor.execute(r);
	}

	UiMessageRouter m_uiMessageRouter;

	public void setUiMessageRouter(UiMessageRouter umr) {
		m_uiMessageRouter = umr;
	}

}
