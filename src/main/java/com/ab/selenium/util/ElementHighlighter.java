package com.ab.selenium.util;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public enum ElementHighlighter {
	INSTANCE;

	/** WebDriver set to highlight the elements. */
	private JavascriptExecutor jsDriver;

	Map<By, String> xpathToStyle = new HashMap<By, String>();

	static final String HIGHLIGHT_STYLE = "background: yellow; border: 2px solid red;";
	static final String SET_STYLE_ATTRIBUTE_TEMPLATE = "arguments[0].setAttribute('style', '%s');";
	static final String HIGHLIGHT_ELEMENT_JS = String.format(SET_STYLE_ATTRIBUTE_TEMPLATE, HIGHLIGHT_STYLE);

	private ElementHighlighter() {
	}

	/**
	 * Highlight the element by setting a predefined bright style. The original style may be set using
	 * {@link #restoreElement(RenewableWebElement)}.
	 * 
	 * @param element
	 */
	public void highlightElement(RenewableWebElement element) {

		// store the original value
		this.xpathToStyle.put(element.getSearchTerm(), element.getAttribute("style"));
		// execute the JavaScript to highlight
		this.jsDriver.executeScript(HIGHLIGHT_ELEMENT_JS, element.getWebElement());

	}

	/**
	 * Restores the style of the element to the original value.
	 * 
	 * @param element
	 */
	public void restoreElement(RenewableWebElement element) {
		if (this.xpathToStyle.containsKey(element.getSearchTerm())) {
			String originalStyle = this.xpathToStyle.remove(element.getSearchTerm());
			this.jsDriver.executeScript(String.format(SET_STYLE_ATTRIBUTE_TEMPLATE, originalStyle),
					element.getWebElement());
		} else {
			throw new IllegalArgumentException("Cannot restore an element, which has not been highlighted before.");
		}
	}

	public void setDriver(WebDriver driver) {
		this.jsDriver = (JavascriptExecutor) driver;
	}

}
