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


/**
 * FIXME, may be not a good way to do it
 * @author guruz
 *
 */
public class ConnectionUtils {

//
//	public static InputStream getInputStream (final SynchronousLogicalStreamConnection s) throws Exception {
//		final PipedInputStream is = new PipedInputStream ();
//		final PipedOutputStream os = new PipedOutputStream (is);
//		
//		Runnable r = new Runnable () {
//			public void run() {
//				try {
//					byte bytes[] = s.readBytes(64);
//					while (bytes != null && bytes.length != 0) {
//						os.write(bytes);
//						bytes = s.readBytes(64);
//					}
//					os.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//					try {
//						os.close();
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//				};
//			}
//		};
//		new Thread(r).start();
//
//		return is;
//	}
}
