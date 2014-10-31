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

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Classes implementing this interface can be used like a stream socket
 * @author guruz
 *
 */
public interface SynchronousLogicalStreamConnection {
	/**
	 * close the stream
	 *
	 */
	public void close() ;
	
	/**
	 * flush the stream
	 *
	 */
	public void flush()  throws Exception;
	
	/**
	 * read a specific amount of bytes from the stream
	 * @param count
	 * @return
	 */
	public byte[] readBytes(int count)  throws Exception;
	
	//public int readBytes(byte[] buf, int count)  throws Exception;
	
	public ReadableByteChannel asReadableByteChannel ();
	
	/**
	 * read a line.. currently only specified to read 1024 bytes
	 * @return
	 */
	public String readLine()  throws Exception;
	
	/**
	 * write the complete array to the stream
	 * @param buf
	 * @throws Exception
	 */
	public void write(byte buf[]) throws Exception;
	
	/**
	 * write len bytes of bug to the stream
	 * @param buf
	 * @param len
	 * @throws Exception
	 */
	public void write(byte buf[], int len) throws Exception;
	
	// not needed
	//public void writeFromBuffer (ByteArrayOutputStream buffer) throws Exception;
	
	public void writeUTF8 (String s) throws Exception;
	
	public WritableByteChannel asWriteableByteChannel ();
	
	
	public void setTimeout (long msecs);
	
	public long getTimeout ();

	public boolean isConnected();
	
	public boolean isDataAvailable ();

	public String getKey();

	public int selectNow();

}
