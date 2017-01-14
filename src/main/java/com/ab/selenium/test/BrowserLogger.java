package com.ab.selenium.test;

import javax.naming.OperationNotSupportedException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.ab.selenium.util.ElementHighlighter;
import com.ab.selenium.util.RenewableWebElement;

/**
 * Use this enum to log URL at the time of test failure.<br>
 * An enum is used for implementation in order to make it a thread safe and serializable singleton.
 */
public enum BrowserLogger {
	/** The only instance of the logger. */
	INSTANCE;

	/** WebDriver set to capture the screen. */
	private WebDriver fDriver;
	/** Platform used. */
	private PlatformTypeEnum fPlatformType;

	/**
	 * Set the WebDriver, which is going to be used.
	 * 
	 * @param driver
	 *            displaying the page.
	 */
	public void setDriver(final WebDriver driver) {
		this.fDriver = driver;
	}

	/**
	 * Write current URL via {@link Logger}.
	 * 
	 * @param url
	 *            optional url to change to before logging
	 * @param captureScreenshot
	 *            do you want a screenshot?
	 * @throws OperationNotSupportedException
	 *             if WebDriver fails to capture the URL.
	 */
	public void logUrl(final String url, final boolean captureScreenshot) throws OperationNotSupportedException {
		if (url != null && url.length() > 0) {
			this.fDriver.navigate().to(url);
		}
		try {
			Logger.logStepResult("Url:[" + this.fDriver.getCurrentUrl() + "]", captureScreenshot);
		} catch (WebDriverException e) {
			throw new OperationNotSupportedException();
		}
	}

	/**
	 * Documents the step by<br>
	 * <li>storing an image if the {@link #setCaptureMode(boolean) capture mode} is {@code true}<br>
	 * or <li>logging a message if the {@link #setCaptureMode(boolean) capture mode} is {@code false}.
	 * 
	 * @param message
	 *            used for log or as file suffix
	 * @param callingClassName
	 *            class, which is calling this method. Used in the log message.
	 */
	public static void logStepResult(final String message, final String callingClassName) {
		Logger.INSTANCE.logStepResult(callingClassName.substring(callingClassName.lastIndexOf(".") + 1) + "_-_"
				+ message);
	}

	/**
	 * Documents the step the same way as {@link #logStepResult(String)} does. Additionally highlights the
	 * <b>element</b>.
	 * 
	 * @param message
	 *            to log as part of the file name
	 * @param element
	 *            to be highlighted
	 * @param callingClassName
	 *            for logging
	 */
	public static void logStepResult(final String message, final RenewableWebElement element,
			final String callingClassName) {
		try {
			ElementHighlighter.INSTANCE.highlightElement(element);
			logStepResult(message, callingClassName);
			ElementHighlighter.INSTANCE.restoreElement(element);
		} catch (Exception e) {
			// Do Nothing
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Get the platform the test is being executed on.
	 * 
	 * @return {@link PlatformTypeEnum}
	 */
	public final PlatformTypeEnum getPlatformType() {
		return this.fPlatformType;
	}

	/**
	 * Set the platform the test is being executed on.
	 * 
	 * @param platformType
	 *            platform
	 */
	public void setPlatformType(final PlatformTypeEnum platformType) {
		this.fPlatformType = platformType;
	}
}
