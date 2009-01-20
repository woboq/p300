package de.guruz.p300.http.responsebodyreaders;

import java.nio.channels.ReadableByteChannel;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;

public abstract class AbstractHTTPResponseBodyReader implements HTTPResponseBodyReader {


	final SynchronousLogicalStreamConnection m_connection;
	final HTTPReplyLine m_replyLine;
	final HTTPHeaders m_headers;
	final ReadableByteChannel m_readableChannel;

	
	protected AbstractHTTPResponseBodyReader(SynchronousLogicalStreamConnection c,HTTPReplyLine rl, 
			HTTPHeaders h) {
		m_connection = c;
		m_replyLine = rl;
		m_headers = h;
		m_readableChannel = m_connection.asReadableByteChannel();
	}

}
