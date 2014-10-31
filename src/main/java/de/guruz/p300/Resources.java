package de.guruz.p300;

import java.io.InputStream;
import java.net.URL;

public class Resources {
	public static InputStream getResourceAsStream (String name)
	{
		if (name.startsWith("/"))
			return MainDialog.class.getResourceAsStream(name);
		else
			return MainDialog.class.getResourceAsStream("/" + name);
	}

	public static URL getResource(String name) {
		URL ret = null;
		
		if (name.startsWith("/"))
			ret =  MainDialog.class.getResource(name);
		else
			ret = MainDialog.class.getResource("/" + name);
		
		if (ret == null)
		{
			if (name.startsWith("/"))
				ret =  Thread.currentThread().getContextClassLoader().getResource(name);
			else
				ret = Thread.currentThread().getContextClassLoader().getResource("/" + name);
		}
		
		return ret;
	}
}
