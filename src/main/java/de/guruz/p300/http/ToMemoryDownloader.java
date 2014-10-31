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
package de.guruz.p300.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.guruz.p300.logging.D;

/**
 * This class downloads data from a URL to memory into a byte array
 * @author guruz
 *
 */
public class ToMemoryDownloader {
	protected String urlString;
	
	protected byte result[] = null;
	
	public boolean download (String u){
		this.urlString = u;
		
		try {
			URL url = new URL (u);
			URLConnection c = url.openConnection();
			
			long len = c.getContentLength();
			
			ByteArrayOutputStream baos;
			
			if (len == -1) {
				baos = new ByteArrayOutputStream (2048);
			} else {
				baos = new ByteArrayOutputStream ((int) len);
			}
			
			InputStream is = c.getInputStream();
			byte[] data = new byte[1024];
			int haveRead = is.read(data);
			while (haveRead > 0) {
				baos.write(data, 0, haveRead);
				haveRead = is.read(data);
			}
			
			this.result = baos.toByteArray();
			
			return true;
			
		} catch (Exception e) {
			//e.printStackTrace();
			D.out (e.toString());
			return false;
		}
		
	}
	
	/**
	 * Try 2 URLs for downloading. This could be done with an array
	 * @param u1
	 * @param u2
	 * @return
	 */
	public boolean downloadWithAlternateURL (String u1, String u2) {
		boolean ok = this.download (u1);
		
		if (!ok) {
			ok = this.download (u2);
		}
		
		return ok;
	}
	
	public byte[] results () {
		return this.result;
		
	}
}
