package com.ab.selenium.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.ab.selenium.pages.AbstractPage;
import com.ab.selenium.test.Logger;

/**
 * When the switch from one page to another one has to be tried out until it succeeds, the factory does it.
 * 
 */
public abstract class PageFactory {
	/** Maximum number of retries when performing transactions. */
	private static final int MAX_RETRIES = 3;

	/**
	 * Clicks to an element max of 3 times and tries to create a new page Instance. Should be used when the click itself
	 * is not reliable. Throws RuntimeException in case of a timeout while waiting for page/post condition.
	 * 
	 * @param element
	 *            webElement to be clicked for transition
	 * @param parentPage
	 *            page where to look for the element
	 * @param returnPageClass
	 *            page to be returned
	 * @param post
	 *            condition to be met after successful click
	 * @return instance of returnPageClass
	 */
	private static AbstractPage goToPageVia(final RenewableWebElement element, final AbstractPage parentPage,
			final Class<? extends AbstractPage> returnPageClass, final ExpectedCondition<Boolean> post) {
		AbstractPage returnPage = null;
		for (int i = 1; i <= MAX_RETRIES; i++) {
			if (element.isElementPresent()) {
				// only if the clickable element is (still) present on the page
				if (i == 1) {
					((JavascriptExecutor) parentPage.getDriver()).executeScript("javascript:window.scrollBy("
							+ ((WebElement) element).getLocation().x + "," + element.getLocation().y + ")");
				}
				element.click();
			}
			// try doing the post
			if (post != null) {
				try {
					AbstractPage.waitForCondition(post, AbstractPage.WAIT_TIME_LIMIT, parentPage.getDriver());
				} catch (TimeoutException e) {
					if (i == MAX_RETRIES) {
						// not successful
						throw new RuntimeException("Unable to verify the post condition [" + post.toString()
								+ "] during transition from " + parentPage.getClass().getSimpleName() + " to "
								+ returnPageClass.getSimpleName());
					}
					continue;
				}
			}
			try {
				returnPage = returnPageClass.getConstructor(AbstractPage.class).newInstance(parentPage);
				break;
			} catch (InstantiationException e) {
				e.printStackTrace();
				if (i == MAX_RETRIES) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				if (i == MAX_RETRIES) {
					e.printStackTrace();
				}
			} catch (IllegalAccessException e) {
				if (i == MAX_RETRIES) {
					e.printStackTrace();
				}
			} catch (IllegalArgumentException e) {
				if (i == MAX_RETRIES) {
					Logger.logStepResult(i + " Retries", false);
					e.printStackTrace();
				}
			} catch (InvocationTargetException e) {
				if (i == MAX_RETRIES) {
					Throwable cause = e.getTargetException();
					if (cause instanceof TimeoutException) {
						e.printStackTrace();
					}
				}
			}
		}
		// if Return Page is null then Throw RuntimeException and exit from the program
		if (returnPage == null) {
			throw new RuntimeException("unable to navigate from " + parentPage.getClass().getSimpleName() + " to "
					+ returnPageClass.getSimpleName());
		}
		return returnPage;
	}

	/**
	 * Switch to New window by clicking to WebElement found by <b>finder</b>.
	 * 
	 * @param finder
	 *            to find the element to click
	 * @param driver
	 *            WebDriver displaying the page
	 */
	public static void switchToNewWindowByClick(final By finder, final WebDriver driver) {
		final Set<String> oldWindowHandles = driver.getWindowHandles();

		for (int i = 1; i < MAX_RETRIES; i++) {
			if (AbstractPage.isElementPresent(finder, driver)
					&& (oldWindowHandles.size() == driver.getWindowHandles().size())) {
				AbstractPage.waitForElement(finder, AbstractPage.WAIT_TIME_LIMIT, driver).click();
			}
			// get current window handles
			try {
				AbstractPage.waitForCondition(new ExpectedCondition<Boolean>() {
					public Boolean apply(final WebDriver innerDriver) {
						final Set<String> newWindowHandles = driver.getWindowHandles();
						for (String windowHandles : newWindowHandles) {
							// compare handles
							if (!oldWindowHandles.contains(windowHandles)) {
								// switch to the new window handle
								innerDriver.switchTo().window(windowHandles);
								return Boolean.TRUE;
							}
						}
						return Boolean.FALSE;
					}

					@Override
					public String toString() {
						return "Waiting for new window to open and then switching to it.";
					}
				}, AbstractPage.WAIT_TIME_LIMIT, driver);
				break;
			} catch (TimeoutException e) {
				if (i == MAX_RETRIES) {
					throw new TimeoutException("Unable to switch to window or click has not resulted in new page");
				}
			}
		}
	}

	/**
	 * Go to Page by clicking on the html element identified by the finder. Before click, the precondition is executed.
	 * And after click, the post condition is executed.
	 * 
	 * @param finder
	 *            Is used to identify the element for click
	 * @param page
	 *            page where the element to click on is located
	 * @param returnPageClass
	 *            class of the page to return
	 * @param pre
	 *            Is the condition that has to be ensured before the clicks on the html element. If null then condition
	 *            will not be executed.
	 * @param post
	 *            Is the condition that has to be ensured after the page object has been initialised. If null then
	 *            condition will not be executed.
	 * @return resulting page
	 */
	public static AbstractPage goToPageVia(final By finder, final AbstractPage page,
			final Class<? extends AbstractPage> returnPageClass, final ExpectedCondition<Boolean> pre,
			final ExpectedCondition<Boolean> post) {
		if (pre != null) {
			AbstractPage.waitForCondition(pre, AbstractPage.WAIT_TIME_LIMIT, page.getDriver());
		}
		AbstractPage returnPage = null;
		RenewableWebElement elementToClick = null;
		try {
			elementToClick = AbstractPage.waitForElement(finder, AbstractPage.WAIT_TIME_LIMIT, page.getDriver());
		} catch (TimeoutException e) {
			throw new TimeoutException("Unable to find the find the finder[" + finder + "] to click ");
		}
		returnPage = goToPageVia(elementToClick, page, returnPageClass, post);

		return returnPage;
	}

}
