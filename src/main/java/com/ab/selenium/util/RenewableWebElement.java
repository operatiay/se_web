package com.ab.selenium.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.ab.selenium.pages.AbstractPage;
import com.ab.selenium.test.Logger;

/**
 * This wrapper class is used to check the existence of an WebElement (see {@link #doesExist()}. You don't need to add
 * the WebElement. Instead the WebElement is defined by a search term. Should the underlying WebElement become staled it
 * is again searched for. Usage:<br>
 * <ul>
 * <li>The most general constructor is
 * {@link RenewableWebElement#RenewableWebElement(WebDriver, WebElement, By, boolean)}, which can be used to set all the
 * parameters manually.</li>
 * <li>The convenience constructor {@link RenewableWebElement#RenewableWebElement(WebDriver, By)} should be used when
 * the search term is known.</li>
 * <li>
 * The convenience constructor {@link RenewableWebElement#RenewableWebElement(WebDriver, WebElement)} should be only
 * used if the search term is not known. Then a heuristic is used to determine the {@link By#xpath(String)} and store it
 * for the repeated search.</li>
 * </ul>
 */
public class RenewableWebElement implements WebElement, TakesScreenshot {

	/** JavaScript snippet to calculate xPath for an html element. Located within the project. */
	private static final String GET_XPATH_JS_FILE = "/javascript/getPathTo.js";
	/** JavaScript snippet as read from {@link #GET_XPATH_JS_FILE}. */
	static final String GET_XPATH_JS;
	/** Maximal number of retries (3). */
	private static final int RETRY_COUNT = 3;

	static {
		String fileContent = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					RenewableWebElement.class.getResourceAsStream(GET_XPATH_JS_FILE)));
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent += line;
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to locate the file getPathTo.js", e);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read the file getPathTo.js", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignore) {
				Logger.logStepResult(
						String.format("There were problems closing the file [%s] after reading", GET_XPATH_JS_FILE),
						false);
			}
		}
		GET_XPATH_JS = fileContent;
	}

	/**
	 * How to find this WebElement. May not be xPath, especially if the constructor
	 * {@link #RenewableWebElement(WebDriver, By)} is used.
	 */
	private By fSearchTerm;
	/**
	 * WebElement underlying this object. May sometimes be null. See also {@link #findElement()},
	 * {@link #getWebElement()}
	 */
	private RemoteWebElement webElement;
	/** WebDriver displaying the page with this element. */
	private WebDriver fDriver;

	// private boolean isAlreadySearched = false;

	/**
	 * Uses the driver to search the WebElement by the given searchTerm.
	 * 
	 * @param driver
	 *            WebDriver displaying the page
	 * @param searchTerm
	 *            how to find this element
	 */
	public RenewableWebElement(final WebDriver driver, final By searchTerm) {
		this(driver, null, searchTerm, false);
	}

	/**
	 * Determine the XPath for the {@code Element} in order to use it for repeated search.
	 * 
	 * @param driver
	 *            {@link WebDriver} displaying the content
	 * @param element
	 *            {@link WebElement}, which should be represented by this object.
	 */
	public RenewableWebElement(final WebDriver driver, final WebElement element) {
		this(driver, element, getXPath(driver, element), true);
	}

	/**
	 * Determines the xPath to find the provided WebElement. Works only if all parameters are not null.
	 * 
	 * @param driver
	 *            WebDriver displaying the page.
	 * @param element
	 *            WebElement on the page.
	 * @return By.xpath, which can be used to find the element on the page.
	 */
	private static By getXPath(final WebDriver driver, final WebElement element) {
		// ensure the parameters are not null
		if (element == null) {
			throw new NullPointerException("WebElement parameter for getXpath is null. Cannot proceed.");
		}
		if (driver == null) {
			throw new NullPointerException("WebDriver is null. Cannot proceed.");
		}
		// now get the xpath, waiting half the time you normally do
		return By.xpath(AbstractPage.waitForCondition(new ExpectedCondition<String>() {
			public String apply(final WebDriver aDriver) {
				String xpath = (String) ((JavascriptExecutor) aDriver).executeScript(GET_XPATH_JS, element);
				Logger.logStepResult("WebElement initiated. xpath=" + xpath, false);
				return xpath;
			}

			@Override
			public String toString() {
				return String.format("Getting xPath for an element [%s] using JavaScript.", element);
			}
		}, Long.valueOf(AbstractPage.WAIT_TIME_LIMIT.longValue() / 2), driver));

	}

	/**
	 * Constructor with additional parameters.
	 * 
	 * @param driver
	 *            WebDriver displaying the element
	 * @param element
	 *            (Remote)WebElement if it has already been found elsewhere
	 * @param searchTerm
	 *            how to find the element
	 * @param alreadySearched
	 *            has it been already searched for?
	 */
	public RenewableWebElement(final WebDriver driver, final WebElement element, final By searchTerm,
			final boolean alreadySearched) {
		this.fDriver = driver;
		this.fSearchTerm = searchTerm;
		this.webElement = (RemoteWebElement) element;
		// this.isAlreadySearched = alreadySearched;
	}

	public void clear() {
		getWebElement().clear();
	}

	@Override
	public final void click() {
		for (int i = 0; i < RETRY_COUNT; i++) {
			try {
				getWebElement().click();
				break;
			} catch (Exception e) {
				if (e.getMessage()!=null && e.getMessage().contains("Timed out waiting for page load")) {
					throw e;
				}
				e.printStackTrace();
			}
		}
	}

	public RenewableWebElement findElement(By by) {
		WebElement thisElement = getWebElement();
		return new RenewableWebElement(this.fDriver, thisElement.findElement(by));
	}

	/**
	 * Find a child element with specified finder.
	 * 
	 * @param by
	 *            how to find the child element
	 * @return RenewableWebElement
	 */

	/**
	 * Find child elements with specified finder.
	 * 
	 * @param by
	 *            how to find the children elements
	 * @return Actually returning {@link RenewableWebElement} objects in a list.
	 */
	public final List<WebElement> findElements(final By by) {
		WebElement thisElement = getWebElement();
		List<WebElement> elements = thisElement.findElements(by);
		Logger.logStepResult("Found [" + elements.size() + "] elements", false);
		List<WebElement> returnElements = new ArrayList<WebElement>();
		for (int i = 0; i < elements.size(); i++) {
			returnElements.add(new RenewableWebElement(this.fDriver, elements.get(i)));
		}
		return returnElements;
	}

	/**
	 * Get attribute value by name.
	 * 
	 * @param name
	 *            attribute name
	 * @return attribute value
	 */
	public final String getAttribute(final String name) {
		String attributeValue = null;
		for (int i = 0; i < RETRY_COUNT; i++) {
			try {
				attributeValue = getWebElement().getAttribute(name);
				break;
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains("Timed out waiting for page load")) {
					throw e;
				}
				e.printStackTrace();
			}
		}
		return attributeValue;
	}

	@Override
	public final String getCssValue(final String propertyName) {
		return getWebElement().getCssValue(propertyName);
	}

	/**
	 * @return location on screen
	 */
	@Override
	public final Point getLocation() {
		return getWebElement().getLocation();
	}

	/**
	 * @return size of the element as {@link Dimension}.
	 */
	public final Dimension getSize() {
		return getWebElement().getSize();
	}

	@Override
	public Rectangle getRect() {
		return getWebElement().getRect();
	}

	// /**
	// * @return tag name of this element
	// */
	@Override
	public final String getTagName() {
		return getWebElement().getTagName();
	}

	@Override
	public final String getText() {
		String text = null;
		for (int i = 0; i < RETRY_COUNT; i++) {
			try {
				text = getWebElement().getText();
				break;
			} catch (Exception e) {
				if (e.getMessage()!=null && e.getMessage().contains("Timed out waiting for page load")) {
					throw e;
				}
				e.printStackTrace();
			}
		}
		return text;
	}

	@Override
	public final boolean isDisplayed() {
		for (int i = 1; i <= RETRY_COUNT; i++) {
			try {
				WebElement element = getWebElement();
				if (element != null) {
					return element.isDisplayed();
				}
				return false;
			} catch (Throwable e) {
				if (i == RETRY_COUNT) {
					e.printStackTrace();
				} else {
					// wait before continuing iterations
					AbstractPage.waitForLoad(this.fDriver, Long.valueOf(1000));
				}
			}
		}
		return false;
	}

	@Override
	public final boolean isEnabled() {
		return getWebElement().isEnabled();
	}

	@Override
	public final boolean isSelected() {
		return getWebElement().isSelected();
	}

	@Override
	public final void sendKeys(final CharSequence... keysToSend) {
		getWebElement().sendKeys(keysToSend);
	}

	@Override
	public final void submit() {
		getWebElement().submit();
	}

	/**
	 * Retrieve the WebElement behind this renewable one.
	 * 
	 * @return the underlying WebElement
	 */
	public final WebElement getWebElement() {
		if (this.webElement == null) {
			findElement();
		} else {
			// try to access the element
			try {
				this.webElement.getTagName();
			} catch (StaleElementReferenceException e) {
				// need to find the element after it has been modified
				findElement();
			} catch (WebDriverException e) {
				findElement();
			} catch (NullPointerException e) {
				// RemoteWebElement.java:323 (Boolean) null
				findElement();
			} catch (IllegalArgumentException e) {
				findElement();
			}
		}
		return this.webElement;
	}

	public void reset() {
		this.webElement = null;
		// this.isAlreadySearched = false;
	}

	/**
	 * Checks if the WebElement defined by the search term exists on the current page.
	 * 
	 * @return true if it exists, otherwise false.
	 */
	public final boolean isElementPresent() {
		return getWebElement() != null;
	}

	private void findElement() {
		// forget the previous object
		reset();
		try {
			this.webElement = (RemoteWebElement) this.fDriver.findElement(this.fSearchTerm);
		} catch (TimeoutException ignore) {
			// Do nothing
		} catch (NoSuchElementException ignore) {
			// Do nothing
		}
	}

	/*
	 * private RemoteWebElement getRemoteWebElement() { return this.webElement; }
	 */

	/**
	 * Get the term to search for this WebElement. May have been set manually (if this object has been created via
	 * {@link #RenewableWebElement(WebDriver, By)} or generated automatically if any other constructor has been used.
	 * 
	 * @return By to find the element
	 */
	public final By getSearchTerm() {
		return this.fSearchTerm;
	}

	@Override
	public final <X> X getScreenshotAs(final OutputType<X> format) throws WebDriverException {
		byte[] wholeScreen;
		X outputScreenshot = null;
		ByteArrayOutputStream baos = null;
		Point location = getWebElement().getLocation();
		Dimension size = getWebElement().getSize();
		if (this.fDriver instanceof TakesScreenshot) {
			try {
				// get the whole page as bytes
				wholeScreen = ((TakesScreenshot) this.fDriver).getScreenshotAs(OutputType.BYTES);
				// cut out the relevant part
				BufferedImage screenshot = ImageIO.read(new ByteArrayInputStream(wholeScreen));
				BufferedImage subimage = screenshot.getSubimage(location.x, location.y, size.width, size.height);
				baos = new ByteArrayOutputStream();
				ImageIO.write(subimage, "PNG", baos);
				baos.flush();
				outputScreenshot = format.convertFromPngBytes(baos.toByteArray());
				baos.close();
			} catch (IOException e) {
				if (baos != null) {
					try {
						baos.close();
					} catch (IOException e1) {
						Logger.logStepResult(String.format(
								"There were problems closing the ByteArrayOutputStream [%s] after writing", baos),
								false);
					}
				}
				throw new WebDriverException(e);
			}
		}
		return outputScreenshot;
	}
}
