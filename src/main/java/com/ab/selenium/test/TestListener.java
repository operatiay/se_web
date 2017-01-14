package com.ab.selenium.test;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.openqa.selenium.WebDriverException;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.ab.selenium.test.evaluation.SoftAssert;
import com.ab.selenium.util.CaptureScreenshots;

public class TestListener extends TestListenerAdapter implements IInvokedMethodListener {

	@Override
	public void onTestFailure(final ITestResult tr) {
		try {
			try {
				BrowserLogger.INSTANCE.logUrl(null, false);
				Logger.logStepResult("Class Called [" + tr.getClass() + ", Method Called [" + tr.getMethod() + "]",
						false);
				Logger.logStepResult("Exception thrown was [" + tr.getThrowable() + "]", false);
				Logger.logStepResult("Exception thrown message was [" + tr.getThrowable().getMessage() + "]", false);
				Logger.logStepResult("Exception thrown stack trace was [" + tr.getThrowable().getStackTrace() + "]",
						false);
				Logger.logStepResult("Exception thrown cause was [" + tr.getThrowable().getCause() + "]", false);

			} catch (OperationNotSupportedException e) {
				Logger.logStepResult("Method Called [" + tr.getMethod() + "]", false);
			}
			CaptureScreenshots.INSTANCE.captureScreen("test failure");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Class<? extends Throwable> thrownClass = tr.getThrowable().getClass();
			if (thrownClass.equals(WebDriverException.class)) {
				Logger.logStepResult("WebDriverException has been thrown, cannot proceed with the test.", false);
				// a webdriver failure is not meaningful as test result (test didn't run through, but didn't find any
				// errors)
				tr.setStatus(ITestResult.SKIP);
				super.onTestSkipped(tr);
			} else {
				Logger.logStepResult("Error of class [" + thrownClass.getCanonicalName() + "] has been thrown. "
						+ "Since it's not a WebDriverException, it must be an exception to be looked at.", false);
				super.onTestFailure(tr);
			}
		}
	}

	@Override
	public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
		// no need to do anything as of now.
	}

	/**
	 * This specific implementation is based on {@link SoftAssert} usage. Should there have been any soft assertion
	 * failures, the test is going to be set as failure with all errors being part of the message.<br>
	 * 
	 * @param method
	 *            invoked method
	 * @param tr
	 *            test result
	 */
	public final void afterInvocation(final IInvokedMethod method, final ITestResult tr) {
		// check SoftAssert verifications

		if (method.isTestMethod()) {

			List<Throwable> verificationFailures = SoftAssert.getVerificationFailures(tr);

			// if there are verification failures...
			if (!verificationFailures.isEmpty()) {

				// set the test to failed
				tr.setStatus(ITestResult.FAILURE);

				// if there is an assertion failure add it to verificationFailures
				if (tr.getThrowable() != null) {
					verificationFailures.add(tr.getThrowable());
				}

				int size = verificationFailures.size();
				// if there's only one failure just set that
				if (size == 1) {
					tr.setThrowable(verificationFailures.get(0));
				} else {
					// create a failure message with all failures and stack traces (except last failure)
					StringBuffer failureMessage = new StringBuffer("Multiple failures (").append(size).append("):\n");
					for (int i = 0; i < size - 1; i++) {
						failureMessage.append(throwableToStringMessage(verificationFailures.get(i), i + 1, size));
						failureMessage.append('\n');
					}

					// final failure
					failureMessage.append(throwableToStringMessage(verificationFailures.get(size - 1), size, size));

					// set merged throwable
					Throwable merged = new AssertionError(failureMessage.toString());
					merged.setStackTrace(verificationFailures.get(0).getStackTrace());

					tr.setThrowable(merged);
				}
				// FIXME: this needs to be done since this is being invoked several times for the same pair of
				// arguments. Probably due to combination of TestListenerAdapter and IInvokedMethodListener

				// empty the verification failures for this test result
				SoftAssert.clearVerificationFailures(tr);
			}
		}
	}

	/**
	 * Beautifully format the error message for a Throwable error.
	 * 
	 * @param t
	 *            error (Throwable)
	 * @param currentNo
	 *            current number of the error
	 * @param totalNo
	 *            total number of errors
	 * @return String representation of the error.
	 */
	private static String throwableToStringMessage(final Throwable t, final int currentNo, final int totalNo) {
		StringBuffer failureMessage = new StringBuffer();
		failureMessage.append("Failure ").append(currentNo).append(" of ").append(totalNo).append(":\n");
		failureMessage.append('\t').append(t.getMessage()).append('\n');
		return failureMessage.toString();
	}
}
