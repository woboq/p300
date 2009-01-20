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

import java.util.BitSet;

/**
 * The normal BitSet.toString is not good for us, we do our own.
 * We could use hex, etc but we just use the characters 0 and 1
 * for simplicity and easier debugging. Who cares about hd space
 * anyway :)
 * @author guruz
 *
 */
public class BitSetUtils {
	public static BitSet setFromString (String s) throws Exception {
		int size = s.length();
		BitSet bs = new BitSet (size);
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '0')
				bs.clear(i);
			else
				bs.set(i);
		}
		
		return bs;
		
	};
	
	public static String setToString  (BitSet bs, int len) {
		StringBuilder sb = new StringBuilder ();
		
		for (int i = 0; i < len; i++) {
			if (bs.get(i) == true) 
				sb.append('1');
			else
				sb.append('0');
		}
		
		return sb.toString();
	};
	
	public static int getRandomUnsetBit (BitSet bs, int len)
	{
		if (bs.cardinality() >= len)
		{
			// failure
			return 0;
		}
		
		// try 20 times to see a random one
		for (int i = 0; i < 20; i++)
		{
			int idx = RandomGenerator.getInt(0, len);
			
			if (!bs.get(idx))
				return idx;
		}
		
		// trying randomly did not work		
		int numberOfUnsetBits = len - bs.cardinality();
		int numberOfUnsetBitToPick = RandomGenerator.getInt(0, numberOfUnsetBits);
		
		int j = numberOfUnsetBitToPick;
		for (int i = 0; i < len; i++)
		{
			if (!bs.get(i))
			{
				// unset bit
				j--;
				
				if (j <= 0)
				{
					//D.out("-- random chunk pick " + numberOfUnsetBitToPick + " -> " + i);
					return i;
				}
			}
		}
		
		
		// should not happen
		//D.out ("Picked chunk 0");
		return 0;
		
	}
	
	
}
