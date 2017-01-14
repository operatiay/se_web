package com.ab.selenium.util;

import java.awt.Point;

import org.openqa.selenium.By;
//import org.openqa.selenium.HasInputDevices;
import org.openqa.selenium.Keys;

//import org.openqa.selenium.Mouse;
//import org.openqa.selenium.HasInputDevices;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ab.selenium.pages.AbstractPage;

/**
 * Encapsulate the mouse events.
 */
public final class MouseActions {

	/**
	 * Private constructor.
	 */
	private MouseActions() {

	}

	/**
	 * Method to simulate a right click on the mouse on a specific element.
	 * 
	 * @param element
	 *            the {@link WebElement} which should be clicked on
	 */
	public static void mouseRightClick(final WebElement element) {
		// right mouse click = SHIFT + F10
		element.sendKeys(Keys.chord(Keys.SHIFT, Keys.F10));
	}

	/**
	 * Method to move the mouse over an element. Sometimes needed to activate a button. Mouse hover need to be
	 * persisted. This method should be used if the activated element is a different one than the one to move the mouse
	 * over.
	 * 
	 * @see {@link Actions#moveToElement(WebElement)}
	 * @param driver
	 *            {@link WebDriver} displaying the page
	 * @param parentElementFinder
	 *            {@link By} finder to be used to place the mouse over. May not be {@code null}.
	 * @param elementToBeVisibleFinder
	 *            {@link By} finder to wait for. May not be {@code null}.
	 * @throws UnsupportedOperationException
	 *             if one of the finders is {@code null}.
	 */
	public static void moveMouseToElement(final WebDriver driver, final By parentElementFinder,
			final By elementToBeVisibleFinder) throws UnsupportedOperationException {
		final long waitBetweenSubactions = 1000L;
		if (parentElementFinder != null && elementToBeVisibleFinder != null) {
			// sleep before moving (in case there is still something blocking the browser)
			waitFor(waitBetweenSubactions);
			Actions builder = new Actions(driver);
			builder.moveToElement(driver.findElement(parentElementFinder)).build().perform();
			waitFor(waitBetweenSubactions);
			WebDriverWait wait =
					new WebDriverWait(driver, TimeUtils.getTimeInSeconds(AbstractPage.WAIT_TIME_LIMIT).longValue());
			wait.until(ExpectedConditions.elementToBeClickable(elementToBeVisibleFinder));
		} else {
			throw new UnsupportedOperationException("Both finders should not be null.");
		}
	}

	/**
	 * Method to move the mouse over an element. Sometimes needed to activate a button. Mouse hover need to be
	 * persisted. This method should be used if the activated element is visible before start. the mouse over.
	 * 
	 * @see {@link Actions#moveToElement(WebElement)}
	 * @param driver
	 *            {@link WebDriver} displaying the page
	 * @param elementFinder
	 *            {@link By} finder to be used to place the mouse over. May not be {@code null}.
	 * @throws UnsupportedOperationException
	 *             if the finder is {@code null}.
	 */
	public static void moveMouseToElement(final WebDriver driver, final By elementFinder)
			throws UnsupportedOperationException {
		if (elementFinder != null) {
			moveMouseToElement(driver, elementFinder, elementFinder);
		} else {
			throw new UnsupportedOperationException("Finder may not be null.");
		}
	}

	/**
	 * Method to move the mouse over an element and click. Sometimes needed to activate a button.
	 * 
	 * @see {@link Actions#moveToElement(WebElement)}
	 * @param driver
	 *            {@link WebDriver} displaying the page
	 * @param element
	 *            {@link WebElement} to place the mouse over and click
	 */
	public static void moveMouseToElementAndClick(final WebDriver driver, final WebElement element) {
		Actions builder = new Actions(driver);
		builder.moveToElement(element).click().build().perform();
	}

	/**
	 * Place the mouse on a specified location relative to a WebElement and click.
	 * 
	 * @see Actions#moveToElement(WebElement, int, int)
	 * @param driver
	 *            {@link WebDriver} displaying the page
	 * @param findBy
	 *            {@link By} to find the 'anchor' element
	 * @param offset
	 *            {@link Point} the offset from the element. Origin is top left corner of the element.
	 */
	public static void moveMouseToCoordinatesAndClick(final WebDriver driver, final By findBy, final Point offset) {
		Actions builder = new Actions(driver);
		builder.moveToElement(driver.findElement(findBy), offset.x, offset.y).click().perform();
	}

	/**
	 * Simulates a touch event on the given element. Note: Currently it is not verified that the implementation
	 * simulates touch on all devices. The touch event was successfully simulated with Chrome on Windows and the
	 * application (SAP Mobile Launcher) used JQuery mobile.
	 * 
	 * @param driver
	 *            {@link WebDriver} displaying the page
	 * @param element
	 *            The {@link WebElement} to touch
	 * @param touchTimeMilis
	 *            the time in milliseconds how long the element is touched.
	 */
	public static void touch(final WebDriver driver, final WebElement element, final long touchTimeMilis) {
		Mouse mouse = getMouse(driver);

		Coordinates coordinates = getCoordinates(element);
		mouse.mouseMove(coordinates);
		mouse.mouseDown(coordinates);
		waitFor(touchTimeMilis);
		mouse.mouseUp(coordinates);
	}

	/**
	 * Get hold of the mouse.
	 * 
	 * @param driver
	 *            WebDriver displaying the page
	 * @return Mouse of the driver
	 */
	private static Mouse getMouse(final WebDriver driver) {
		Mouse mouse = ((HasInputDevices) driver).getMouse();
		return mouse;
	}

	/**
	 * Get the coordinates of the element.
	 * 
	 * @param element
	 *            of interest
	 * @return coordinates of the element
	 */
	private static Coordinates getCoordinates(final WebElement element) {
		Coordinates coordinates = ((Locatable) element).getCoordinates();
		return coordinates;
	}

	/**
	 * Lets the execution of this thread sleep for given time.
	 * 
	 * @param miliseconds
	 *            how long to sleep
	 */
	private static void waitFor(final long miliseconds) {
		try {
			Thread.sleep(miliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Simulate a 'swipe' gesture by dragging the start element to the end element.
	 * 
	 * @param driver
	 *            WebDriver displaying the page
	 * @param start
	 *            WebElement to start the gesture from
	 * @param end
	 *            WebElement to end the gesture at
	 */
	public static void swipe(final WebDriver driver, final WebElement start, final WebElement end) {
		Actions builder = new Actions(driver);
		builder.dragAndDrop(start, end).perform();
	}

	/**
	 * Convenience method to swipe vertically.
	 * 
	 * @param driver
	 *            WebDriver displaying the page
	 * @param start
	 *            WebElement to start the gesture from
	 * @param yOffset
	 *            how far to move
	 */
	public static void swipeVertical(final WebDriver driver, final WebElement start, final int yOffset) {
		Actions builder = new Actions(driver);
		builder.dragAndDropBy(start, yOffset, start.getLocation().getY()).perform();
	}

}
