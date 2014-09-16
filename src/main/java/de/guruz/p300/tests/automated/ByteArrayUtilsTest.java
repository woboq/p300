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
package de.guruz.p300.tests.automated;

import java.util.Arrays;

import junit.framework.TestCase;
import de.guruz.p300.utils.ByteArrayUtils;

public class ByteArrayUtilsTest extends TestCase {

	public void testCutAt() {
		byte[] test = new byte[] { 1, 2, 3, 4, 5 };
		byte[] test2 = ByteArrayUtils.cutAt(null, 0);
		byte[] test3 = ByteArrayUtils.cutAt(test, -1);
		byte[] test4 = ByteArrayUtils.cutAt(test3, 0);
		byte[] test5 = ByteArrayUtils.cutAt(test4, 1);
		byte[] test6 = ByteArrayUtils.cutAt(test5, 3);
		byte[] test7 = ByteArrayUtils.cutAt(test6, 2);

		assertTrue(test2 == null);
		assertTrue(Arrays.equals(test3, test));
		assertTrue(Arrays.equals(test4, test));
		assertTrue(Arrays.equals(test5, new byte[] { 2, 3, 4, 5 }));
		assertTrue(Arrays.equals(test6, new byte[] { 5 }));
		assertTrue(Arrays.equals(test7, new byte[0]));
	}

	public void testAppend() {
		byte[] test = new byte[] { 1, 2 };
		byte[] test2 = new byte[] { 3, 4 };
		byte[] test3 = ByteArrayUtils.append(test, test2);
		byte[] test4 = ByteArrayUtils.append(null, test);
		byte[] test5 = ByteArrayUtils.append(test2, null);
		byte[] test6 = ByteArrayUtils.append(null, null);

		assertTrue(Arrays.equals(test3, new byte[] { 1, 2, 3, 4 }));
		assertTrue(Arrays.equals(test4, test));
		assertTrue(Arrays.equals(test5, test2));
		assertTrue(Arrays.equals(test6, new byte[0]));
	}

	public void testCopy() {
		byte[] test1 = new byte[] { 1, 2, 3 };
		byte[] test2 = ByteArrayUtils.copy(test1, 2);
		byte[] test3 = ByteArrayUtils.copy(test1, 5);
		byte[] test4 = ByteArrayUtils.copy(null, 2);

		assertTrue(Arrays.equals(test2, new byte[] { 1, 2 }));
		assertTrue(Arrays.equals(test3, test1));
		assertTrue(Arrays.equals(test4, new byte[0]));
	}

	public void testIndexOf() {
		byte[] test1 = new byte[] { 1, 2, 3, 50, 4, 5, 6, 78, 79 };
		int index1 = ByteArrayUtils.indexOf(test1, new byte[] { 50 });
		int index2 = ByteArrayUtils.indexOf(test1, new byte[] { 78, 79 });
		int index3 = ByteArrayUtils.indexOf(test1, new byte[] { 50, 78 });
		int index4 = ByteArrayUtils.indexOf(null, new byte[] { 0 });
		int index5 = ByteArrayUtils.indexOf(null, null);

		assertEquals(index1, 3);
		assertEquals(index2, 7);
		assertEquals(index3, -1);
		assertEquals(index4, -1);
		assertEquals(index5, -1);
	}

}
