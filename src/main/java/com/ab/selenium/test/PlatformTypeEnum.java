package com.ab.selenium.test;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of supported browsers and mobile devices. Each browser, which should be used for testing has to have a
 * corresponding enumeration entry.
 * 
 * @author abilyak
 */
public enum PlatformTypeEnum {

	/** Internet Explorer 10 On windows 7. */
	IE("iexplorer", Boolean.TRUE, Boolean.TRUE),

	/** Mozilla Firefox 22 On windows 7. */
	FIREFOX("firefox", Boolean.TRUE, Boolean.TRUE),

	/** Safari (driver needs to be installed separately; beware of developer license expiration). */
	SAFARI("safari", Boolean.TRUE, Boolean.TRUE),

	/** Google Chrome. **/
	CHROME("chrome", Boolean.TRUE, Boolean.TRUE),

	/** iPad for native app. **/
	IPAD("ipad", Boolean.FALSE, Boolean.FALSE),

	/** Safari browser on iPad. **/
	SAFARIIPAD("safariIpad", Boolean.TRUE, Boolean.FALSE);

	/** name of the browser. */
	private String fName;

	/** Is it a browser used for web? */
	private Boolean fIsWeb;

	/** Is browser should be maximize? */
	private Boolean fIsMaximizable;

	/**
	 * internal mapping of names to enumeration entries for quick access by name.
	 */
	private static Map<String, PlatformTypeEnum> allElementsMap;

	static {
		allElementsMap = new HashMap<String, PlatformTypeEnum>();
		for (PlatformTypeEnum type : PlatformTypeEnum.values()) {
			allElementsMap.put(type.fName, type);
		}
	}

	/**
	 * Internal constructor.
	 * 
	 * @param name
	 *            the name of the browser
	 * @param isWeb is this platform on the web
	 * @param isMaximizable is this platform maximisable? normally {@code true} for browsers
	 */
	private PlatformTypeEnum(final String name, final Boolean isWeb, final Boolean isMaximizable) {
		this.fName = name;
		this.fIsWeb = isWeb;
		this.fIsMaximizable = isMaximizable;
	}

	/**
	 * Use this method to get the corresponding enumeration entry by name.
	 * 
	 * @param name
	 *            the name of the browser
	 * @return enumeration entry for the name or null
	 */
	public static PlatformTypeEnum getByName(final String name) {
		return allElementsMap.get(name);
	}

	/**
	 * Is this platform running on web (browser)?
	 * 
	 * @return {@code true} if it's a web browser.
	 */
	public Boolean isWeb() {
		return this.fIsWeb;
	}

	/**
	 * Can the platform be maximised?
	 * 
	 * @return {@code true} if the platform supports a non full screen mode.
	 */
	public Boolean isMaximizable() {
		return this.fIsMaximizable;
	}
}
