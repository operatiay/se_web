package com.ab.selenium.structure;

import java.util.List;

/**
 * Used to store tree structure.
 * @param <K>
 */
public interface ITreeNode<K extends ITreeNode<K>> {

	/**
	 * Retrieve children of current node. An empty list should be returned if no children are present.
	 * 
	 * @return children nodes
	 */
	List<K> getChildren();

	/**
	 * Add parameter to the stored children and set current node to be parent of the parameter node.
	 * @param child to be added
	 */
	void addChild(K child);

	/**
	 * Get the name displayed in the tree.
	 * @return displayed name
	 */
	String getDisplayedName();

	/**
	 * Set the displayed name.
	 * @param displayedName displayed name
	 */
	void setDisplayedName(String displayedName);

	/**
	 * Are there any children under this node?
	 * @return presence of children
	 */
	boolean hasChildren();

	/**
	 * Get the parent node of current one.
	 * @return parent node
	 */
	K getParent();

	/**
	 * Set the parent node of current one.
	 * @param parent node
	 */
	void setParent(K parent);

	/**
	 * Get the last child in the children list.
	 * @return last child
	 */
	K getLastChild();

	/**
	 * Get the last added child node. Might be different from {@link #getLastChild()}.
	 * @return last added child node
	 */
	K getLastAddedNode();
}
