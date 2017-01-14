package com.ab.selenium.pages;

import org.openqa.selenium.WebDriver;

/**
 * Workflow is a process through multiple pages which results in returning to the page prior to the workflow. This page
 * is the &lt;StartPage>.
 * 
 * @param <StartPage>
 *            class of the parent page.
 */
public abstract class AWorkflowPage<StartPage extends AbstractPage> extends AbstractPage {

	public AWorkflowPage(StartPage parentPage) {
		super(parentPage);
	}

	public AWorkflowPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * @return class for parentPage
	 */
	@SuppressWarnings("unchecked")
	public Class<StartPage> getStartPageClass() {
		return (Class<StartPage>) this.getParentPage().getClass();
	}
}
