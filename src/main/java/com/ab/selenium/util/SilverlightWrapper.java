package com.ab.selenium.util;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * The SilverLightSelenium is the component adding SilverLight communication capabilities to the Selenium framework.
 * Basically, the Silvernium is a Selenium RC Client driver extension for helping exercise the tests against the
 * SilverLight component.
 **/
public class SilverlightWrapper {
    private String scriptKey = "";
    private final String silverLightJSStringPrefix;
    private JavascriptExecutor jsDriver;

    private enum BrowserConstants {
        FIREFOX3("Firefox/3."), FIREFOX2("Firefox/2."), IE("MSIE");
        private String prefix;

        private BrowserConstants(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix;
        }
    }

    protected JavascriptExecutor getJSExecutor() {
        return this.jsDriver;
    }

    public SilverlightWrapper(final WebDriver driver, final String silverLightObjectId, final String constScriptKey) {
        if (constScriptKey != null && !constScriptKey.equals("")) {
            // got some meaningful value, replacing
            this.scriptKey = constScriptKey + ".";
        }
        setDriver(driver);
        // verify the browser type
        String userAgent = getUserAgent();
        if (userAgent.contains(BrowserConstants.FIREFOX3.getPrefix())
                || userAgent.contains(BrowserConstants.IE.getPrefix())) {
            this.silverLightJSStringPrefix = createJSPrefixViaWindowDocument(silverLightObjectId);
        } else {
            this.silverLightJSStringPrefix = createJSPrefixViaDocument(silverLightObjectId);
        }
    }

    /**
     * Constructor for general purpose.
     * 
     * @param driver
     *            WebDriver displaying the page
     * @param silverLightObjectId
     *            html id value of the silverlight component.
     */
    public SilverlightWrapper(final WebDriver driver, final String silverLightObjectId) {
        setDriver(driver);
        // verify the browser type
        String userAgent = getUserAgent();
        if (userAgent.contains(BrowserConstants.FIREFOX3.getPrefix())
                || userAgent.contains(BrowserConstants.IE.getPrefix())) {
            silverLightJSStringPrefix = createJSPrefixViaWindowDocument(silverLightObjectId);
        } else {
            silverLightJSStringPrefix = createJSPrefixViaDocument(silverLightObjectId);
        }
    }

    /**
     * Retrieve the user agent from the browser.
     * 
     * @return user agent of the browser.
     */
    private String getUserAgent() {
        String userAgent = getJSExecutor().executeScript("return navigator.userAgent;").toString();
        return userAgent;
    }

    /**
     * This constructor is used for test purposes.
     * 
     * @param driver
     *            WebDriver displaying the page
     * @param silverLightObjectId
     *            id of the SilverLight object on the page
     * @param constSilverLightJSStringPrefix
     *            JavaScript prefix to communicate with the SilverLight object
     * @param testOnly
     *            is this a test?
     */
    SilverlightWrapper(final WebDriver driver, final String silverLightObjectId,
            final String constSilverLightJSStringPrefix, final boolean testOnly) {
        setDriver(driver);
        this.silverLightJSStringPrefix = constSilverLightJSStringPrefix;
    }

    /**
     * Setting the driver, initialising some help structures.
     * 
     * @param driver
     *            WebDriver displaying the page.
     */
    private void setDriver(final WebDriver driver) {
        if (driver instanceof JavascriptExecutor) {
            this.jsDriver = (JavascriptExecutor) driver;
        } else {
            throw new UnsupportedOperationException("Thid driver doesn't support JavaScript execution");
        }
    }

    /**
     * Method used to create an instance for test purpose.
     * 
     * @param driver
     *            WebDriver displaying the page
     * @param silverLightObjectId
     *            id of the SilverLight object on the page
     * @return instance of SilverlightWrapper
     */
    static SilverlightWrapper createSilverLightObjAsDocument(final WebDriver driver, final String silverLightObjectId) {
        return new SilverlightWrapper(driver, silverLightObjectId, createJSPrefixViaDocument(silverLightObjectId), true);
    }

    /**
     * SilverLight object is the Window document? (whatever)
     * 
     * @param driver
     *            WebDriver displaying the page
     * @param silverLightObjectId
     *            id of the SilverLight object on the page
     * @return instance of SilverlightWrapper
     */
    static SilverlightWrapper createSilverLightObjAsWindowDocument(final WebDriver driver,
            final String silverLightObjectId) {
        return new SilverlightWrapper(driver, silverLightObjectId,
                createJSPrefixViaWindowDocument(silverLightObjectId), true);
    }

    /**
     * JavaScript prefix to access via 'window.document'.
     * 
     * @param silverLightObjectId
     *            id of the SilverLight object to interact with
     * @return JavaScript prefix
     */
    static String createJSPrefixViaWindowDocument(final String silverLightObjectId) {
        return "return window.document['" + silverLightObjectId + "'].";
    }

    /**
     * JavaScript prefix to access via 'document'.
     * 
     * @param silverLightObjectId
     *            id of the SilverLight object to interact with
     * @return JavaScript prefix
     */
    static String createJSPrefixViaDocument(final String silverLightObjectId) {
        return "return document['" + silverLightObjectId + "'].";
    }

    /**
     * Execute function as a direct method.
     * 
     * @param functionName
     *            function to be executed
     * @param args
     *            parameters for the function
     * @return result
     */
    public final String executeDirectMethod(final String functionName, final String... args) {
        return getJSExecutor().executeScript(this.jsForDirectMethod(functionName, args)).toString();
    }

    public String executeContentMethod(final String functionName, final String... args) {
        return getJSExecutor().executeScript(this.jsForContentMethod(functionName, args)).toString();
    }

    public String getPropertyValue(String propertyName) {
        return getJSExecutor().executeScript(this.jsForContentScriptGetProperty(propertyName)).toString();
    }

    public String setPropertyValue(String propertyName, String arg) {
        return getJSExecutor().executeScript(this.jsForContentScriptSetProperty(propertyName, arg)).toString();
    }

    public String call(String functionName, String... args) {
        return getJSExecutor().executeScript(this.jsForContentScriptMethod(functionName, args)).toString();
    }

    public String getSettingsProperty(String propertyName) {
        return getJSExecutor().executeScript(this.jsForSettingsProperty(propertyName)).toString();
    }

    public String getContentProperty(String propertyName) {
        return getJSExecutor().executeScript(this.jsForContentProperty(propertyName)).toString();
    }

    public String getDirectProperty(String propertyName) {
        return getJSExecutor().executeScript(this.jsForDirectProperty(propertyName)).toString();
    }

    String silverLightJSStringPrefix() {
        return this.silverLightJSStringPrefix;
    }

    String jsForDirectMethod(String functionName, String... args) {
        String functionArgs = "";
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                functionArgs = functionArgs + "'" + args[i] + "',";
            }
            // remove last comma
            functionArgs = functionArgs.substring(0, functionArgs.length() - 1);
        }
        return silverLightJSStringPrefix + functionName + "(" + functionArgs + ");";
    }

    String jsForContentScriptMethod(final String functionName, String... args) {
        String functionArgs = "";
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                functionArgs = functionArgs + "'" + args[i] + "',";
            }
            // remove last comma
            functionArgs = functionArgs.substring(0, functionArgs.length() - 1);
        }
        return silverLightJSStringPrefix + "content." + scriptKey + functionName + "(" + functionArgs + ");";
    }

    String jsForContentScriptGetProperty(String propertyName) {
        return silverLightJSStringPrefix + "content." + scriptKey + propertyName + ";";
    }

    String jsForContentScriptSetProperty(String propertyName, String arg) {
        return silverLightJSStringPrefix + "content." + scriptKey + propertyName + "='" + arg + "';";
    }

    String jsForContentMethod(String functionName, String... args) {
        String functionArgs = "";
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                functionArgs = functionArgs + "'" + args[i] + "',";
            }
            // remove last comma
            functionArgs = functionArgs.substring(0, functionArgs.length() - 1);
        }
        return silverLightJSStringPrefix + "content." + functionName + "(" + functionArgs + ");";
    }

    String jsForSettingsProperty(String propertyName) {
        return silverLightJSStringPrefix + "settings." + propertyName + ";";
    }

    String jsForContentProperty(String propertyName) {
        return silverLightJSStringPrefix + "content." + propertyName + ";";
    }

    /**
     * Construct JavaScript to read direct property.
     * 
     * @param propertyName
     *            which direct property to ask for
     * @return JavaScript to read direct property
     */
    final String jsForDirectProperty(final String propertyName) {
        return silverLightJSStringPrefix + propertyName + ";";
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        int jsExecutorHash = 0;
        if (getJSExecutor() != null) {
            jsExecutorHash = getJSExecutor().hashCode();
        }
        result = prime * result + jsExecutorHash;
        int jssStringPrefixHash = 0;
        if (silverLightJSStringPrefix != null) {
            jssStringPrefixHash = silverLightJSStringPrefix.hashCode();
        }
        result = prime * result + jssStringPrefixHash;
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SilverlightWrapper other = (SilverlightWrapper) obj;
        if (getJSExecutor() == null) {
            if (other.getJSExecutor() != null) {
                return false;
            }
        } else if (!getJSExecutor().equals(other.getJSExecutor())) {
            return false;
        }
        if (silverLightJSStringPrefix == null) {
            if (other.silverLightJSStringPrefix != null) {
                return false;
            }
        } else if (!silverLightJSStringPrefix.equals(other.silverLightJSStringPrefix)) {
            return false;
        }
        return true;
    }

    /**
     * Verify whether this version of Silverlight is being supported.
     * 
     * @param versionString
     *            version to be checked
     * @return yes/no
     */
    public final Boolean isVersionSupported(final String versionString) {
        return new Boolean(this.executeDirectMethod("isVersionSupported", versionString));
    }

    /**
     * Read content property 'accessibility'.
     * 
     * @return its value
     */
    public final String getAccessibility() {
        return this.getContentProperty("accessibility");
    }

    /**
     * Read content property 'actualHeight'.
     * 
     * @return its value
     */
    public final Integer getActualHeight() {
        return new Integer(this.getContentProperty("actualHeight"));
    }

    /**
     * Read content property 'actualWidth'.
     * 
     * @return its value
     */
    public final Integer actualWidth() {
        return new Integer(this.getContentProperty("actualWidth"));
    }

    /**
     * Create xamlContent in specified scope.
     * 
     * @param xamlContent
     *            content
     * @param nameScope
     *            scope
     */
    public final void createFromXaml(final String xamlContent, final String nameScope) {
        this.executeContentMethod("createFromXaml", xamlContent, nameScope);
    }

    /**
     * Find an object by name using content method 'findName'.
     * 
     * @param objectName
     *            to be found
     * @return result of execution (What exactly?)
     */
    public final String findName(final String objectName) {
        return this.executeContentMethod("findName", objectName);
    }

    /**
     * Is the Silverlight application being executed in full screen?
     * 
     * @return yes/no
     */
    public final boolean isFullScreen() {
        return new Boolean(this.getContentProperty("fullScreen")).booleanValue();
    }

    /**
     * Get direct property 'initParams'.
     * 
     * @return its value
     */
    public final String getInitParams() {
        return this.getDirectProperty("initParams");
    }

    /**
     * Get direct property 'isLoaded'.
     * 
     * @return yes/no
     */
    public final boolean isLoaded() {
        return new Boolean(this.getDirectProperty("isLoaded")).booleanValue();
    }

    /**
     * Get direct property 'root'.
     * 
     * @return its value
     */
    public final String getRoot() {
        return this.getDirectProperty("root");
    }

    /**
     * Get settings property 'background'.
     * 
     * @return its value
     */
    public final String getBackground() {
        return this.getSettingsProperty("background");
    }

    /**
     * Get settings property 'enabledFramerateCounter'.
     * 
     * @return yes/no
     */
    public final boolean isEnabledFramerateCounter() {
        return new Boolean(this.getSettingsProperty("enabledFramerateCounter")).booleanValue();
    }

    /**
     * Get settings property 'enableRedrawRegions'.
     * 
     * @return yes/no
     */
    public final boolean isEnableRedrawRegions() {
        return new Boolean(this.getSettingsProperty("enableRedrawRegions")).booleanValue();
    }

    /**
     * Get settings property 'enableHtmlAccess'.
     * 
     * @return yes/no
     */
    public final boolean isEnableHtmlAccess() {
        return new Boolean(this.getSettingsProperty("enableHtmlAccess")).booleanValue();
    }

    /**
     * Get settings property 'maxFrameRate'.
     * 
     * @return its value
     */
    public final int getMaxFrameRate() {
        return new Integer(this.getSettingsProperty("maxFrameRate")).intValue();
    }

    /**
     * Get settings property 'windowless'.
     * 
     * @return yes/no
     */
    public final boolean isWindowless() {
        return new Boolean(this.getSettingsProperty("windowless")).booleanValue();
    }

    /**
     * Get direct property 'source'.
     * 
     * @return its value
     */
    public final String getSource() {
        return this.getDirectProperty("source");
    }
}
