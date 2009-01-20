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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Creates our lockfile in .p300
 * @author guruz
 *
 */
public class LockFile {
	public LockFile(String fn) {
		this.filename = fn;
	}

	String filename = null;

	FileLock lock = null;
	
	File file = null;

	FileChannel channel = null;

	public boolean lock() {
		try {
			// Get a file channel for the file
			this.file = new File(this.filename);
			this.file.deleteOnExit();
			this.channel = new RandomAccessFile(this.file, "rw").getChannel();
			
			// Try acquiring the lock without blocking. This method returns
			// null or throws an exception if the file is already locked.
			try {
				this.lock = this.channel.tryLock();
			} catch (OverlappingFileLockException e) {
				// File is already locked in this thread or virtual machine
				return false;
			}

			return true;

		} catch (Exception e) {
			return false;
		}
	}

	public void unlock() {

		if (this.lock != null) {
			try {
				// Release the lock
				this.lock.release();
				

				// Close the file
				this.channel.close();
			} catch (Exception e) 
			{}
		}
		
		try {
		if (this.file != null) {
			this.file.delete();
		}
		} catch (Exception e)
		{}
		
		
		try 
		{}
		finally {
			this.file = null;
			this.lock = null;
		}
	}
	
	@Override
	public void finalize () {
		this.unlock ();
	}
}
