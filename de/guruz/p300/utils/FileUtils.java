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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import de.guruz.p300.logging.D;

public class FileUtils {
	public static void gcHack() {
		// HACK! see the sun forums
		System.gc();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		System.runFinalization();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		// System.gc();
		// System.runFinalization();
		// try {
		// Thread.sleep(3000);
		// } catch (InterruptedException e) {
		// }

	}

	/**
	 * Move a file. We need this because renameTo of the Java API does not work
	 * everywhere :(
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean renameTo(File from, File to) {
		RandomAccessFile raf = null;
		FileOutputStream fos = null;
		FileInputStream fis = null;

		try {
			// D.out("moving " + from + " to " + to);

			// do it the API way

			to.delete();
			boolean ok = from.renameTo(to);

			if (ok) {
				// D.out("(normal) moved " + from + " to " + to);
				return true;
			}

			// renaming did not work. we try the gcHack and try again
			gcHack();
			ok = from.renameTo(to);

			if (ok) {
				D.out("(Windows GC hack) moved " + from + " to " + to);
				return true;
			}

			// we may not need these because we have gcHack ()

			D
					.out("Moving via file.move did not move, even not with the System.gc() hack. Copying manually now and then deleting.");

			// not renamed properly, do it manually
			raf = new RandomAccessFile(to, "rw");
			raf.setLength(from.length());
			raf.getFD().sync();
			fos = new FileOutputStream(to);

			fis = new FileInputStream(from);

			FileChannel outChannel = fos.getChannel();
			FileChannel inChannel = fis.getChannel();

			long length = from.length();
			long toWrite = length;

			while (toWrite > 0) {
				inChannel.position(length - toWrite);
				long written = inChannel.transferTo(inChannel.position(),
						toWrite, outChannel);

				if (written == -1)
					throw new Exception("Copy failure");

				toWrite -= written;
			}

			outChannel.close();
			inChannel.close();
			raf.close();

			// delete from file
			gcHack();
			from.delete();

			// we do not do deleteOnExit here because the caller may not want
			// this
			D.out("(manual) moved " + from + " to " + to);
			return true;

		} catch (Exception e) {
			D.out(e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();

				if (fis != null)
					fis.close();

				if (raf != null)
					raf.close();
			} catch (Exception e) {

			}
		}

	}
}
