package com.ab.selenium.structure;

/**
 * Interface for a table row.
 * @param <K> class extending {@link ITableRow}. This makes returning types for methods specific for the implementer.
 */
public interface ITableRow<K extends ITableRow<K>> {

	/**
	 * Get text visible for user.
	 * @return visible text.
	 */
	String getText();

	/**
	 * Click on the appropriate part of the row to select it.
	 */
	void select();

	/**
	 * Checks whether the row is selected.
	 * @return {@code true} if selected
	 */
	boolean isSelected();

	/**
	 * Make sure that the next row is visible (may require scrolling the table).
	 * @return next row in the table
	 * @throws IllegalAccessError
	 */
	K getNextRow() throws IllegalAccessError;

	/**
	 * Get the row position in the table.
	 * @return position in the table
	 */
	int getTablePositionCounter();
}
