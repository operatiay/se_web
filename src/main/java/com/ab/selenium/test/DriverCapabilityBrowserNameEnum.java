package com.ab.selenium.test;

import java.util.HashMap;
import java.util.Map;

public enum DriverCapabilityBrowserNameEnum {
	/** Internet Explorer. */
	IE("internet explorer"),
	/** Mozilla Firefox. */
	FIREFOX("firefox");
	/** name of the browser. */
	private String fName;

	/**
	 * internal mapping of names to enumeration entries for quick access by
	 * name.
	 */
	private static Map<String, DriverCapabilityBrowserNameEnum> allElementsMap;

	static {
		allElementsMap = new HashMap<String, DriverCapabilityBrowserNameEnum>();
		for (DriverCapabilityBrowserNameEnum type : DriverCapabilityBrowserNameEnum.values()) {
			allElementsMap.put(type.fName, type);
		}
	}
	/**
	 * Internal constructor.
	 * @param name
	 *            the name of the browser
	 */
	DriverCapabilityBrowserNameEnum(final String name) {
		this.fName = name;
	}

	/**
	 * Use this method to get the corresponding enumeration entry by name.
	 * @param name
	 *            the name of the browser
	 * @return enumeration entry for the name or null
	 */
	public static DriverCapabilityBrowserNameEnum getByName(final String name) {
		return allElementsMap.get(name);
	}
}
