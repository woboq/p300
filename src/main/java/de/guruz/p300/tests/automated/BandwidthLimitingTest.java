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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import de.guruz.p300.hosts.allowing.HostAllowanceManager;

public class BandwidthLimitingTest extends TestCase {
	public void testMatching () {
		List<String> allowedKeys = new ArrayList<String> ();
		allowedKeys.add("192.168.0.1");
		allowedKeys.add("192.168");
		allowedKeys.add("10.");
		String unlimitedKeys[] = {"192.168.0.1", "10."};
		
		Assert.assertEquals("false", HostAllowanceManager.checkIPMatching("192.168.0.2", allowedKeys, unlimitedKeys), false);
		Assert.assertEquals("true", HostAllowanceManager.checkIPMatching("192.168.0.1", allowedKeys, unlimitedKeys), true);
		Assert.assertEquals("false", HostAllowanceManager.checkIPMatching("192.168.255.255", allowedKeys, unlimitedKeys), false);
		Assert.assertEquals("true", HostAllowanceManager.checkIPMatching("10.1.1.1", allowedKeys, unlimitedKeys), true);
		
	}
}
