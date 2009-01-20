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
package de.guruz.p300.requests;

import de.guruz.p300.http.HTTPVerb;

/**
 * The about page
 * 
 * @author guruz
 *
 */
public class AboutRequest extends Request {

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (!((rt == HTTPVerb.GET) || (rt == HTTPVerb.HEAD))) {
			return false;
		}

		if (!reqpath.equals("/about")) {
			return false;
		}

		return true;
	}
	
	@Override
	public void handle() throws Exception {
		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpContentType("text/html");
		this.requestThread.httpContents ();
		
		Layouter layouter = new Layouter (this.requestThread); 
		layouter.replaceBasicStuff ();
		layouter.replaceTitle ("about p300");
		
		this.requestThread.write(layouter.getBeforeMainDiv());
		
		this.requestThread.write ("<h1>p300 file sharing program</h1>");
		
		this.requestThread.write ("<h3>Website</h3>");
		this.requestThread.write ("<p>Our website is on <a href='http://p300.eu/'>p300.eu</a></p>");
		
		this.requestThread.write ("<h3>Copyright</h3>");
		this.requestThread.write ("<p>p300 is &copy; 2006-2008 by <a href='http://guruz.de'>Markus Goetz</a>.</p>");
		this.requestThread.write ("<p>Some contributions are &copy; 2006, 2007, 2008 by <a href='http://tomcat.ranta.info/'>Sebastian Breier</a>.</p>");
		this.requestThread.write ("<p>Splash screen &copy; until June, 2008 by Sylvia Riester. Thanks a lot :)</p>");
		this.requestThread.write ("<p>Splash screen, p300 icon &copy; since June, 2008 by <a href='http://tonice.de'>ToniCE</a>. Font by <a href='http://nils-von-blanc.de/?page_id=23'>Nils von Blanc</a>. Thanks a lot :)</p>");
		this.requestThread.write ("<p>Chat message sound effect &copy; 2009 by <a href='http://falsemirror.de/'>False Mirror</a>.  Thanks a lot :)</p>");
		this.requestThread.write ("<p>Some icons are &copy; <a href='http://tango.freedesktop.org/Tango_Desktop_Project'>Tango Desktop Project</a> (License: <a href='http://creativecommons.org/licenses/by-sa/2.5/'>Creative Commons Attribution Share-Alike license</a>).</p>");
		this.requestThread.write ("<p>Some icons are &copy; <a href='http://www.gnome.org/'>GNOME</a> (License: <a href='http://www.gnu.org/licenses/gpl.txt'>GNU General Public License</a>).</p>");
		
		this.requestThread.write ("<h3>License</h3>");
		//this.requestThread.write ("<p>p300 is free to use for everyone.</p>");
		this.requestThread.write ("<p>p300 is released under the <a href='http://www.gnu.org/licenses/gpl.txt'>GNU General Public License</a>.<br>Relicensing under another license might be possible, ask us for information.</p>");
		
		this.requestThread.write ("<h3>Sponsorship</h3>");
		this.requestThread.write ("<p>Want to sponsor p300 and have your link on the p300 website or in p300 itself? Contact us!</p>");
		
		this.requestThread.write(layouter.getAfterMainDiv());

		

		
		this.requestThread.flush();
		this.requestThread.close();

	}

}
