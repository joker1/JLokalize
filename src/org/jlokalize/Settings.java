package org.jlokalize;

public class Settings {
	
	public static final String ENCODING = System.getProperty("jlokalize.encoding", "UTF-8");
	
	public static final boolean ESCAPE = Boolean.parseBoolean(System.getProperty("jlokalize.escape", "false"));
}
