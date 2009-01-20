package de.guruz.p300.utils;

public class Integers {
	public static int getIntegerDefault (String s, int def)
	{
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return def;
		}
	}
}
