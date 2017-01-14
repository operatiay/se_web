package com.ab.selenium.pages;
/**
 * Interface to 'create' pages.
 *
 * @param <T> resulting page class
 * @deprecated
 */
public interface IPageFactroy<T extends AbstractPage> {

	/**
	 * Creates page starting from parentPage.
	 * @param parentPage where to perform the necessary operations.
	 * @return new page
	 */
	T createPage(AbstractPage parentPage);

}
