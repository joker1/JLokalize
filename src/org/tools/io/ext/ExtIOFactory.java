package org.tools.io.ext;

import java.io.Writer;

import org.apache.commons.configuration.PropertiesConfiguration.DefaultIOFactory;
import org.apache.commons.configuration.PropertiesConfiguration.PropertiesWriter;
import org.jlokalize.Settings;

public class ExtIOFactory extends DefaultIOFactory {
	
	private final boolean escapeUnicode;
	
	public ExtIOFactory() {
		this(Settings.ESCAPE);
	}
	
	public ExtIOFactory(boolean escapeUnicode) {
		super();
		this.escapeUnicode = escapeUnicode;
	}
	
	@Override
	public PropertiesWriter createPropertiesWriter(Writer out, char delimiter) {
		return new ExtPropertyWriter(out, delimiter, escapeUnicode);
	}
}
