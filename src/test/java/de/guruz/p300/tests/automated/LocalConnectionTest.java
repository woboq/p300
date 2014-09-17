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

import de.guruz.p300.connections.LocalConnection;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

public class LocalConnectionTest {

    @Test
    public void testRegular() {
        LocalConnection conn = new LocalConnection();

        byte[] b1 = new byte[]{1};
        byte[] b2 = new byte[]{2};
        byte[] b3 = new byte[0];
        byte[] b4 = new byte[]{3};

        try {
            conn.write(b1);
            conn.write(null);
            conn.write(b2);
            conn.write(b3);
            conn.write(b4);

            byte[] b5 = conn.readBytes(1);
            byte[] b6 = conn.readBytes(3);
            byte[] b7 = conn.readBytes(5);

            assertTrue(Arrays.equals(b5, b1));
            assertTrue(Arrays.equals(b6, new byte[]{2, 3}));
            assertTrue(Arrays.equals(b7, new byte[0]));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void testLen() {
        LocalConnection conn = new LocalConnection();

        byte[] b1 = new byte[]{1, 2, 3, 4, 5};
        try {
            conn.write(b1, 3);
            byte[] b2 = conn.readBytes(3);
            assertTrue(Arrays.equals(b2, new byte[]{1, 2, 3}));

            conn.write(b1, 6);
            byte[] b3 = conn.readBytes(6);
            assertTrue(Arrays.equals(b3, b1));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void testBuffer() {
        LocalConnection conn = new LocalConnection();

        byte[] test = new byte[]{1, 2, 3, 4, 5};
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            stream.write(test);
            conn.writeFromBuffer(stream);
            byte[] read = conn.readBytes(5);
            assertTrue(Arrays.equals(read, test));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void testReadLine() {
        LocalConnection conn = new LocalConnection();

        byte[] test1 = new byte[]{65, 66, 13, 10, 66, 65, 10, 65, 66};
        try {
            conn.write(test1);
            String s1 = conn.readLine();
            String s2 = conn.readLine();
            String s3 = conn.readLine();
            byte[] test2 = conn.readBytes(3);

            assertEquals(s1, "AB\r\n");
            assertEquals(s2, "BA\n");
            assertEquals(s3, "");
            assertTrue(Arrays.equals(test2, new byte[]{65, 66}));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Ignore(value = "to be reviewed as LocalConnection.close() does not throw an Exception")
    @Test
    public void testUnimplemented() {
        LocalConnection conn = new LocalConnection();

        try {
            conn.close();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
        try {
            conn.flush();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
