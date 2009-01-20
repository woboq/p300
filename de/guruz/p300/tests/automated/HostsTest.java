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
package de.guruz.p300.tests.automated;

import junit.framework.Assert;
import junit.framework.TestCase;
import de.guruz.p300.hosts.Host;

public class HostsTest extends TestCase {

	public HostsTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testURLGeneration () {
		Host h = new Host ("abc");
		h.addIPAndPort("192.168.42.10", 4337, 0);
		h.addIPAndPort("192.168.0.150", 4337, 0);
		
		Assert.assertEquals ("Location count is 2", 2, h.getLocations().length);
		
		Assert.assertEquals ("Seems online is true", true, h.seemsOnline());
		
		String url = h.toURLBestMatchingThisIP("192.168.42.11");
		Assert.assertEquals ("Best matching URL test 1.", "http://192.168.42.10:4337", url);
		
		url = h.toURLBestMatchingThisIP("192.168.0.130");
		Assert.assertEquals ("Best matching URL test 2.", "http://192.168.0.150:4337", url);
	}

}
