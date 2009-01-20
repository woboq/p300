package de.guruz.p300.http.bodywriters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class NullHTTPBodyWriter implements HTTPBodyWriter {

	public WritableByteChannel getWritableByteChannel() {
		return new WritableByteChannel () {

			public int write(ByteBuffer src) throws IOException {
				return src.remaining();
			}

			public void close() throws IOException {
			}

			public boolean isOpen() {
				return true;
			}
			
		};
	}

}
