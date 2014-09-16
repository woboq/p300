package de.guruz.p300.http.bodywriters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;

public class BoundedHTTPBodyWriter implements HTTPBodyWriter {

	final SynchronousLogicalStreamConnection m_connection;
	
	final long m_bodyLen;
	
	long m_remaining;
	
	public BoundedHTTPBodyWriter(SynchronousLogicalStreamConnection c, long bl) {
		m_connection = c;
		m_bodyLen = bl;
		m_remaining = m_bodyLen;
	}

	public WritableByteChannel getWritableByteChannel() {
		
		
		return new WritableByteChannel ()
		{
			final WritableByteChannel m_streamChannel = m_connection.asWriteableByteChannel();
			
			public int write(ByteBuffer src) throws IOException {
				if (m_remaining <= 0)
					throw new ClosedChannelException ();
				
				int haveWritten = m_streamChannel.write(src);
				
				m_remaining = m_remaining - haveWritten;
				
				return haveWritten;
			}

			public void close() throws IOException {

			}

			public boolean isOpen() {
				return m_remaining > 0;
			}
			
		};
	}

}
