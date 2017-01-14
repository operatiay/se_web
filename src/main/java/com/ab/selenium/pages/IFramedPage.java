package com.ab.selenium.pages;

/**
 * Define the functionality special for a page containing IFrames.
 * @author abilyak
 */
public interface IFramedPage {
	/**
	 * Set the correct iFrame (content) as receiver for driver commands. This
	 * method should take care of the right iFrame for the method calls. Each
	 * page should know, which iFrame it belongs to and also take care of the
	 * calls of this method.The intended implementation would be:<br>
	 * {@code // clear}<br>
	 * {@code getDriver().switchTo().defaultContent();}<br>
	 * {@code // set to the right iFrame}<br>
	 * {@code switchToFrame(...);}<br>
	 */
	void resetActiveIFrame();
}
