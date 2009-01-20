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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;

import de.guruz.p300.Configuration;

/**
 * URL utility functions
 * @author guruz
 *
 */
public class URL {
	public static String extractParameter(String url, String param) {
		String s = url.substring(url.indexOf('?'));
		return HTTP.extractParameter(s, param);
	}


	public static String encode(String s) {
		String ret = null;
		try {
			ret = URLEncoder.encode(s, Configuration.getDefaultEncoding());
		} catch (UnsupportedEncodingException e) {
			try {
				ret = URLEncoder.encode(s, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				ret = "";
			}
			
		}
		
		ret = ret.replace("+","%20");
		//System.out.println ("Encoded " + ret);
		
		return ret;
	}
	
	public static String makeURLHexTriplet (int b) {
		byte bb = (byte) b;
		
		return '%' + Integer.toHexString((bb & 0xF0) >> 8) + Integer.toHexString((bb & 0x0F));

	}

	public static String decode(String reqpath) {
		return URL.decode(reqpath, Configuration.getDefaultEncoding());
	}
	
	public static String decode(String reqpath, String enc) {
		try {
			return URLDecoder.decode(reqpath, enc);
		} catch (UnsupportedEncodingException e) {
			try {
				return URLDecoder.decode(reqpath, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				return "";
			}
		}
	}
	
	public static void charsetTest () {
		SortedMap<String, Charset> cm = Charset.availableCharsets();
		Set<String> cks = cm.keySet();
		Iterator<String> cki = cks.iterator();
		while (cki.hasNext()) {
			String ck = cki.next();
			
			String fn = "/Users/guruz/p300_test/Bo%A8sedeath/";
			boolean exists = (new File (de.guruz.p300.utils.URL.decode (fn, ck))).exists ();
			
			if (exists) {
				System.out.println (fn.concat(" is ").concat(ck));
			}
		}
		cki = cks.iterator();
		while (cki.hasNext()) {
			String ck = cki.next();
			
			String fn = "/Users/guruz/p300_test/Bo%CC%88sedeath/";
			boolean exists = false;
			try {
			exists = (new File (de.guruz.p300.utils.URL.decode (fn, ck))).exists ();
			} catch (Exception e) {

			}
			
			if (exists) {
				System.out.println (fn.concat(" is ").concat(ck));
			}
		}
	}


	public static String getOnlyPath(String currentPath) {
		int num = 0;
		for (int i = 0; i < currentPath.length(); i++)
		{
			if (currentPath.charAt(i) == '/')
				num++;
			
			if (num == 3)
				return currentPath.substring(i);
		}
		
		// default case, no third / found
		return "/";
	}
}
