package com.ab.selenium.structure;

/**
 * Interface for a table.
 * 
 * @param <Row>
 */
public interface ITable<Row extends ITableRow<Row>> {

	/**
	 * Select a row in the table using the parameter.
	 * 
	 * @param rowText
	 *            text visible for the user
	 * @return row having the text
	 */
	Row selectByText(String rowText);
}
