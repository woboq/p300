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

/**
 * Utilities for working on byte arrays
 * Generally, these utilities won't change the original given arrays.
 * @author tomcat
 *
 */
public class ByteArrayUtils {

	/**
	 * Cut the arrays from position pos.
	 * Won't change the original array.
	 * @param b
	 * @param pos
	 * @return Copy of the array, only between pos and end
	 */
	public static byte[] cutAt(byte[] b, int pos) {
		if (b == null || pos <= 0) {
			return b;
		}
		if (pos >= b.length) {
			return new byte[0];
		}
		byte[] rest = new byte[b.length - pos];
		for (int i = 0; i < b.length - pos; i++) {
			rest[i] = b[i + pos];
		}
		
		return rest;
	}
	
	/**
	 * Add two arrays.
	 * Won't change the original array.
	 * @param b
	 * @param b2
	 * @return A new array containing both arrays
	 */
	public static byte[] append(byte[] b, byte[] b2) {
		int bLength = 0;
		int b2Length = 0;
		if (b != null) {
			bLength = b.length;
		}
		if (b2 != null) {
			b2Length = b2.length;
		}
		
		byte[] result = new byte[bLength + b2Length];
		for (int i = 0; i < bLength; i++) {
			result[i] = b[i];
		}
		for (int i = bLength; i < bLength + b2Length; i++) {
			result[i] = b2[i - bLength];
		}
		
		return result;
	}
	
	/**
	 * Copy first "count" bytes from byte array.
	 * Won't change the original array.
	 * @param b
	 * @param count
	 * @return Copy of the first "count" bytes
	 */
	public static byte[] copy(byte[] b, int count) {
		int copy = 0;
		if (b != null) {
			copy = Math.min(b.length, count);
		}
		byte[] result = new byte[copy];
		for (int i = 0; i < copy; i++) {
			result[i] = b[i];
		}
		
		return result;
	}
	
	/**
	 * Find the first occurrence of sub in b
	 * Work in progress
	 * @param b
	 * @param sub
	 * @return Position of the first match or -1 if not found
	 */
	public static int indexOf(byte[] b, byte[] sub) {
		if (b == null || sub == null) {
			return -1;
		}
		
		for (int i = 0; i < b.length - sub.length + 1; i++) {
			boolean matches = true;
			for (int j = 0; j < sub.length; j++) {
				if (b[i+j] != sub[j]) {
					matches = false;
				}
			}
			if (matches) {
				return i;
			}
		}
		
		return -1;
	}
	
}
