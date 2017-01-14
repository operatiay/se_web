package com.ab.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.ab.selenium.pages.AbstractPage;

/**
 * A collection of widely used ExpectedConditions for WebElement.
 */
public abstract class ExpectedConditionFactory {

	/**
	 * Generates ExpectedCondition for element to be present.
	 * 
	 * @param finder
	 *            for the WebElement which has to be present
	 * @return ExpectedCondition which returns true if element present else false
	 */
	public static ExpectedCondition<Boolean> elementPresentCondition(final By finder) {
		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				return Boolean.valueOf(AbstractPage.waitForElement(finder,
						Long.valueOf((AbstractPage.WAIT_TIME_LIMIT).longValue() / 2), driver).isElementPresent());
			}

			@Override
			public String toString() {
				return String.format("Verifying that the element [%s] is present", finder);
			}
		};
		return condition;
	}
}
