package com.ab.selenium.test;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.testng.Reporter;

import com.ab.selenium.util.CaptureScreenshots;

/**
 * Use this enum to log messages. An enum is used for implementation in order to make it a threadsafe and serializable
 * singleton.
 */
public enum Logger {
	INSTANCE;

	// /** Time to sleep after a log entry. In milliseconds. */
	// private static final Long TIME_TO_SLEEP = 1000L;
	/** capture screenshots mode. */
	private Boolean isCaptureMode = Boolean.TRUE;
	/** format to print out the time. */
	private static String timeFormat = "%02d:%02d:%02d:%03d";
	/** start time of the test. */
	private static long startTimeMillis;

	/**
	 * {@code Boolean.TRUE} means that the logging is done via screenshots.<br />
	 * {@code Boolean.FALSE} means that the logging is done via log output. Make sure to call this before trying to log.
	 * 
	 * @param captureMode
	 *            the captureMode to set
	 */
	public void setCaptureMode(final Boolean captureMode) {
		this.isCaptureMode = captureMode;
	}

	/**
	 * Reset the logger to start from scratch (e.g. message counting etc.)
	 */
	public static void reset() {
		CaptureScreenshots.INSTANCE.resetCounter();
		startTimeMillis = System.currentTimeMillis();
	}

	/**
	 * Documents the step by <li>storing an image if the {@link #setCaptureMode(boolean) capture mode} is {@code true}<br>
	 * or <li>logging a message if the {@link #setCaptureMode(boolean) capture mode} is {@code false}.
	 * 
	 * @param message
	 *            used for log or as file suffix
	 */
	public void logStepResult(final String message) {
		if (this.isCaptureMode == null) {
			throw new IllegalStateException("The capture mode has not yet been set.");
		}
		logStepResult(message, this.isCaptureMode);
	}

	/**
	 * Logging the message without taking the screenshot.
	 * @param message
	 */
	public static void logMessage(final String message) {
		// log message
		String messagePrefix = "STEP_DOC [" + getSystemTime() + "] [" + getRelativeTime() + "]:";
		Reporter.log(messagePrefix + message);
		System.out.println(messagePrefix + message);
		// AbstractPage.waitForLoad(driver, TIME_TO_SLEEP);
	}

	private static String getRelativeTime() {
		long timeDifference = System.currentTimeMillis() - startTimeMillis;
		long hours, minutes, seconds, millis, rest;
		hours = TimeUnit.MILLISECONDS.toHours(timeDifference);
		rest = timeDifference - TimeUnit.HOURS.toMillis(hours);
		minutes = TimeUnit.MILLISECONDS.toMinutes(rest);
		rest -= TimeUnit.MINUTES.toMillis(minutes);
		seconds = TimeUnit.MILLISECONDS.toSeconds(rest);
		millis = rest - TimeUnit.SECONDS.toMillis(seconds);
		return String.format(timeFormat, hours, minutes, seconds, millis);
	}

	private static String getSystemTime() {
		Calendar now = Calendar.getInstance();
		Integer hours = Integer.valueOf(now.get(Calendar.HOUR_OF_DAY));
		Integer minutes = Integer.valueOf(now.get(Calendar.MINUTE));
		Integer seconds = Integer.valueOf(now.get(Calendar.SECOND));
		Integer millis = Integer.valueOf(now.get(Calendar.MILLISECOND));
		return String.format(timeFormat, hours, minutes, seconds, millis);
	}

	/**
	 * Involve the {@link CaptureScreenshots#captureScreen(String)}.
	 * 
	 * @param message
	 *            description of the captured moment.
	 */
	private static void captureScreenshot(final String message) {
		try {
			CaptureScreenshots.INSTANCE.captureScreen(message);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Documents the step by <li>storing an image if the parameter {@code captureScreenshot} is {@code true}</li><br>
	 * or <li>logging a message if the parameter {@code captureScreenshot} is {@code false}.</li>
	 * 
	 * @param message
	 *            used for log or as file suffix
	 * @param captureScreenshot
	 *            should a screenshot be taken rather than writing a logging message?
	 */
	public static void logStepResult(final String message, final Boolean captureScreenshot) {
		if (captureScreenshot.booleanValue()) {
			String cleanMessage = message;
			// remove tailing '.' if present
			if (cleanMessage.charAt(cleanMessage.length() - 1) == '.') {
				cleanMessage = cleanMessage.substring(0, cleanMessage.length() - 2);
			}
			captureScreenshot(cleanMessage);
		} else {
			logMessage(message);
		}
	}
}
