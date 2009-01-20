package de.guruz.p300.http.bodywriters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;

public class ChunkedHTTPBodyWriter implements HTTPBodyWriter {

	final SynchronousLogicalStreamConnection m_connection;

	public ChunkedHTTPBodyWriter(SynchronousLogicalStreamConnection c) {
		m_connection = c;
	}

	/*
	 * Return the WritableByteChannel that can be used to send data
	 */
	public WritableByteChannel getWritableByteChannel() {	
		return new WritableByteChannel ()
		{
			final WritableByteChannel m_streamChannel = m_connection.asWriteableByteChannel();
			
			ByteBuffer m_chunkStartBuffer = ByteBuffer.allocate(15);
			
			/*
			 * Write the length of a chunk. See the HTTP RFC.
			 */
			protected void writeChunkLen (int len) throws IOException
			{
				m_chunkStartBuffer.clear();
				m_chunkStartBuffer.put(Integer.toHexString(len).getBytes ("ASCII"));
				m_chunkStartBuffer.put((byte) 13);
				m_chunkStartBuffer.put((byte) 10);
				
				if (len == 0)
				{
					// last chunk
					m_chunkStartBuffer.put((byte) 13);
					m_chunkStartBuffer.put((byte) 10);					
				}
				
				m_chunkStartBuffer.flip();
				
				while (m_chunkStartBuffer.remaining() > 0)
				{
					int ret = m_streamChannel.write(m_chunkStartBuffer);
					
					if (ret == -1)
						throw new ClosedChannelException ();
				}
			}

			public int write(ByteBuffer src) throws IOException {
				writeChunkLen (src.remaining());
				
				while (src.remaining() > 0)
				{
					int ret = m_streamChannel.write(src);
					
					if (ret == -1)
						throw new ClosedChannelException ();
				}
				
				return src.limit();
			}

			/*
			 * closing = sending chunk of length 0
			 */
			public void close() throws IOException {
				writeChunkLen (0);
			}

			/*
			 * At the moment, always return true. Does not matter currently.
			 */
			public boolean isOpen() {
				return true;
			}
			
		};
	}
}
