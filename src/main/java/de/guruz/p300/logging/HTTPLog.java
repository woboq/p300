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
package de.guruz.p300.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPLog {
	final static int FIVE_MB = 1024*1024*5;
	
	protected static Formatter formatter = new FormatterWithNewline ();
	

	
	// http://httpd.apache.org/docs/2.2/logs.html#accesslog
	//  127.0.0.1 - - [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
	/**
	 * @param fn
	 * @param ip
	 * @param reqline
	 * @param code
	 * @param sentSize
	 * @return
	 * @author guruz
	 * @see #HTTPaccessLog(String, String, int, long)
	 * @see #HTTPuploadLog(String, String, int, long)
	 */
	public static String doHTTPLog (Logger logger, String ip, String reqline, int code, long sentSize) {
		String logLine = "";
		
		String date = LogUtils.getLogDate ();
		logLine = ip + " - - [" + date + "] \"" + reqline + "\" " + code + ' ' + sentSize;
						
		logger.log(Level.ALL, logLine);

		return logLine;		
	}
	
	
}
