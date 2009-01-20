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
package de.guruz.guruzsplash.implementation;
import java.awt.SplashScreen;
import java.net.URL;

import de.guruz.guruzsplash.interfaces.GuruzsplashManager;

public class GuruzsplashManagerImplementation implements GuruzsplashManager {
	SplashScreen splashScreen = SplashScreen.getSplashScreen();
	public void hide () {
		splashScreen.close();
	}

	public boolean isVisible () {
		return (splashScreen != null && splashScreen.isVisible());
	}

	public void setImageURL(URL imageURL) throws Exception {
		splashScreen.setImageURL(imageURL);
		
	}
}
