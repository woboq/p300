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

import java.io.ByteArrayOutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import de.guruz.p300.utils.ByteArrayUtils;

/**
 * A local (loop) connection not needing any networking Used for testing
 * purposes
 *
 * @author tomcat
 *
 */
public class LocalConnection implements SynchronousLogicalStreamConnection {

	private byte[] buffer;
	private static final byte[] lineEnd = { 10 };

	public void close() {
	}

	public void flush() throws Exception {
		throw new Exception("Not implemented");
	}

	public byte[] readBytes(int count) throws Exception {
		byte[] result = ByteArrayUtils.copy(buffer, count);
		buffer = ByteArrayUtils.cutAt(buffer, count);
		return result;
	}

	public String readLine() throws Exception {
		int indexLineEnd = ByteArrayUtils.indexOf(buffer, lineEnd);
		if (indexLineEnd == -1) {
			return "";
		} else {
			return readString(indexLineEnd + lineEnd.length);
		}
	}

	/**
	 * Read count bytes as a String
	 * @param count
	 * @return
	 */
	private String readString(int count) {
		try {
			byte[] read = readBytes(count);
			return new String(read);
		} catch (Exception e) {
			return "";
		}
	}

	public void setTimeout(int msecs) {
	}

	public void write(byte[] buf) throws Exception {
		buffer = ByteArrayUtils.append(buffer, buf);
	}

	public void write(byte[] buf, int len) throws Exception {
		byte[] write = ByteArrayUtils.copy(buf, len);
		write(write);
	}

	public void writeFromBuffer(ByteArrayOutputStream buffer) throws Exception {
		byte[] write = buffer.toByteArray();
		write(write);
	}

	public void writeUTF8(String s) throws Exception {
		write (s.getBytes("UTF-8"));
	}

	public int readBytes(byte[] buf, int count) throws Exception {
		throw new UnsupportedOperationException();
	}

	public ReadableByteChannel asReadableByteChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	public WritableByteChannel asWriteableByteChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDataAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	public int selectNow() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setTimeout(long msecs) {
		// TODO Auto-generated method stub

	}

}