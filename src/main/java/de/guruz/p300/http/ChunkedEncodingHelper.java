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
package de.guruz.p300.http;

import java.io.UnsupportedEncodingException;

import de.guruz.p300.Configuration;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.utils.SoftLimitedByteBuffer;

/**
 * This class takes a variable amount of byte arrays and writes
 * them out in the chunked encoding when requested.
 * @author guruz
 *
 */
public class ChunkedEncodingHelper {
	// FIXME: optimize all the constant string/byte stuff
	
	protected SynchronousLogicalStreamConnection connection = null;
	protected SoftLimitedByteBuffer buffer = new SoftLimitedByteBuffer ();
	
	public void append (String s) throws Exception {
		try {
			this.append(s.getBytes(Configuration.getDefaultEncoding()));
		} catch (UnsupportedEncodingException e) {
			this.append(s.getBytes());
		}
	}
	
	// possible optimization: if b is already larger than the buffer then flush
	// and after that send b immediatly
	public void append (byte b[]) throws Exception {
		// the thing to append is already large. flush 
		if ((b.length >= this.buffer.getLimit ()) && (this.buffer.getCount() > 0)) {
			this.flush ();
			this.buffer.append(b);
			this.flush ();
			return;
		}
		
		// default case: just append
		this.buffer.append(b);
	}
	
	public void append (byte b[], int c) throws Exception {
		if (b.length == c) {
			this.append (b);
		} else {
			//append (java.util.Arrays.);
			byte newb[] = new byte[c];
			System.arraycopy(b, 0, newb, 0, c);
			this.append (newb);
		}
	}
	
	public boolean needsFlush () {
		return this.buffer.isFull();
	}
	
	public void flush () throws Exception {
		// just do nothing when there is nothing to flush
		if (this.buffer.getCount() == 0) {
			return;
		}
		
		// write the length of the buffer
		int len = this.buffer.getCount();
		byte[] lenAsAscii = Integer.toHexString(len).getBytes ("ASCII");
		this.connection.write(lenAsAscii);
		this.totalSent += lenAsAscii.length;
		
		// and an \r\n
		byte[] newline = new byte[] {13, 10};
		//byte[] newline = new byte[] {10};
		this.connection.write (newline);
		this.totalSent += 2;
		
		// write the buffer contents
		byte[][] bytes = this.buffer.getBytes();
		for (byte[] element : bytes) {
			this.connection.write(element);
			this.totalSent += element.length;
		}
		
		// reset buffer
		this.buffer.reset();
		
		this.connection.write (newline);
		this.totalSent += 2;
		
		//System.out.println (connection.toString() + " - flushed 1 chunk with " +  len + " bytes (softlimit=" + buffer.getLimit() + ")");
	}
	

	public void finish () throws Exception {
		this.flush ();
		
		String lastChunk = (new String ("0\r\n\r\n"));
		
		this.connection.writeUTF8(lastChunk);
		this.totalSent += lastChunk.length();
		
		lastChunk = null;
		
		//System.out.println (connection.toString() + " - end chunking");
	}

	public ChunkedEncodingHelper(SynchronousLogicalStreamConnection c) {
		super();
		this.connection = c;
		this.buffer.setLimit(1024); // arbitarily chosen
	}

	
	long totalSent = 0;
	public long getTotalSent() {
		return this.totalSent;
	}
}
