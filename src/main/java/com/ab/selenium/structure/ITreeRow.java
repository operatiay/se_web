package com.ab.selenium.structure;

/**
 * Interface for a tree row.
 */
public interface ITreeRow extends ITableRow<ITreeRow> {
	/**
	 * Expand the tree on this row.
	 */
	void expandTree();

	/**
	 * Is the row already expanded (children are visible)?
	 * 
	 * @return {@code true} if children are visible
	 */
	boolean isExpanded();

	/**
	 * Is the row a leaf (not expandable)?
	 * 
	 * @return {@code true} if row is a leaf.
	 */
	boolean isLeaf();

}
