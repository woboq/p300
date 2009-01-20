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
package de.guruz.p300.utils;

import java.util.Vector;

/**
 * A buffer class that holds bytes. We can add byte arrays.
 * As soon as the content byte count is larger than the limit,
 * isFull returns true.
 * This class only does a soft limit, you can still append more :)
 * @author guruz
 *
 */
public class SoftLimitedByteBuffer {
	protected Vector<byte[]> bytes = new Vector<byte[]>();
	
	protected int limit = 0;
	
	public void setLimit (int c) {		
		this.limit = c;
	}
	
	public void reset () {
		this.bytes.clear();
		this.count = 0;
	}
	
	protected int count = 0;
	
	public void append (byte b[]) {
		this.bytes.add(b);
		this.count += b.length;
	}
	
	
	
	public boolean isFull () {
		return this.count >= this.limit;
	}
	
	public byte[][] getBytes () {
		int size = this.bytes.size();
		byte[][] ret = new byte[size][];
		for (int i = 0; i < size; i++) {
			ret[i] = this.bytes.get(i);
		}
		
		return ret;	
	}
	
	public int getCount () {
		return this.count;
	}
	
	public int getLimit () {
		return this.limit;
	}
}
