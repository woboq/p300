package de.guruz.p300.tests.automated;

import junit.framework.TestCase;
import de.guruz.p300.utils.IP;

public class IPUtilTest extends TestCase {
	public void testMatchesIPorHostnamePort ()
	{
		assertTrue (IP.matchesIPorHostnamePort("192.168.0.150:4337"));
		assertTrue (IP.matchesIPorHostnamePort("bla:1234"));
		assertTrue (IP.matchesIPorHostnamePort("blubb-bla.de:4337"));
		assertTrue (IP.matchesIPorHostnamePort("1.2.3.4:4337"));
		
		assertFalse (IP.matchesIPorHostnamePort("1.2:3.4:4337"));
		assertFalse (IP.matchesIPorHostnamePort("1.2.3.4:abc"));
		assertFalse (IP.matchesIPorHostnamePort("fdsddfadf"));
		assertFalse (IP.matchesIPorHostnamePort("%%%%"));
		assertFalse (IP.matchesIPorHostnamePort("1234"));
	}
}
