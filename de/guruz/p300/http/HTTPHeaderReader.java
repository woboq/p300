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

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;

/**
 * This helper class can read/parse/store HTTP headers for us
 * 
 * @author guruz
 *
 */
public class HTTPHeaderReader {
	public static HTTPHeaders readHeaders (SynchronousLogicalStreamConnection con) throws Exception
	{
		HTTPHeaderReader reader = new HTTPHeaderReader ();
		reader.read(con);
		return reader.getHeaders();
	}
	
	/**
	 * a hashmap with all the HTTP headers received
	 */
	HTTPHeaders m_headers = new HTTPHeaders ();
	
	public void read (SynchronousLogicalStreamConnection con) throws Exception {
		String line = con.readLine();
		
		if (line == null) {
			// end of stream after the request line.. close
			throw new Exception("End of stream after request line");
		}
		
		
		while (line.length() > 0) {
			this.saveHeader(line);
			line = con.readLine();
			if (line == null) {
				// connection closed while reading the headers
				throw new Exception("Closed prematurely when reading headers");
			}
		}
	}
	
	private String lastHeaderName = "";

	private void saveHeader(String line) throws Exception {
		// this stuff does not work anymore.. FIXME
		// is continuated
		if (line.startsWith(" ") && (this.lastHeaderName.length() > 0)) {
			String lastHeaderValue = this.getHeader(this.lastHeaderName, "X-Whatever");
			m_headers.setHeader(this.lastHeaderName, lastHeaderValue + line);
			return;
		}

		String contents[] = line.split(":", 2);

		// check if the resulting array has at least 2 elements, the header name
		// and its value
		if (contents.length < 2) {
			throw new Exception("Cannot parse header");
		}

		String name = contents[0].trim().toLowerCase();
		String value = contents[1].trim();
		this.m_headers.setHeader(name, value);

		this.lastHeaderName = name;

		// System.out.println("<" + name + "> = <" + value + ">");
	}
	
	/**
	 * gets the contents of a http client header
	 * 
	 * @param name
	 * @param def
	 * @return
	 */
	public String getHeader(String name, String def) {
		String key = name.toLowerCase();
		String value = m_headers.getHeader(key);

		if (value != null)
			return value;
		else
			return def;

	}

	public void clear() {
		m_headers.clearHeaders();
		
	}
	
	public HTTPHeaders getHeaders ()
	{
		return m_headers;
	}
}
