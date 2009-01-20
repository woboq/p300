package de.guruz.p300.http.responsebodyreaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;

public class UnboundedHTTPResponseBodyReader extends AbstractHTTPResponseBodyReader {
	boolean finished = false;

	public UnboundedHTTPResponseBodyReader(
			SynchronousLogicalStreamConnection c, HTTPReplyLine rl,
			HTTPHeaders h) {
		super(c, rl, h);
	}
	
	public boolean hasFinished() {
		return finished;
	}

//	public int read(byte[] buf, int howMuch) throws Exception {
//		int read = m_connection.readBytes(buf, howMuch);
//		
//		if (read == -1)
//			finished = true;
//		
//		return read;
//	}

	public boolean hasAborted() {
		return false;
	}


	public ReadableByteChannel asReadableByteChannel() {
		return new ReadableByteChannel ()
		{

			public int read(ByteBuffer bb) throws IOException {
				int read =  m_readableChannel.read(bb);
				
				if (read == -1)
					finished = true;
				
				return read;
			}

			public void close() throws IOException {
//				try {
//					m_connection.close();
//				} catch (Exception e) {
//					IOException ioe = new IOException ();
//					ioe.initCause(e);
//					throw ioe;
//				}
				
			}

			public boolean isOpen() {
				return (!hasFinished () && !hasAborted ()) || !m_connection.isConnected();
			}
			
		};
	}

}
