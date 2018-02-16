package org.tools.io.ext;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration.PropertiesConfiguration.PropertiesWriter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jlokalize.Settings;

public class ExtPropertyWriter extends PropertiesWriter {
	
	/* Copied from PropertiesConfiguration */
	public static final String ESCAPE = "\\";
	private static final String DOUBLE_ESC = ESCAPE + ESCAPE;
	private static final char[] SEPARATORS = new char[]{'=', ':'};
	private static final char[] WHITE_SPACE = new char[]{' ', '\t', '\f'};
	
	/* Copied from super class */
	private static final int BUF_SIZE = 8;
	
	private final char delimiter;
	
	private final boolean escapeUnicode;
	
	public ExtPropertyWriter(Writer writer, char delimiter) {
		this(writer, delimiter, Settings.ESCAPE);
	}
	
	public ExtPropertyWriter(Writer writer, char delimiter, boolean escapeUnicode) {
		super(writer, delimiter);
		this.delimiter = delimiter;
		this.escapeUnicode = escapeUnicode;
	}
	
	/* Copied from PropertiesConfiguration */
	private static int countTrailingBS(String line) {
		int bsCount = 0;
		for (int idx = line.length() - 1; idx >= 0 && line.charAt(idx) == '\\'; idx--) {
			bsCount++;
		}
		
		return bsCount;
	}
	
	/* Copied from super class */
	@Override
	public void writeProperty(String key, Object value, boolean forceSingleLine) throws IOException {
		String v;
		
		if (value instanceof List) {
			List<?> values = (List<?>) value;
			if (forceSingleLine) {
				v = makeSingleLineValue(values);
			} else {
				writeProperty(key, values);
				return;
			}
		} else {
			v = escapeValue(value, false);
		}
		
		write(escapeKey(key));
		write(fetchSeparator(key, value));
		write(v);
		
		writeln(null);
	}
	
	/* Copied from super class */
	protected String escapeKey(String key) {
		StringBuilder newkey = new StringBuilder();
		
		for (int i = 0; i < key.length(); i++) {
			char c = key.charAt(i);
			
			if (ArrayUtils.contains(SEPARATORS, c) || ArrayUtils.contains(WHITE_SPACE, c)) {
				// escape the separator
				newkey.append('\\');
				newkey.append(c);
			} else {
				newkey.append(c);
			}
		}
		
		return newkey.toString();
	}
	
	/* Copied from super class */
	protected String escapeValue(Object value, boolean inList) {
		String escapedValue = handleBackslashs(value, inList);
		if (delimiter != 0) {
			escapedValue = StringUtils.replace(escapedValue, String.valueOf(delimiter), ESCAPE + delimiter);
		}
		return escapedValue;
	}
	
	/* Copied from super class */
	protected String handleBackslashs(Object value, boolean inList) {
		String strValue = String.valueOf(value);
		
		if (inList && strValue.indexOf(DOUBLE_ESC) >= 0) {
			char esc = ESCAPE.charAt(0);
			StringBuilder buf = new StringBuilder(strValue.length() + BUF_SIZE);
			for (int i = 0; i < strValue.length(); i++) {
				if (strValue.charAt(i) == esc && i < strValue.length() - 1 && strValue.charAt(i + 1) == esc) {
					buf.append(DOUBLE_ESC).append(DOUBLE_ESC);
					i++;
				} else {
					buf.append(strValue.charAt(i));
				}
			}
			
			strValue = buf.toString();
		}
		
		return escapeValue(strValue, escapeUnicode);
	}
	
	/* Copied from super class */
	protected String makeSingleLineValue(List<?> values) {
		if (!values.isEmpty()) {
			Iterator<?> it = values.iterator();
			String lastValue = escapeValue(it.next(), true);
			StringBuilder buf = new StringBuilder(lastValue);
			while (it.hasNext()) {
				// if the last value ended with an escape character, it has
				// to be escaped itself; otherwise the list delimiter will
				// be escaped
				if (lastValue.endsWith(ESCAPE) && countTrailingBS(lastValue) / 2 % 2 != 0) {
					buf.append(ESCAPE).append(ESCAPE);
				}
				buf.append(delimiter);
				lastValue = escapeValue(it.next(), true);
				buf.append(lastValue);
			}
			return buf.toString();
		} else {
			return null;
		}
	}
	
	/* Copied from commons-lang StringEscapeUtils.hex(...) */
	public static String hex(char ch) {
		return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
	}
	
	/* Copied from commons-lang StringEscapeUtils.escapeJavaStyleString(...) */
	public static String escapeValue(String str, boolean escapeUnicode) {
		StringBuilder sb = new StringBuilder();
		if (str == null) {
			return sb.toString();
		}
		int sz;
		sz = str.length();
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			
			// handle unicode
			if (ch > 0xfff) {
				if (escapeUnicode) {
					sb.append("\\u" + hex(ch));
				} else {
					sb.append(ch);
				}
			} else if (ch > 0xff) {
				if (escapeUnicode) {
					sb.append("\\u0" + hex(ch));
				} else {
					sb.append(ch);
				}
			} else if (ch > 0x7f) {
				if (escapeUnicode) {
					sb.append("\\u00" + hex(ch));
				} else {
					sb.append(ch);
				}
			} else if (ch < 32) {
				switch (ch) {
					case '\b':
						sb.append('\\');
						sb.append('b');
						break;
					case '\n':
						sb.append('\\');
						sb.append('n');
						break;
					case '\t':
						sb.append('\\');
						sb.append('t');
						break;
					case '\f':
						sb.append('\\');
						sb.append('f');
						break;
					case '\r':
						sb.append('\\');
						sb.append('r');
						break;
					default:
						if (escapeUnicode) {
							if (ch > 0xf) {
								sb.append("\\u00" + hex(ch));
							} else {
								sb.append("\\u000" + hex(ch));
							}
						} else {
							sb.append(ch);
						}
						break;
				}
			} else {
				switch (ch) {
					case '"':
						sb.append('\\');
						sb.append('"');
						break;
					case '\\':
						sb.append('\\');
						sb.append('\\');
						break;
					default:
						sb.append(ch);
						break;
				}
			}
		}
		return sb.toString();
	}
}
