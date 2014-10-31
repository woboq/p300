package de.guruz.p300.http.responsebodyreaders;

import java.nio.channels.ReadableByteChannel;

public interface HTTPResponseBodyReader {
	//int read(byte buf[], int howMuch) throws Exception;
	
	/**
	 * -1 means EOF
	 * everything else is the number of bytes read
	 * @throws Exception 
	 */
	//public int read (ByteBuffer bb) throws IOException;

	boolean hasFinished();
	
	boolean hasAborted ();
	
	public ReadableByteChannel asReadableByteChannel ();
}
