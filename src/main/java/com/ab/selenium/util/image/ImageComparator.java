package com.ab.selenium.util.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Compares images.
 */
public class ImageComparator {
	/**
	 * Image being the reference. This one should be found in multiple images, therefore it is stored in the instance.
	 */
	private BufferedImage refImage;

	/**
	 * Constructor for the comparator.
	 * 
	 * @param referenceImage
	 *            image to be looked for during comparison
	 * @throws IOException
	 *             if unable to read from the stream
	 */
	public ImageComparator(final InputStream referenceImage) throws IOException {
		this.refImage = normaliseImage(referenceImage);
	}

	/**
	 * Normalising the image by writing white background and setting the image to be an RGB without alpha.
	 * 
	 * @param imageStream
	 *            input image
	 * @return normalised image
	 * @throws IOException
	 *             if unable to read from the stream
	 */
	private static BufferedImage normaliseImage(final InputStream imageStream) throws IOException {
		BufferedImage image = ImageIO.read(imageStream);
		BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		imageRGB.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
		return imageRGB;
	}

	/**
	 * Tries to find the preset image within the input parameter image.
	 * 
	 * @param bigImageIS
	 *            big image to be evaluated.
	 * @return {@code true} if the preset image was found within the big image
	 * @throws IOException
	 *             if not able to read from stream
	 */
	public final boolean isPartOf(final InputStream bigImageIS) throws IOException {
		// find reference image on big image
		BufferedImage bigImage = normaliseImage(bigImageIS);
		int biRGB;
		boolean found = false;
		bi: for (int biy = 0; biy <= bigImage.getHeight() - this.refImage.getHeight(); biy++) {
			for (int bix = 0; bix <= bigImage.getWidth() - this.refImage.getWidth(); bix++) {
				found = false;
				ri: for (int riy = 0; riy < this.refImage.getHeight(); riy++) {
					for (int rix = 0; rix < this.refImage.getWidth(); rix++) {
						biRGB = bigImage.getRGB(bix + rix, biy + riy);
						if (biRGB != this.refImage.getRGB(rix, riy)) {
							found = false;
							break ri;
						}
						found = true;
						// go on searching for the rest of pixels
					}
				}
				if (found) {
					// all pixels from referenceImage have been found on the big one
					break bi;
				}
			}
		}
		return found;
	}
}
