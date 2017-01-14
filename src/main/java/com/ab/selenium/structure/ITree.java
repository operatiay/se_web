package com.ab.selenium.structure;

/**
 * Interface for a tree.
 * 
 * @param <R>
 *            tree node details (one row in the tree)
 * @param <K>
 *            tree node representation (with substructure)
 */
public interface ITree<K extends ITreeNode<K>, R extends ITreeRow> {

	/**
	 * Select the element in the tree, which is found using the path defined by parameter(s).
	 * 
	 * @param elementNames
	 *            names of the tree elements, every next one being the child of the previous.
	 * @return selected element representation (row).
	 */
	R selectByPath(String... elementNames);

	/**
	 * Go to the tree element specified by parameter and gather everything, which is located under it.
	 * 
	 * @param elementNames
	 *            names of elements defining the path
	 * @return list of {@link ITreeNode} entries
	 */
	K getTreeStructureFromPath(String... elementNames);

	/**
	 * Gather the substructure of previously selected node.
	 * 
	 * @return substructure starting by the selected node.
	 * @throws IllegalStateException
	 *             if nothing has been selected
	 */
	K getTreeStructureFromSelection() throws IllegalStateException;

	/**
	 * Deliver the total number of rows, which can be viewed, even if scrolling is necessary. Expanding/collapsing inner
	 * nodes of the tree is going to change the returned value.
	 * 
	 * @return number of expanded rows
	 */
	int getExpandedRowsCount();

}
