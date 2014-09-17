package de.guruz.p300.tests.automated;

import junit.framework.TestCase;
import de.guruz.p300.utils.FileNameUtils;

public class FileNameUtilsTest extends TestCase {
	public void testSanitize ()
	{

		
		assertEquals ("bla/ble_ble", FileNameUtils.sanitizeFilepathForLocalOS("bla/ble\nble"));
		assertEquals ("_", FileNameUtils.sanitizeFilepathForLocalOS("\0"));
	}
}
