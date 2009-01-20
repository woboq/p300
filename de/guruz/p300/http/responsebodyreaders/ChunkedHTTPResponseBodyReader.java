package de.guruz.p300.http.responsebodyreaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.http.HTTPHeaderReader;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;

/*
 * Get information on http://jmarshall.com/easy/http/
 */
public class ChunkedHTTPResponseBodyReader extends
		AbstractHTTPResponseBodyReader {

	private enum ReadingStateType {
		READING_CHUNK_SIZE, READING_CHUNK_DATA, READING_TRAILER
	};

	protected ReadingStateType state;

	int chunkSize = -1;

	int remainingChunkSize = -1;

	boolean aborted = false;
	
	boolean footerWasRead = false;

	public ChunkedHTTPResponseBodyReader(
			SynchronousLogicalStreamConnection c, HTTPReplyLine rl,
			HTTPHeaders h) {
		super(c, rl, h);

		state = ReadingStateType.READING_CHUNK_SIZE;
	}

	public boolean hasFinished() {
		// a chunk with length 0 was read
		return !aborted && remainingChunkSize == 0 && chunkSize == 0 && footerWasRead;
	}

	public boolean hasAborted() {
		return aborted;
	}



	public ReadableByteChannel asReadableByteChannel() {
		return new ReadableByteChannel ()
		{

			public int read(ByteBuffer bb) throws IOException {
				try {
					if (hasFinished() || hasAborted())
						return -1;

					// switch through our states
					switch (state) {
					case READING_CHUNK_SIZE: {
						// "a line with the size of the chunk data, in hex, possibly
						// followed by a semicolon and extra parameters you can ignore (none
						// are currently standard), and ending with CRLF."
						String line = m_connection.readLine();

						String lineSplitted[] = line.split(";");

						int cs = Integer.parseInt(lineSplitted[0].trim(), 16);

						// FIXME

						if (cs > 0)
							state = ReadingStateType.READING_CHUNK_DATA;
						else
							state = ReadingStateType.READING_TRAILER;

						chunkSize = cs;
						remainingChunkSize = cs;

						// call us again, this time for READING_CHUNK_DATA (or trailer)
						return read (bb);
					}
					case READING_CHUNK_DATA: {

						int howMuchCouldRead = (int) (Math.min(remainingChunkSize, bb
								.remaining()));
						
						//System.out.println ("could read " + howMuchCouldRead);
						
						bb.limit(bb.position() + howMuchCouldRead);

						int haveRead = m_readableChannel.read(bb);
						if (haveRead == -1) {
							aborted = true;
							throw new Exception("Premature EOF");
						}

						remainingChunkSize = remainingChunkSize - haveRead;

						if (remainingChunkSize == 0) {
							// "followed by CRLF."
							byte crlf[] = m_connection.readBytes(2);
							if (crlf == null || crlf.length != 2 || crlf[0] != 13
									|| crlf[1] != 10) {
								aborted = true;
								throw new Exception("Improper termination of chunk data");
							}

							// set state
							state = ReadingStateType.READING_CHUNK_SIZE;
						}

						return haveRead;
					}
					case READING_TRAILER: {
						// "Note the blank line after the last footer. [...] The footers
						// should be treated like headers, as if they were at the top of the
						// response."

						// just read and ignore
						
						new HTTPHeaderReader().read(m_connection);
						footerWasRead = true;
						
						
						return -1;

					}

					}

					// this will not happen? :)
					throw new Exception("Wont happen..");
				} catch (Exception e) {
					IOException ioe = new IOException ();
					ioe.initCause(e);
					throw ioe;
				}
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
				return (!hasFinished () && !hasAborted ()) && m_connection.isConnected();
			}
			
		};
	}

}
