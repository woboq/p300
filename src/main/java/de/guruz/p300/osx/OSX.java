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
package de.guruz.p300.osx;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

public class OSX implements OSXInterface, ApplicationListener{
	protected OSXCallbackInterface callback = null;
	
	public void handleAbout(ApplicationEvent arg0) {
		//System.out.println ("handleAbout");
		callback.OSXabout();
		arg0.setHandled(true);
	}

	public void handleOpenApplication(ApplicationEvent arg0) {

	}

	public void handleOpenFile(ApplicationEvent arg0) {
	
	}

	public void handlePreferences(ApplicationEvent arg0) {
		
	}

	public void handlePrintFile(ApplicationEvent arg0) {
	}

	public void handleQuit(ApplicationEvent arg0) {
		System.exit(0);
	}

	public void handleReOpenApplication(ApplicationEvent arg0) {
		callback.OSXreOpenApplication();
	}

	public OSXCallbackInterface getCallback() {
		return callback;
	}

	public void setCallback(OSXCallbackInterface callback) {
		this.callback = callback;
		
		Application app = Application.getApplication();
		app.addApplicationListener(this);
		app.setEnabledPreferencesMenu(false);
		app.addAboutMenuItem();
		app.setEnabledAboutMenu(true);
	}


}
