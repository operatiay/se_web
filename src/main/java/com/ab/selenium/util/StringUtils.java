package com.ab.selenium.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	/**
	 * /item/view/10024209 Finds the item id from the url.
	 * 
	 * @param url
	 *            where item id has to be found
	 * @return item id.
	 */
	public static Integer getItemIdFromUrl(String url) {
		Matcher matcher = Pattern.compile("(\\d){2,11}").matcher(url);
		matcher.find();
		return Integer.valueOf(matcher.group());
	}

	/**
	 * Find the no of the item in the url.
	 * 
	 * @param url
	 *            where no of item is present.
	 * @return total of item present in page.
	 */
	public static Integer getItemCount(String url) {
		Matcher matcher = Pattern.compile("(\\d){1,2}").matcher(url);
		matcher.find();
		return Integer.valueOf(matcher.group());
	}

	/**
	 * Finds the Nth occurrences of the digits in the string.
	 * 
	 * @param text
	 *            where nth occurrences of digits has to be found.
	 * @param n
	 *            occurrences of digits.
	 * @return nth digits.
	 */
	public static int getNthDigitsFromString(String text, int n) {
		int results = 0;
		Matcher matcher = Pattern.compile("(\\d+)").matcher(text);
		for (int i = 1; i <= n; i++) {
			matcher.find();
			results = Integer.valueOf(matcher.group());
		}
		return results;
	}
}
