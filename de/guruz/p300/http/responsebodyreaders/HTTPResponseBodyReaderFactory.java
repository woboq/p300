package de.guruz.p300.http.responsebodyreaders;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;

public class HTTPResponseBodyReaderFactory {
	public static HTTPResponseBodyReader createReader(
			SynchronousLogicalStreamConnection c, HTTPReplyLine rl, HTTPHeaders h) throws Exception {
		String transferEncoding = h.getHeader("Transfer-Encoding");
		String contentLength = h.getHeader("Content-Length");
		
		if (transferEncoding != null && transferEncoding.length() > 0 && transferEncoding.contains("chunked"))
		{
			return new ChunkedHTTPResponseBodyReader (c, rl, h);
		} 
		else if (contentLength != null && contentLength.length() > 0)
		{
			return new BoundedHTTPResponseBodyReader (c, rl, h);
		}
		else
		{
			return new UnboundedHTTPResponseBodyReader (c, rl, h);
		}
	}
}
