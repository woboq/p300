package de.guruz.p300.http.bodywriters;

import java.nio.channels.WritableByteChannel;

public interface HTTPBodyWriter {
	public WritableByteChannel getWritableByteChannel ();
}
