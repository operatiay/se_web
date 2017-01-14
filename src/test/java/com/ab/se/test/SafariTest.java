package com.ab.se.test;

import org.testng.annotations.Test;

import com.ab.selenium.test.AbstractTest;

public class SafariTest extends AbstractTest {
	
	@Test
	public void testGoogle() throws InterruptedException {
		getDriver().wait(3000L);
		
	}

}
