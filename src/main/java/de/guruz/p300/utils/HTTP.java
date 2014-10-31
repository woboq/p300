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

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HTTP {

	public static HTTPRange parseRangeHeader(String header, long fileSize) {
		long rangeBeginLong = 0;
		long rangeEndLong = fileSize - 1;
		long rangeLength = (rangeEndLong - rangeBeginLong) + 1;
		
		if (header == null) {
			return new HTTPRange (rangeBeginLong, rangeEndLong, rangeLength);
		}

		if (header.length() == 0) {
			return new HTTPRange (rangeBeginLong, rangeEndLong, rangeLength);
		};

		
		String rangeHeader = header;


		try {
			//System.out.println("1 Range header is =" + rangeHeader);
			rangeHeader = rangeHeader.substring(rangeHeader.indexOf('=') + 1);
			//System.out.println("2 Range header is now =" + rangeHeader);
			String rangeBegin = rangeHeader.substring(0, rangeHeader
					.indexOf('-'));
			//System.out.println("3 Range begin is =" + rangeBegin);
			String rangeEnd = rangeHeader
					.substring(rangeHeader.indexOf('-') + 1);
			//System.out.println("4 Range end is =" + rangeEnd);

			if (rangeBegin.length() > 0) {
				rangeBeginLong = Long.parseLong(rangeBegin);
			}

			if (rangeEnd.length() > 0) {
				rangeEndLong = Long.parseLong(rangeEnd);
			}

			rangeLength = (rangeEndLong - rangeBeginLong) + 1; // BLA

			// swap if other ranges
			if (rangeBeginLong > rangeEndLong) {
				long t = rangeEndLong;
				rangeEndLong = rangeBeginLong;
				rangeBeginLong = t;
				
			}
			
			long from = rangeBeginLong;
			long to = rangeEndLong;
			long len = rangeLength;

			return new HTTPRange (from, to, len);
			
		} catch (Exception e) {
			e.printStackTrace();
			rangeBeginLong = 0;
			rangeEndLong = fileSize - 1;
			rangeLength = (rangeEndLong - rangeBeginLong) + 1;

			return new HTTPRange (rangeBeginLong, rangeEndLong, rangeLength);
		}
		

	}
	
	public static SimpleDateFormat httpDateFormat;
	
	public static String getHTTPDate () {
		return HTTP.getHTTPDate (System.currentTimeMillis());
	}
	
	public synchronized static String getHTTPDate (long epoch) {
		// "Sun, 06 Nov 1994 08:49:37 GMT"
		if (HTTP.httpDateFormat == null) {
			HTTP.httpDateFormat = new SimpleDateFormat   ("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			HTTP.httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		}
			
		Date d = new Date (epoch);
		
		String s = HTTP.httpDateFormat.format(d);
		
		return s;
	}
	
	public static String extractParameter(byte formData[], String param) {
		try {
			String formDataString = new String (formData, "UTF-8");
			String val = HTTP.extractParameter(formDataString, param);
			
			return val;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String extractParameter (String s, String param) {
		try {
			if (! (s.contains(param + '=')))
				return null;

			int paramStartIndex = s.indexOf(param + '=') + 1 + param.length();
			int paramEndIndex = s.indexOf("&", paramStartIndex);

			if (paramEndIndex == -1) {
				paramEndIndex = s.length();
			}

			String value = s.substring(paramStartIndex, paramEndIndex);
			value = URLDecoder.decode(value, "UTF-8");

			return value;

		} catch (Exception e) {
			return null;
		}
	}
}
