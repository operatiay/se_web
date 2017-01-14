package com.ab.selenium.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Html utility methods to work with tags.
 * 
 */
public final class HtmlUtils {

	/**
	 * Should not be used, since all methods are static.
	 */
	private HtmlUtils() {
	}

	/**
	 * Split the html style string.
	 * 
	 * @param htmlStyle
	 *            original html style string
	 * @return Map containing keys and values.
	 */
	public static Map<String, String> getStyle(final String htmlStyle) {
		return getKeyValueMap(htmlStyle, ";", ":", false);
	}

	/**
	 * Use to extract a key-value map from an html attribute containing multiple key-value pairs.
	 * 
	 * @param htmlToParse
	 *            html attribute to parse
	 * @param entrySeparator
	 *            separator used to separate one key-value pair from the next one
	 * @param keyValueSeparator
	 *            separator used to separate key from value
	 * @param removeValueQuotation
	 *            should the flanking quotation signs ({@code"} and {@code '}) be removed from values? They are removed
	 *            only if the first and last character of trimmed value are the same quotation signs.
	 * @return a Map containing parsed key-value pairs
	 */
	public static Map<String, String> getKeyValueMap(final String htmlToParse, final String entrySeparator,
			final String keyValueSeparator, final boolean removeValueQuotation) {
		HashMap<String, String> results = new HashMap<String, String>();
		String[] entries = htmlToParse.split(getRegexByString(entrySeparator) + "+");
		String[] keyValuePair = new String[2];
		for (String entry : entries) {
			keyValuePair = entry.split(getRegexByString(keyValueSeparator) + "+");
			if (keyValuePair[0] != null && keyValuePair[0].length() > 0) {
				// try to remove quotation if required
				if (removeValueQuotation) {
					keyValuePair[1] = removeQuotation(keyValuePair[1]);
				}
				results.put(keyValuePair[0], keyValuePair[1]);
			}
		}
		return results;
	}

	/**
	 * Convert a plain string to a regular expression, which matches exactly the parameter.
	 * 
	 * @param plainString
	 *            any string to be converted
	 * @return regular expression matching the plainString parameter
	 */
	private static String getRegexByString(final String plainString) {
		String regexp = "";
		char[] letters = plainString.toCharArray();

		for (int i = 0; i < letters.length; i++) {
			regexp += getCharRegexp(letters[i]);
		}
		return regexp;
	}

	/**
	 * Create a regular expression, which is going to match a specific character.
	 * 
	 * @param ch
	 *            character for regular expression.
	 * @return regular expression matching the character
	 */
	private static String getCharRegexp(final char ch) {
		String regex = "";
		if ("\\.^$|?*+[]{}()\\t\\n\\f\\r".indexOf(ch) != -1) {
			regex += "\\" + ch;
		} else if (Character.isSpaceChar(ch)) {
			regex += "[" + ch + "]";
		} else {
			regex += ch;
		}
		return regex;
	}

	/**
	 * Removes leading and flanking quotation if found. Flanking white spaces are removed as well.
	 * 
	 * @param value
	 *            to truncate quotations from
	 * @return value without quotation signs, if there were any around it.
	 */
	private static String removeQuotation(final String value) {
		if (value == null) {
			return null;
		}
		String trimmedValue = value.trim();
		if (trimmedValue.length() > 1) {
			String firstChar = trimmedValue.substring(0, 1);
			String lastChar = trimmedValue.substring(trimmedValue.length() - 1);
			if (firstChar.equals(lastChar) && firstChar.matches("[\\\"']")) {
				// this value has leading and flanking quotation signs
				return trimmedValue.substring(1, trimmedValue.length() - 1);
			}
		}
		return trimmedValue;
	}
}
