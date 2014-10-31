package de.guruz.p300.http.bodywriters;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.http.HTTPHeaders;

public class HTTPBodyWriterFactory {
	/*
	 * "The presence of a message-body in a request is signaled by the inclusion
	 * of a Content-Length or Transfer-Encoding header field in the request's
	 * message-headers."
	 */
	public static HTTPBodyWriter createWriter (SynchronousLogicalStreamConnection c, long bl, HTTPHeaders h)
	{
		if (bl == 0)
		{
			// no body -> null writer
			h.setHeader("Content-length", "0");
			return new NullHTTPBodyWriter ();
		}
		else if (bl == -1)
		{
			// unknown bodylen -> go chunked
			h.setHeader("Transfer-encoding", "chunked");
			return new ChunkedHTTPBodyWriter (c);
			
		}
		else
		{
			// known bodylen
			h.setHeader("Content-length", "" + bl);
			
			return new BoundedHTTPBodyWriter (c, bl);
		}
		
	}
}
