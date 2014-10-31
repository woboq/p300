/*
 Copyright 2006, 2007, 2008 Markus Goetz, Sebastian Breier
 Webpage on http://p300.eu/
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.guruz.p300.connections;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import de.guruz.p300.logging.D;

/**
 * An implementation of SynchronousLogicalStreamConnection for TCP sockets.
 * 
 * @author guruz
 * 
 */
public abstract class TCP implements SynchronousLogicalStreamConnection {

	protected boolean debug = false;

	protected SocketChannel socketChannel = null;

	protected Selector selector = null;

	protected String m_key;

	protected long m_timeout = 10 * 1000;

	protected TCP() {
	}

	public String toString ()
	{
		return socketChannel.toString();
	}
	
	public int selectNow() {
		try {

			SelectionKey sk = null;

			sk = socketChannel.keyFor(selector);

			if (sk == null)
				sk = socketChannel.register(selector, SelectionKey.OP_READ);

			sk.interestOps(SelectionKey.OP_READ);

			int count = selector.selectNow();

			selector.selectedKeys().remove(sk);

			// D.out(this.socketChannel + " selectNow count was " + count);

			return count;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		return 0;
	}

	protected void select(int f) throws IOException {

		SelectionKey sk = null;

		long timeoutedTime = System.currentTimeMillis() + m_timeout - 1000;

		sk = socketChannel.keyFor(selector);

		if (sk == null)
			sk = socketChannel.register(selector, f);

		sk.interestOps(f);

		int count = selector.select(m_timeout);

		selector.selectedKeys().remove(sk);

		if (/*count == 0 &&*/ System.currentTimeMillis() > timeoutedTime) {
			close();
			throw new IOException("Timeout while select ()");
		}

	}

	protected void finalize() throws Throwable {
		if (selector != null)
			selector.close();
	}

	public void doClose() {
		close();
	}

	public void close() {
		// System.out.println("We are closing " + this.socketChannel.toString()
		// + "");

		try {
			if (selector != null && selector.isOpen()) {
				selector.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if (this.socketChannel.isOpen() && this.socketChannel.isConnected())
		// {

		try {
			if (this.socketChannel.isConnected()
					&& !this.socketChannel.isBlocking())
				this.socketChannel.configureBlocking(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (!this.socketChannel.socket().isOutputShutdown()
					&& this.socketChannel.isConnected())
				this.socketChannel.socket().shutdownOutput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		try {
			if (!this.socketChannel.socket().isInputShutdown()
					&& this.socketChannel.isConnected())
				this.socketChannel.socket().shutdownInput();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		try {
			if (this.socketChannel.isOpen())
				this.socketChannel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			this.selectNow();
		} catch (Exception e) {
			// e.printStackTrace();
		}

	}

	public void flush() throws Exception {
		// hopefully this flushes data, i am not sure
		socketChannel.socket().setTcpNoDelay(true);
		socketChannel.socket().setTcpNoDelay(false);
	}

//	public int readBytes(byte[] buf, int count) throws Exception {
//		ByteBuffer bb = ByteBuffer.wrap(buf, 0, count);
//
//		int retval = this.socketChannel.read(bb);
//		if (retval == 0) {
//			select(SelectionKey.OP_READ);
//			retval = this.socketChannel.read(bb);
//		}
//		return retval;
//	}

	/*
	 * FIXME: guruz modified this so it does not return null when less than
	 * count bytes were read
	 */
	public byte[] readBytes(int count) throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(count);

		while (bb.remaining() > 0) {

			socketChannel.read(bb);

			if (bb.remaining() > 0) {
				select(SelectionKey.OP_READ);
			}
		}

		return bb.array();
	}

	ByteBuffer currentLine = null;

	public String readLine() throws Exception {
		if (!isConnected())
			throw new Exception("Channel already closed");

		if (currentLine == null) {
			currentLine = ByteBuffer.allocate(1024);
		}

		currentLine.clear();

		// "CRLF, though you should gracefully handle lines ending in just LF"
		ByteBuffer bb = ByteBuffer.allocate(1);
		byte currentByte = 0;
		while (currentLine.remaining() > 0 && currentByte != 10) {
			if (!isConnected())
				throw new Exception(
						"Connection was closed while trying to read line");

			bb.clear();

			int haveRead = this.socketChannel.read(bb);
			// System.out.println (currentLine.toString() + " " +
			// bb.toString());
			if (haveRead == -1) {
				// D.out(this.socketChannel.toString() + " "
				// + this.getClass().getCanonicalName()
				// + this.socketChannel.isConnected()
				// + this.socketChannel.isBlocking());
				throw new Exception("Premature EOF while reading line");
			}

			if (haveRead == 0) {
				select(SelectionKey.OP_READ);
				continue;
			}

			// in the byte we have the current char
			bb.flip();
			currentByte = bb.get();

			if (currentByte == 13) {
				// System.out.println ("13");
			} else if (currentByte == 10) {
				// System.out.println ("10");
				// yay :) end of line
				break;
			} else {
				// a proper byte
				currentLine.put(currentByte);
			}

		}

		String s = new String(currentLine.array(), 0, currentLine.position(),
				"UTF-8");
		// System.out.println ("<- >" + s + "<");

		if (s == null)
			D.out("allocated s was null");

		return s;
	}

	public void setTimeout(long to) {

		m_timeout = to;

	}

	public long getTimeout() {
		return m_timeout;
	}

	public void setTrafficClass(byte tos) throws Exception {
		try {
			this.socketChannel.socket().setTrafficClass(tos);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void write(byte[] buf) throws Exception {
		this.write(buf, buf.length);
	}

	public void write(byte[] buf, int len) throws Exception {
		// this.outputStream.write(buf, 0, len);

		// for (int i = 0; i < len; i++)
		// System.out.print((char) buf[i]);

		ByteBuffer bb = ByteBuffer.wrap(buf, 0, len);
		// System.out.println ("write: " + buf);

		while (bb.remaining() > 0) {

			try {

				this.socketChannel.write(bb);

			} catch (Exception e) {
				D.out("socket: " + socketChannel.toString() + " blocking:"
						+ socketChannel.isBlocking() + " remaining:" + bb.remaining());
				e.printStackTrace();
				throw new Exception(e);
			}

			if (bb.remaining() > 0)
				select(SelectionKey.OP_WRITE);
		}

		// if (bb.remaining() != 0)
		// throw new Exception("Incomplete write");
	}

	/*
	 * public void writeFromBuffer(ByteArrayOutputStream buffer) throws
	 * Exception { buffer.writeTo(Channels.newOutputStream(this.socketChannel)); }
	 */

	public void writeUTF8(String s) throws Exception {
		// System.out.print ("" + s + "");
		write(s.getBytes("UTF-8"));
	}

	public ReadableByteChannel asReadableByteChannel() {
		return new ReadableByteChannel() {

			public int read(ByteBuffer dst) throws IOException {
				int retval = socketChannel.read(dst);
				if (retval == 0) {
					select(SelectionKey.OP_READ);
					retval = socketChannel.read(dst);
				}
				return retval;
			}

			public void close() throws IOException {
				doClose();
			}

			public boolean isOpen() {
				return isConnected();
			}

		};
	}

	public WritableByteChannel asWriteableByteChannel() {
		return new WritableByteChannel() {

			public int write(ByteBuffer src) throws IOException {
				int retval = socketChannel.write(src);

				// this may sound stupid, but probably it works
				// because the one who tried to write() will call us again and
				// we have selected for him
				if (src.remaining() > 0) {
					select(SelectionKey.OP_WRITE);
					retval = socketChannel.write(src);
				}

				return retval;
			}

			public void close() throws IOException {
				doClose();

			}

			public boolean isOpen() {
				return isConnected();
			}

		};
	}

	public boolean isConnected() {
		// if those return false we have no chance :)
		if (socketChannel.isConnected() == false
				|| socketChannel.isOpen() == false)
			return false;

		return true;
	}

	public String getKey() {
		return m_key;
	}

	public boolean isDataAvailable() {
		return selectNow() > 0;
	}

}
