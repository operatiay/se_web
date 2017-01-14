package com.ab.selenium.util;

/**
 * Utility class for JavaScript calls, which are not as intuitive as they should be.
 */
public final class JavaScriptUtil {

	/**
	 * Private constructor.
	 */
	private JavaScriptUtil() {
		// should not be called, since all methods are static
	}

	/**
	 * 
	 * @param objectName
	 *            name of the object
	 * @return JavaScript snippet to verify the presence of an object named {@code objectName} within the loaded page
	 */
	public static String getJSSnippetToFindObjectByName(final String objectName) {

		return "selenium.isElementPresent(\"" + objectName + "\");";
	}

	/**
	 * 
	 * @param xPathExpression
	 *            to find the object
	 * @return JavaScript snippet to verify the presence of an object which can be found using {@code xPathExpression}
	 *         within the loaded page
	 */
	public static String getJSSnippetToFindObjectByXPath(final String xPathExpression) {
		return "selenium.isElementPresent(\"xpath=" + xPathExpression + "\");";
	}

	/**
	 * This method uses a Selenium core API to find an object by ID or XPath. Should be used by
	 * {@link com.thoughtworks.selenium.Selenium#waitForCondition(String, String)} for the first parameter.
	 * 
	 * @param elementIdOrXPathExpression
	 *            the ID of a specific object or an XPath expression to find it
	 * @return JavaScript snippet to verify the presence of an object {@code elementIdOrXPathExpression} within the
	 *         loaded page
	 */
	public static String getJSSnippetToFindObjectUsingBrowserbot(final String elementIdOrXPathExpression) {
		return "(var x = selenium.browserbot.findElementOrNull('" + elementIdOrXPathExpression
				+ "'); x != null && x.style.display == 'none';";
	}

	// /**
	// * This method uses document api to find an element and call a mouse event
	// * 'mouseover'/'onmouseover' to simulate a hover event.
	// * This is a workaround for native Action event. See also Selenium <a
	// href='http://code.google.com/p/selenium/issues/detail?id=2067'>Bug 2067</a>.
	// * @param elementId
	// * html attribute {@code @id} of the element to be marked
	// * @return JavaScript snippet to hover over an element
	// */
	// public static String getJSSnippetToPersistHoverOnElementById(final String elementId) {
	// String snippet = "var elem = document.getElementById('".concat(elementId).concat("');")
	// .concat("if(document.createEvent) {")
	// .concat("    var evObj = document.createEvent('MouseEvents');")
	// .concat("    evObj.initEvent( 'mouseover', true, false );")
	// .concat("    elem.dispatchEvent(evObj);")
	// .concat("} else if(document.createEventObject) {")
	// .concat("    elem.fireEvent('onmouseover');")
	// .concat("}");
	// return snippet;
	// }

	/**
	 * This method uses document api to find an element and call a mouse event 'mouseover'/'onmouseover' to simulate a
	 * hover event. This is a workaround for native Action event. See also Selenium <a
	 * href='http://code.google.com/p/selenium/issues/detail?id=2067'>Bug 2067</a>. Usage of the snippet is via
	 * {@code ((JSExecutor)driver).executeScript(getJSSnippetToPersistHoverOnElement(),webElement);}.
	 * 
	 * @return JavaScript snippet to be executed.
	 */
	public static String getJSSnippetToPersistHoverOnElement() {
		String snippet = "if(document.createEvent){" + "    var evObj = document.createEvent('MouseEvents');"
				+ "    evObj.initEvent('mouseover', true, false);" + "    arguments[0].dispatchEvent(evObj);"
				+ "} else if(document.createEventObject) {" + "    arguments[0].fireEvent('onmouseover');" + "}";
		return snippet;
	}

	public static String getJSSnippetToNavigateBack() {
		return "history.go(-1)";

	}

}
