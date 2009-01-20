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

/**
 * Represents the first line of a HTTP reply, e.g. "HTTP/1.0 200 bla blabla"
 * @author guruz
 *
 */
public class HTTPReplyLine {
	protected HTTPVersion version = null;
	protected int nr = -1;
	protected String msg = null;
	protected String m_line = null;
	
	public HTTPReplyLine(String line) throws Exception {
		//System.out.println ("<<" + line + ">>");
		String[] parts = line.split(" ", 3);
		if (parts.length < 3) {
			throw new Exception ("Could not split answer line");
		}
		
		this.version = HTTPVersion.convert(parts[0]);
		this.nr = Integer.parseInt(parts[1]);
		this.msg = parts[2];
		m_line = line;
	}

	public String getMsg() {
		return this.msg;
	}

	public int getNr() {
		return this.nr;
	}

	public HTTPVersion getVersion() {
		return this.version;
	}
	
	public boolean isOK ()
	{
		return this.nr >= 200 && this.nr <= 299;
	}
	
	public String toString ()
	{
		return m_line;
	}

}
