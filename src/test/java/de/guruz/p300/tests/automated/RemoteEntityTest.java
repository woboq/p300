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
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;

public class RemoteEntityTest extends TestCase {
	public void testPath () {
		RemoteEntity e1 = new RemoteFile ("/home/test/testfile");
		
		Assert.assertEquals ("Pathname", "/home/test/testfile", e1.getPath());
		Assert.assertEquals ("Filename of Pathname", "testfile", e1.getName());
		Assert.assertEquals ("Parent Pathname", "/home/test", e1.getParent().getPath());
		
		RemoteEntity e2 = new RemoteFile ("/home\\test\\testfile2");
		Assert.assertEquals ("Backslash replacement", "/home/test/testfile2", e2.getPath());
		
		RemoteEntity e3 = new RemoteFile (new RemoteDir("/mount/"), e2.getPath());
		Assert.assertEquals ("Concatenation", "/mount/home/test/testfile2", e3.getPath());
		
		RemoteEntity e4 = new RemoteDir ("/");
		Assert.assertEquals ("Is root true", true, e4.isRoot());
		Assert.assertEquals ("Is root false", false, e1.isRoot());
		Assert.assertEquals ("Is root false", false, e3.isRoot());
		Assert.assertEquals ("Root name", "/", e4.getName());
		
		RemoteEntity e5 = new RemoteDir ("/test/");
		Assert.assertEquals ("test name", "test", e5.getName());
		Assert.assertEquals ("test toString", "test/", e5.toString());
	}
	
}
