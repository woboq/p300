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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class MapWithSemaphores {
	int count = 5;
	
	public MapWithSemaphores (int c)
	{
		count = c;
	}
	
	long acquireCount;
	long releaseCount;
	
	private Map<String, Semaphore> map = new HashMap<String, Semaphore>();

	public boolean acquireSlot(String k) {
		synchronized (map) {
			boolean ret = false;
			if (map.containsKey(k)) {
				 ret = map.get(k).tryAcquire();
			} else {
				Semaphore s = new Semaphore(count);
				map.put(k, s);
				ret = s.tryAcquire();
			}
			
			//System.out.println (map.get(k).availablePermits());
			
			if (ret)
				acquireCount++;
			
			return ret;
		}
	}

	public void releaseSlot(String k) {
		synchronized (map) {
			Semaphore s = map.get(k);
		
			if (s != null)
			{
				releaseCount++;
				s.release();
				map.put(k, s);
				
				if (map.get(k).availablePermits() == 0)
					System.out.println ("CANNOT HAPPEN!");
			}
			else
				System.err.println ("Error: MapWithSemaphores: releaseSlot: There is no key=" + k);
			//System.out.println (map.get(k).availablePermits());
		}
	}

	public void debugOutput() {
		System.out.println ("------------------");
		System.out.println ("acquired: " + acquireCount);
		System.out.println ("released: " + releaseCount);
		
		
		for (Map.Entry<String,Semaphore> e : map.entrySet())
		{
			System.out.println (e.getKey() + " " + e.getValue().availablePermits());
		}
		System.out.println ("------------------");
		
	}
}
