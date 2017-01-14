package com.ab.selenium.test.evaluation;

import com.ab.selenium.pages.AbstractPage;

/**
 * Interface, which should be used to evaluate a result, which needs special logic to comprehend.
 * Such a result could be a message, which is visible to the user or a complete html page.<br>
 * 
 * An implementer is intended to be an{@code enum}, making it a threadsafe and serializable singleton.<br>
 * An example code:<blockquote><pre>{@code <b>public enum</b> MyEvaluator <b>implements</b> ResultEvaluator{
 *        INSTANCE;
 *
 *        private MyEvaluator() {
 *        }
 *
 *        public static MyEvaluator getInstance()
 *
 *        <b>public void</b> evaluateCallResult(WebDriver driver) {
 *             ...
 *        }
 *    }</pre></blockquote>
 *
 */
public interface IResultEvaluator {

	/**
	 * Evaluate the parameter using {@code assert} evaluations.
	 * @param page {@link AbstractPage} page which needs to be evaluated
	 */
	void evaluateCallResult(AbstractPage page);
}
