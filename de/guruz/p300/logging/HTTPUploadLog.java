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

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.guruz.p300.Configuration;
import de.guruz.p300.utils.FileNameUtils;

public class HTTPUploadLog extends HTTPLog {
	protected static Logger logger = null;
	
	public static void addLogHandler (Handler lh) {
		lh.setFormatter(formatter);
		logger.addHandler (lh);
	}
	
	static{
		logger = Logger.getLogger("de.guruz.p300.HTTPUploadLog");
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		

		try {
			String fn = FileNameUtils.replaceVariables (Configuration.instance().getHTTPuploadLog());
			FileHandler fh = new FileHandler (fn, FIVE_MB, 5, true);
			addLogHandler (fh);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	};
	
	/**
	 * @param ip
	 * @param reqline
	 * @param code
	 * @param sentSize
	 * @author guruz
	 * @see #HTTPgenericLog(String, String, String, int, long)
	 * @see HTTPAccessLog#HTTPaccessLog(String, String, int, long)
	 */
	public static void out (String ip, String reqline, int code, long sentSize) {
		doHTTPLog (logger, ip, reqline, code, sentSize);	
	}

}
