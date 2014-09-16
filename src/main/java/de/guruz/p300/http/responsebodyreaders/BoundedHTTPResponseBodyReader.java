package de.guruz.p300.http.responsebodyreaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;

public class BoundedHTTPResponseBodyReader extends
		AbstractHTTPResponseBodyReader {

	long toBeRead = 0;
	
	boolean aborted = false;

	public BoundedHTTPResponseBodyReader(
			SynchronousLogicalStreamConnection c, HTTPReplyLine rl,
			HTTPHeaders h) {
		super(c, rl, h);

		//System.out.println ("Bounded reader " + c + " " + rl.toString());
		
		String contentLength = h.getHeader("Content-Length");
		toBeRead = Long.parseLong(contentLength);
	}

	public boolean hasFinished() {
		return toBeRead <= 0;
	}

//	public int read(byte[] buf, int howMuch) throws Exception {
//		int howMuchCouldRead = (int) (Math.min(Math.min(toBeRead, howMuch),
//				buf.length));
//
//		if (howMuchCouldRead > 0) {
//			int haveRead = m_connection.readBytes(buf, howMuchCouldRead);
//			
//			if (haveRead == -1)
//			{
//				aborted = true;
//				throw new Exception ("Connection was closed");
//			}
//
//			toBeRead = toBeRead - haveRead;
//
//			return haveRead;
//		}
//		
//		throw new Exception ("Entity alread finished");
//
//	}

	public boolean hasAborted() {
		return aborted;
	}



	public ReadableByteChannel asReadableByteChannel() {
		return new ReadableByteChannel () {
			public int read(ByteBuffer bb) throws IOException {
				//D.out ("read");
				
				if (bb.limit() > toBeRead)
					bb.limit((int) toBeRead);
				
				if (toBeRead > 0)
				{
					int haveRead = m_readableChannel.read(bb);
					
					if (haveRead == -1)
					{
						aborted = true;
						throw new IOException ("Connection was closed");
					}
					
					toBeRead = toBeRead - haveRead;

					return haveRead;
				}
				
				//throw new IOException ("Entity alread finished");
				return -1;
			}

			public void close() throws IOException {
				//D.out ("close");
			}

			public boolean isOpen() {
				boolean ret = !hasFinished () && !hasAborted () && m_connection.isConnected();
				//D.out ("isOpen " + ret);
				return ret;
			}
		};
	}

}
