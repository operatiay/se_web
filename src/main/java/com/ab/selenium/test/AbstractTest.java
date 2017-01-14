package com.ab.selenium.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.ab.selenium.test.evaluation.IResultEvaluator;
import com.ab.selenium.util.CaptureScreenshots;
import com.ab.selenium.util.ElementHighlighter;

/**
 * Starts the session based on the test suite parameter <b>{@code testTarget}</b> in the web browser defined by the test
 * suite parameter <b>{@code platform}</b>. <br>
 * The extending class can access the WebDriver using {@link #getDriver()}, which has the initial page loaded. <br>
 * The session is automatically started and closed. <br>
 * 
 * @see {@link AbstractPage#logStepResult(String)} to capture screenshots if the test is set to capture mode via
 *      {@link AbstractPage#setCaptureMode(boolean)} . Alternatively use
 *      {@link CaptureScreenshots#captureScreen(String)} .
 */
@Listeners(TestListener.class)
public abstract class AbstractTest {

	/** Separate the array parameters in TestNG.xml using <b>{@code [-->]}</b> in order to split using it. */
	private static final String DEFAULT_PARAMETER_SEPARATOR_PATTERN = "\\[-->\\]";

	/** WebDriver for test. */
	private WebDriver fDriver;

	/** Screenshot store folder. */
	private String fScreenshotStoreFolder;

	/** Minimal length of a valid html page. Productive pages should be at least this long. */
	private static final int MIN_HTML_PAGE_LENGTH = 50;
	/** Maximal number of retries until marking as failure. */
	private static final int MAX_RETRY_NR = 3;

	/** Platform to be tested on. */
	private PlatformTypeEnum platformTypeEnum;

	/**
	 * This method is run before the test. It opens the {@link webSite} in the {@link browser} and preconfigures the
	 * {@link screenshotStoreFolder} for screenshots in case they are going to be stored. <br>
	 * If there is need to execute something before the platform initialisation the method
	 * {@link #ensurePreConditions()} needs to be overwritten.
	 * 
	 * @param gridServer
	 *            Selenium Grid server to run on (may be omitted to execute the test locally)
	 * @param driverPath
	 *            path to the driver executable for local test execution. Not all drivers need it.
	 * @param platform
	 *            testing platform to be used. It's mainly used for browsers. See {@link PlatformTypeEnum}.
	 * @param operatingSystem
	 *            default: WINDOWS, should something different be used, this value can override it. Make sure to use the
	 *            same platform as used in node.json configuration. For allowed values see {@link Platform}
	 * @param testTarget
	 *            the start website to open in the browser
	 * @param screenshotStoreFolder
	 *            the folder to store images
	 * @throws MalformedURLException
	 *             if the gridServer parameter is not correct
	 */

	@BeforeClass(groups = { "default" }, alwaysRun = true)
	@Parameters({ "gridServer", "port", "driverPath", "os", "platform", "testTarget", "screenshotStoreFolder" })
	protected final void startSession(@Optional("") final String gridServer, @Optional("4444") final String port,
			@Optional() final String driverPath, @Optional("WINDOWS") final String operatingSystem,
			final String platform, final String testTarget, final String screenshotStoreFolder)
			throws MalformedURLException {

		this.fScreenshotStoreFolder = screenshotStoreFolder;
		// do the @pre
		ensurePreConditions();

		// initialise driver
		DesiredCapabilities capabilities;
		this.platformTypeEnum = PlatformTypeEnum.getByName(platform);

		switch (this.platformTypeEnum) {
		case CHROME:
			capabilities = DesiredCapabilities.chrome();
			// multiple platforms (operating systems) possible
			capabilities.setPlatform(Platform.valueOf(operatingSystem));
			if (useGridServer(gridServer)) {
				setDriver(new RemoteWebDriver(getGridServerURL(gridServer, port), capabilities));
				// add capture screenshot capabilities
				augmentRemoteWebDriverCapabilities();
			} else {
				applyDriver("webdriver.chrome.driver", driverPath);
				// capabilities.setCapability("chrome.nativeEvents", true);
				ChromeDriver chromeDriver = new ChromeDriver(capabilities);
				setDriver(chromeDriver);
			}
			break;

		case SAFARI:
			capabilities = DesiredCapabilities.safari();
			// capabilities.setCapability(SafariDriver.CLEAN_SESSION_CAPABILITY, true);
			// multiple platforms (operating systems) possible
			capabilities.setPlatform(Platform.MAC);
			if (useGridServer(gridServer)) {
				// File safariExecutable = new File("C:\\Program Files (x86)\\Safari");
				// System.setProperty("SafariDefaultPath", safariExecutable.getAbsolutePath());
				for (int i = 1; i <= MAX_RETRY_NR; i++) {
					try {
						setDriver(new RemoteWebDriver(getGridServerURL(gridServer, port), capabilities));
						break;
					} catch (WebDriverException e) {
						if (i == MAX_RETRY_NR) {
							throw e;
						}
						Logger.logStepResult("Retrying again to create new web driver", false);
					}
				}
				// add capture screenshot capabilities
				augmentRemoteWebDriverCapabilities();
			} else {
				// File safariExecutable = new File("D:\\Programs\\Safari\\Safari.exe");
				// if (safariExecutable.exists()) {
				// System.setProperty("SafariDefaultPath", safariExecutable.getAbsolutePath());
				// }
				setDriver(new SafariDriver(capabilities));
			}
			break;

		case FIREFOX:
			capabilities = DesiredCapabilities.firefox();
			// multiple platforms (operating systems) possible
			capabilities.setPlatform(Platform.valueOf(operatingSystem));
			ProfilesIni allProfiles = new ProfilesIni();
			FirefoxProfile profile = allProfiles.getProfile("default");
			profile.setAcceptUntrustedCertificates(true);
//			profile.setEnableNativeEvents(true);
			capabilities.setCapability(FirefoxDriver.PROFILE, profile);

			if (useGridServer(gridServer)) {
				// capabilities.setCapability(FirefoxDriver.PROFILE, "WebDriver");
				setDriver(new RemoteWebDriver(getGridServerURL(gridServer, port), capabilities));
				// add capture screenshot capabilities
				augmentRemoteWebDriverCapabilities();
			} else {
				setDriver(new FirefoxDriver(capabilities));
			}
			break;
		case IE:
		default:
			capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setPlatform(Platform.valueOf(operatingSystem));
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
			capabilities.setCapability("requireWindowFocus", true);
			capabilities.setCapability("enablePersistentHover", false);

			if (useGridServer(gridServer)) {
				// InternetExplorerDriverService service = new
				// InternetExplorerDriverService.Builder().usingPort(4444).withHost(gridServer).build();

				setDriver(new RemoteWebDriver(getGridServerURL(gridServer, port), capabilities));
				// setDriver(new InternetExplorerDriver(service, capabilities));
				// add capture screenshot capabilities
				augmentRemoteWebDriverCapabilities();
			} else {
				applyDriver("webdriver.ie.driver", driverPath);

				setDriver(new InternetExplorerDriver(capabilities));
			}
			break;
		}
		checkPreConditions();

		// make the driver known to the Browser Url at the test failures
		BrowserLogger.INSTANCE.setDriver(getDriver());
		BrowserLogger.INSTANCE.setPlatformType(this.platformTypeEnum);
		// make the driver known to the ElementHighlighter
		ElementHighlighter.INSTANCE.setDriver(getDriver());
		// reset the logger
		Logger.reset();

		// make the driver known to the CaptureScreenshots
		CaptureScreenshots.INSTANCE.setDriver(getDriver());

		if (this.platformTypeEnum.isWeb().booleanValue()) {

			// setupScreenToSize(new Dimension(browserWindowWidth, browserWindowHeight));
			if (this.platformTypeEnum.isMaximizable().booleanValue()) {
				maximiseWindow();
			}

			// delete cookies
			deleteCookies(testTarget);

			// now open the web site
			getDriver().get(testTarget);

		}

	}

	/**
	 * Verify the @pre-conditions before running. To ensure the conditions implement {@link #ensurePreConditions()}
	 * Default implementation does nothing. Override to do something.
	 */
	protected void checkPreConditions() {
	}

	/**
	 * Method to be run before any test is being executed. Here we can init the screenshot folder.
	 * 
	 * @param screenshotStoreFolder
	 *            screenshot storage folder as defined in the configuration. Subfolders are going to be created.
	 * @param beforeMethod
	 *            method, before which the current method is being executed
	 */
	@BeforeMethod(alwaysRun = true)
	@Parameters({ "screenshotStoreFolder" })
	public static final void initSingleTest(final String screenshotStoreFolder,
			final java.lang.reflect.Method beforeMethod) {
		String callingClassName = beforeMethod.getDeclaringClass().getName();
		String callingMethodName = beforeMethod.getName();
		// store the folder for screenshots for further reference
		initScreenshotFolder(screenshotStoreFolder, beforeMethod);
		Logger.logStepResult("Starting test for class [" + callingClassName + "] and method [" + callingMethodName
				+ "]", false);
	}

	/**
	 * Initialise the screenshot store folder. Be aware that the {@link CaptureScreenshot} is not ready for usage after
	 * this. The driver still needs to be set via {@link CaptureScreenshots#setDriver(WebDriver)}.
	 * 
	 * @param screenshotStoreFolder
	 *            folder to store the screenshots
	 * @param method
	 *            calling method (might be a test method or cleanAfterTests)
	 */
	private static void initScreenshotFolder(final String screenshotStoreFolder, final java.lang.reflect.Method method) {
		// store the folder for screenshots for further reference
		String callingClassName = method.getDeclaringClass().getName();
		String callingMethodName = method.getName();

		CaptureScreenshots.INSTANCE.initScreenshotStoreFolder(screenshotStoreFolder + File.separator
				+ callingClassName.substring(callingClassName.lastIndexOf(".") + 1) + File.separator
				+ callingMethodName);

	}

	/**
	 * Finish the test, clean up if needed.
	 * 
	 * @param afterMethod
	 *            which test method has been executed. Used for logging.
	 */
	@AfterMethod(alwaysRun = true)
	public static final void finalizeTest(final java.lang.reflect.Method afterMethod) {
		String callingClassName = afterMethod.getDeclaringClass().getName();
		String callingMethodName = afterMethod.getName();
		Logger.logStepResult("Test finished for class [" + callingClassName + "] and method [" + callingMethodName
				+ "]", false);
	}

	/**
	 * Hook to execute something, which needs to run before the test starts (even before the platform is initialised).
	 * Default implementation does nothing.<br>
	 * Override to do something.
	 */
	protected void ensurePreConditions() {
		// empty default implementation
	}

	/**
	 * To execute something after tests. Default implementation checks does nothing. Override to do something.
	 */
	public void cleanAfterTests() {
	}

	/**
	 * checks for the screenshot functionality to be ready for usage. Initialises the folder and set driver fo
	 * screenshot capture.
	 */
	private void initCaptureScreenshot() {
		// if (!CaptureScreenshots.INSTANCE.isReady()) {
		Method thisMethod = null;
		try {
			thisMethod = this.getClass().getMethod("cleanAfterTests");
		} catch (NoSuchMethodException | SecurityException e) {
			// this should not occur, since the search is for the method itself
			e.printStackTrace();
		}
		initScreenshotFolder(this.fScreenshotStoreFolder, thisMethod);
		CaptureScreenshots.INSTANCE.setDriver(getDriver());
		// }

	}

	/**
	 * Verifies that the driver is available under the defined path and sets the system property. Used for local
	 * execution of tests.
	 * 
	 * @param driverProperty
	 *            property as required by the driver
	 * @param driverPath
	 *            path to the driver executable
	 */
	private static void applyDriver(final String driverProperty, final String driverPath) {
		if (driverPath != null && driverPath.trim().length() > 0) {
			File serverDriverFile = new File(driverPath);
			// the parameter was set
			if (serverDriverFile.exists()) {
				// can access the file
				System.setProperty(driverProperty, driverPath);
			} else {
				throw new RuntimeException("Cannot access driver '" + driverProperty + "' with path ["
						+ serverDriverFile.getAbsolutePath() + "]");
			}
		}
	}

	/**
	 * RemoteWebDriver does not implement the {@link org.openqa.selenium.TakesScreenshot TakesScreenshot} class if the
	 * driver does have the Capabilities to take a screenshot then Augmenter will add the TakesScreenshot methods to the
	 * instance.
	 * 
	 * @see org.openqa.selenium.remote.Augmenter
	 */
	private void augmentRemoteWebDriverCapabilities() {
		WebDriver augmentedDriver = new Augmenter().augment(getDriver());
		// now store for further usage
		setDriver(augmentedDriver);
	}

	/**
	 * Opens a new window and calls the parameter url. The result is evaluated using {@link IResultEvaluator}
	 * 
	 * @param url
	 *            to be called
	 * @param evaluator
	 *            provides the right result evaluation
	 */
	protected final void callURLInNewWindow(final String url, final IResultEvaluator evaluator) {
		// open a new window
		JavascriptExecutor jsExecutor = (JavascriptExecutor) getDriver();
		jsExecutor.executeScript("window.open()");
		String[] windowHandles = new String[] {};
		// get the handles for currently open browser windows (should be 2)
		windowHandles = getDriver().getWindowHandles().toArray(windowHandles);
		// verify the number of open windows
		assert windowHandles.length == 2 : "Unexpected number of open windows.";

		// go to the last opened window
		for (String windowHandle : windowHandles) {
			getDriver().switchTo().window(windowHandle);
			if (getDriver().getPageSource().length() < MIN_HTML_PAGE_LENGTH) {
				// this should be the newly opened window
				break;
			}
		}
		// call
		getDriver().navigate().to(url);

		// TODO should wait for results

		// evaluate the call result
		Logger.INSTANCE.logStepResult("urlExecuted");
		// evaluator.evaluateCallResult(getDriver());
		// close the window
		getDriver().close();
		getDriver().switchTo().window(getDriver().getWindowHandles().iterator().next());
	}

	/**
	 * Generate URL for Selenium Grid Server.
	 * 
	 * @param gridServer
	 *            Selenium Grid server name
	 * @param port
	 *            Selenium Grid server port
	 * @return URL to communicate with hub
	 * @throws MalformedURLException
	 *             if the URL has errors
	 */
	private static URL getGridServerURL(final String gridServer, final String port) throws MalformedURLException {
		return new URL("http://" + gridServer + ":" + port + "/wd/hub");
	}

	/**
	 * Verify that the Selenium Grid server should be used based on the parameter.
	 * 
	 * @param gridServer
	 *            the name of the Grid Server
	 * @return {@code true} if the parameter is not {@code null} and not empty
	 */
	private static boolean useGridServer(final String gridServer) {
		return gridServer != null && gridServer.trim().length() > 0;
	}

	/**
	 * Set the browser window to size provided by the parameter.
	 * 
	 * @param windowDimension
	 *            {@link Dimension} containing width and height
	 */
	@SuppressWarnings("unused")
	private void setupScreenToSize(final Dimension windowDimension) {
		getDriver().manage().window().setSize(windowDimension);
	}

	/**
	 * Expands the opened browser window to full screen.
	 */
	protected final void maximiseWindow() {
		// ((JavascriptExecutor) getDriver())
		// .executeScript("window.moveTo(0,0); window.resizeTo(screen.width,screen.height);");
		getDriver().manage().window().maximize();
	}

	/**
	 * Always close the session after the tests.
	 */
	@SuppressWarnings("static-access")
	@AfterClass(groups = { "default" }, alwaysRun = true)
	protected final void closeSession() {
		// screenshots functionality must be present even if everything failed
		initCaptureScreenshot();
		// hook to execute something before closing everything
		cleanAfterTests();
		Logger.INSTANCE.logStepResult("Closing session.", false);
		CaptureScreenshots.INSTANCE.resetCounter();
		if (getDriver() != null && getDriver().getWindowHandles() != null) {
			// driver has been successfully initialised and something is still open
			for (String windowHandle : getDriver().getWindowHandles()) {
				try {
					getDriver().switchTo().window(windowHandle);
					Logger.logStepResult("Closing window " + windowHandle, Boolean.FALSE);
					getDriver().close();
					Logger.logStepResult("Closed window " + windowHandle, Boolean.FALSE);
				} catch (NoSuchWindowException e) {
					// do nothing
					Logger.logStepResult("failed to close the window", Boolean.FALSE);
				}
			}
			try {
				// waiting for close to take effect before quitting (https://bugzilla.mozilla.org/show_bug.cgi?id=1027222)
				Logger.logStepResult("waiting for 3 secs before terminating the session.", Boolean.FALSE);
				Thread.sleep(3000L);
			} catch( InterruptedException e) {
				// do nothing
				Logger.logStepResult("interrupted while waiting", Boolean.FALSE);
			}
			Logger.logStepResult("Quitting session.", Boolean.FALSE);
			try {
				getDriver().quit();
			} catch (WebDriverException e) {
				// driver seems to be dead already, but I don't care
				Logger.logStepResult("driver.quit was not successfully sent", Boolean.FALSE);
			}
		}
		Logger.INSTANCE.logStepResult("Session closed.", Boolean.FALSE);
	}

	/**
	 * Delete all cookies of the browsers.
	 * 
	 * @param testTarget
	 *            url to navigate to. Needed to delete cookies in some browsers.
	 */
	@SuppressWarnings("fallthrough")
	public final void deleteCookies(final String testTarget) {
		if (getDriver() == null) {
			return;
		}
		switch (this.platformTypeEnum) {
		case SAFARI:
			getDriver().get(testTarget);
		default:
			getDriver().manage().deleteAllCookies();
		}
	}

	/**
	 * Get {@link #fDriver}.
	 * 
	 * @return driver
	 */
	public final WebDriver getDriver() {
		return this.fDriver;
	}

	/**
	 * Set {@link #fDriver}. Used to call a test from another test.
	 * 
	 * @param driver
	 *            the {@link WebDriver} to set
	 */
	public final void setDriver(final WebDriver driver) {
		this.fDriver = driver;
		this.fDriver.manage().timeouts().pageLoadTimeout(5, TimeUnit.MINUTES);
	}

	/**
	 * Splits the parameter assuming that the {@link #DEFAULT_PARAMETER_SEPARATOR_PATTERN} can be used.
	 * 
	 * @param concatenatedParameters
	 *            one string with parameters, separated by <b>{@code [-->]}</b>
	 * @return array containing the splitting result
	 */
	protected static String[] splitParameter(final String concatenatedParameters) {
		return concatenatedParameters.split(DEFAULT_PARAMETER_SEPARATOR_PATTERN + "+");
	}

}
