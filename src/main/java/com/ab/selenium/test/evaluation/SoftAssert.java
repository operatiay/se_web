package com.ab.selenium.test.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.ab.selenium.test.Logger;

/**
 * Class provides functionality to do 'soft' assertions.<br>
 * This means that assertion (done via {@code verify*} methods) is executed, but the resulting failure is not thrown.
 * Throwing an exception would stop the evaluation. Rather the exception is stored and can be obtained via
 * {@link #getVerificationFailures()}.
 * 
 */
public final class SoftAssert {
	/** Singleton instance of the class. */
	private static final SoftAssert INSTANCE = new SoftAssert();

	/**
	 * Map containing verification failures for every test within execution. Key is
	 * {@link #getTestResultHash(ITestResult)}
	 */
	private Map<Long, List<Throwable>> verificationFailuresMap = new HashMap<Long, List<Throwable>>();

	/**
	 * Constructor. Doing nothing.
	 */
	private SoftAssert() {
	}

	/**
	 * Get the instance of this class, which is having the assertion failures stored.
	 * 
	 * @return the singleton instance
	 */
	private static SoftAssert getInstance() {
		return INSTANCE;
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param condition
	 *            to be checked
	 */
	public static void verifyTrue(final boolean condition) {
		verifyTrue(condition, null);
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param condition
	 *            to be checked
	 * @param errorMessage
	 *            to display on failure
	 */
	public static void verifyTrue(final boolean condition, final String errorMessage) {
		try {
			Assert.assertTrue(condition, errorMessage);
		} catch (Throwable e) {
			getInstance().addVerificationFailure(e);
		}
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param condition
	 *            to be checked to be false
	 * @param errorMessage
	 *            to display on failure
	 */
	public static void verifyFalse(final boolean condition, final String errorMessage) {
		try {
			Assert.assertFalse(condition, errorMessage);
		} catch (Throwable e) {
			getInstance().addVerificationFailure(e);
		}
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param condition
	 *            to be ckecked to be false
	 */
	public static void verifyFalse(final boolean condition) {
		verifyFalse(condition, null);
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param actual
	 *            value
	 * @param expected
	 *            value
	 * @param <T>
	 *            type doesn't really matter, since we're relying on {@link Assert#assertEquals(actual, expected)}
	 */
	public static <T> void verifyEquals(final T actual, final T expected) {
		verifyEquals(actual, expected, null);
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param actual
	 *            value
	 * @param expected
	 *            value
	 * @param errorMessage
	 *            to display on failure
	 * @param <T>
	 *            type doesn't really matter, since we're relying on {@link Assert#assertEquals(actual, expected)}
	 */
	public static <T> void verifyEquals(final T actual, final T expected, final String errorMessage) {
		try {
			Assert.assertEquals(actual, expected, errorMessage);
		} catch (Throwable e) {
			getInstance().addVerificationFailure(e);
		}
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param object
	 *            to be verified
	 * @param errorMessage
	 *            to display on failure
	 * @param <T>
	 *            type doesn't really matter, since we're relying on {@link Assert#assertNotNull(object, message)}
	 */
	public static <T> void verifyNotNull(final T object, final String errorMessage) {
		try {
			Assert.assertNotNull(object, errorMessage);
		} catch (Throwable e) {
			getInstance().addVerificationFailure(e);
		}
	}

	/**
	 * Doing the assertion but won't be stopping on failure. Assertion failures can be obtained later on via
	 * {@link #getVerificationFailures()}
	 * 
	 * @param object
	 *            to be verified
	 * @param <T>
	 *            type doesn't really matter, since we're relying on {@link Assert#assertNotNull(object)}
	 */
	public static <T> void verifyNotNull(final T object) {
		verifyNotNull(object, null);
	}

	/**
	 * Storing the verification error for further evaluation.
	 * 
	 * @param e
	 *            error
	 */
	private void addVerificationFailure(final Throwable e) {
		ITestResult testResult = Reporter.getCurrentTestResult();
		List<Throwable> verificationFailures = getVerificationFailures(testResult);
		this.verificationFailuresMap.put(getTestResultHash(testResult), verificationFailures);
		verificationFailures.add(e);
		Logger.INSTANCE.logStepResult(e.getMessage());
	}

	/**
	 * Get verification failures for a specific test.
	 * 
	 * @param testResult
	 *            test execution result
	 * @return list of soft assertion failures
	 */
	public static List<Throwable> getVerificationFailures(final ITestResult testResult) {
		List<Throwable> verificationFailures = getInstance().verificationFailuresMap.get(getTestResultHash(testResult));
		if (verificationFailures == null) {
			verificationFailures = new ArrayList<Throwable>();
		}
		return verificationFailures;
	}

	/**
	 * Remove all stored verification failures for a specific test.
	 * 
	 * @param testResult
	 *            test execution identified by its result
	 */
	public static void clearVerificationFailures(final ITestResult testResult) {
		getInstance().verificationFailuresMap.remove(getTestResultHash(testResult));
	}

	/**
	 * Unique test result hash. Should be unique for any test execution. Calculates based on host, test class, test name
	 * and start time.
	 * 
	 * @param testResult
	 *            to calculate the hash for
	 * @return Long to be used as hash
	 */
	private static Long getTestResultHash(final ITestResult testResult) {
		String uniqueRepresentation = "";
		// host
		uniqueRepresentation += testResult.getHost();
		// class
		uniqueRepresentation += testResult.getTestClass().getName();
		// test name
		uniqueRepresentation += testResult.getTestName();
		// start time
		uniqueRepresentation += testResult.getStartMillis();
		return Long.valueOf(uniqueRepresentation.hashCode());
	}

}
