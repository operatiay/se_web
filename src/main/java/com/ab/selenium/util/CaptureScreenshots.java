package com.ab.selenium.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.ab.selenium.util.image.ImageComparator;

/**
 * Use this class to capture screen.
 */
public enum CaptureScreenshots {
	/**
	 * Simulates a singleton.
	 */
	INSTANCE;

	/** An index for the next picture to be captured. */
	private int captureIndex = 0;

	/** Directory for screenshots. */
	private File outputDir = new File(System.getProperty("java.io.tmpdir"));

	/** Extension for compressed images. */
	private static final String CAPTURE_COMPRESSED_EXT = "JPG";
	/** Extension for not compressed images. */
	private static final String CAPTURE_UNCOMPRESSED_EXT = "PNG";
	/** default extension for capture files (jpg). */
	private static final String DEFAULT_CAPTURE_FILE_EXT = CAPTURE_COMPRESSED_EXT;

	/** WebDriver set to capture the screen. */
	private WebDriver fDriver;

	/**
	 * Private constructor.
	 */
	private CaptureScreenshots() {

	}

	/**
	 * Capture the screen of the active browser using the {@link #driver} as {@link TakesScreenshot}.
	 * 
	 * @param driver
	 *            WebDriver displaying the page
	 * @param outputfilePath
	 *            the path to store the image excluding extension (see {@code extension} parameter).
	 * @param extension
	 *            the extension for capture image. Default is {@link #DEFAULT_CAPTURE_FILE_EXT}. Other types are not
	 *            supported yet.
	 * @throws IOException
	 *             if the used {@link WebDriver} doesn't implement {@link TakesScreenshot} or there were problems
	 *             writing the file.
	 * @return file with captured screenshot
	 */
	protected static File captureScreen(final WebDriver driver, final String outputfilePath, final String extension)
			throws IOException {
		byte[] scrFile;
		if (driver instanceof TakesScreenshot) {
			scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
			// // store the file as png
			// File outputfile = new File(outputfilePath + "." + extension);
			// FileUtils.copyFile(scrFile, outputfile);
			// store the file as jpg
			return saveAsFormat(outputfilePath, scrFile, extension);
		}
		throw new IOException("WebDriver doesn't support screenshot");

	}

	/**
	 * Internal method to store the image in specified format.
	 * 
	 * @param outputfilePath
	 *            where to store the file
	 * @param srcFile
	 *            PNG file as byte array
	 * @param format
	 *            supported format
	 * @return the resulting file reference
	 * @throws IOException
	 *             if there were problems writing the file
	 */
	private static File saveAsFormat(final String outputfilePath, final byte[] srcFile, final String format)
			throws IOException {
		BufferedImage imagePNG = ImageIO.read(new ByteArrayInputStream(srcFile));

		BufferedImage imageRGB =
				new BufferedImage(imagePNG.getWidth(), imagePNG.getHeight(), BufferedImage.TYPE_INT_RGB);
		imageRGB.createGraphics().drawImage(imagePNG, 0, 0, Color.WHITE, null);
		File resultFile = new File(outputfilePath + "." + format);
		ImageIO.write(imageRGB, format, resultFile);
		return resultFile;
	}

	/**
	 * Capture screen with default parameters.
	 * <p>
	 * <b>Warning</b>: the method {@link #setDriver(WebDriver)} should have been called beforehand. Otherwise a
	 * RuntimeException is thrown
	 * 
	 * @param message
	 *            suffix for the file name
	 * 
	 * @throws IOException
	 *             if there were problems to write to the file
	 */
	public void captureScreen(final String message) throws IOException {
		if (this.fDriver != null && this.fDriver.getWindowHandle() != null) {
			File file = null;
			captureScreen(file, message);
		} else {
			throw new RuntimeException("Trying to capture screen, although the WebDriver has not been set.");
		}
	}

	/**
	 * Capturing part of the displayed page around the {@code element}. {@code message} is used as part of the resulting
	 * file name.
	 * 
	 * @param element
	 *            {@link WebElement}, which should be captured. Should have the right size and position in order to be
	 *            captured correctly.
	 * @param message
	 *            suffix for the file name.
	 * @return {@link File}, where the captured image has been stored.
	 * @throws IOException
	 *             when trying to store the image.
	 */
	public File captureElement(final RenewableWebElement element, final String message) throws IOException {
		return captureElement(element, message, DEFAULT_CAPTURE_FILE_EXT);
	}

	/**
	 * Capturing part of the displayed page around the {@code element}. {@code message} is used as part of the resulting
	 * file name. Stores with extension.
	 * 
	 * @param element
	 *            {@link WebElement}, which should be captured. Should have the right size and position in order to be
	 *            captured correctly.
	 * @param message
	 *            suffix for the file name.
	 * @param captureFileExt
	 *            file extension. PNG would result in uncompressed image. JPG comresses the image with quality loss.
	 * @return {@link File}, where the captured image has been stored.
	 * @throws IOException
	 *             when trying to store the image.
	 */
	public File captureElement(final RenewableWebElement element, final String message, String captureFileExt)
			throws IOException {
		byte[] screenshotAs = element.getScreenshotAs(OutputType.BYTES);
		BufferedImage subimage = ImageIO.read(new ByteArrayInputStream(screenshotAs));
		BufferedImage imageRGB =
				new BufferedImage(subimage.getWidth(), subimage.getHeight(), BufferedImage.TYPE_INT_RGB);
		imageRGB.createGraphics().drawImage(subimage, 0, 0, Color.WHITE, null);
		File resultFile =
				new File(this.outputDir.getAbsolutePath() + File.separator + generateNewCaptureIndex() + "_" + message
						+ "." + DEFAULT_CAPTURE_FILE_EXT);
		ImageIO.write(imageRGB, captureFileExt, resultFile);
		return resultFile;
	}

	/**
	 * Captures the screen. The resulting file is stored under {@code outputFileDir} if it is not {@code null}.
	 * Otherwise it is stored under the default java temp directory.<br>
	 * The generated file name looks as follows:<br>
	 * {@code [generatedIndex]_[fileExplanation].[{@link #DEFAULT_CAPTURE_FILE_EXT}] }
	 * 
	 * @param outputFileDir
	 *            directory for output
	 * @param fileExplanation
	 *            suffix to add to the generated index
	 * @throws IOException
	 *             if there were problems writing the file
	 */
	private void captureScreen(final File outputFileDir, final String fileExplanation) throws IOException {
		if (this.fDriver != null) {
			String outputfilePath;
			// can capture, since WebDriver is set
			if (outputFileDir != null) {
				// trying to store in a specific directory
				outputfilePath = outputFileDir.getAbsolutePath();
			} else {
				// storing in the default directory
				outputfilePath = this.outputDir.getAbsolutePath();
			}
			outputfilePath += File.separator + generateNewCaptureIndex() + "_" + fileExplanation;
			captureScreen(this.fDriver, outputfilePath, DEFAULT_CAPTURE_FILE_EXT);
		} else {
			throw new RuntimeException("Trying to capture screen, although the WebDriver has not been set.");
		}
	}

	/**
	 * Fills up the leading 0's up to length of the private variable {@code digitsPlaceholder} and increases the
	 * {@link #captureIndex} for the next run.
	 * 
	 * @return a standardised capture index as string.
	 */
	private String generateNewCaptureIndex() {
		String digitsPlaceholder = "0000";
		int missingDigits = digitsPlaceholder.length() - (this.captureIndex + "").length();
		return digitsPlaceholder.substring(0, missingDigits) + this.captureIndex++;
	}

	/**
	 * Initialise the screenshot store folder. The folder is created if necessary.
	 * 
	 * @param screenshotStoreFolder
	 *            folder to store screenshots
	 */
	public void initScreenshotStoreFolder(final String screenshotStoreFolder) {
		File storeFolder = null;
		if (screenshotStoreFolder.trim().length() > 0) {
			try {
				storeFolder = new File(screenshotStoreFolder);
				storeFolder.getParentFile();
				if (!storeFolder.exists()) {
					// try to create the folder (structure)
					storeFolder.mkdirs();
				}
			} catch (SecurityException e) {
				throw new RuntimeException("The folder to store screenshots can't be created. "
						+ "Please provide a location with permission to write in.", e);
			}
			// if (storeFolder != null) {
			this.outputDir = storeFolder;
			// }
		}
	}

	/**
	 * Verifies that the partImage is part of the elementOfInterest. No transformations are performed.
	 * 
	 * @param elementOfInterest
	 *            part of the page, which may contain {@code partImage}
	 * @param partImage
	 *            the part image to be found. Should be in PNG format (without compression) and 24-bit colour scheme.
	 * @return {@code true} if partImage has been found in fullImage
	 * @throws IOException
	 *             if there were problems reading the files
	 */
	public static boolean containsImage(final RenewableWebElement elementOfInterest, final File partImage)
			throws IOException {
		boolean isPartOfFullImage = false;

		FileInputStream partImageIS = new FileInputStream(partImage);
		ImageComparator comparator = new ImageComparator(partImageIS);
		partImageIS.close();

		FileInputStream fullImageIS =
				new FileInputStream(CaptureScreenshots.INSTANCE.captureElement(elementOfInterest,
						"captureForComparison", CAPTURE_UNCOMPRESSED_EXT));
		isPartOfFullImage = comparator.isPartOf(fullImageIS);
		fullImageIS.close();
		return isPartOfFullImage;
	}

	/**
	 * Reset the screenshot counter.
	 */
	public void resetCounter() {
		// reset capture index
		this.captureIndex = 0;
	}

	/**
	 * Sets the driver for further usage.
	 * 
	 * @param driver
	 *            the {@link WebDriver} displaying the page.
	 */
	public void setDriver(final WebDriver driver) {
		this.fDriver = driver;
	}

	/**
	 * Simple way to check whether the screenshot functionality has been correctly set up. For this {
	 * {@link #setDriver(WebDriver)} should have been called.
	 * Ideally call {@link #initScreenshotStoreFolder(String)} beforehand to store screenshots in a well defined folder.
	 * 
	 * @return {@code true} if ready to capture screenshots.
	 */
	public boolean isReady() {
		return (this.fDriver != null);
	}
}
