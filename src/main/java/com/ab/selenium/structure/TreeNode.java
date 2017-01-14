package com.ab.selenium.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Node from the tree.
 * 
 */
public class TreeNode implements ITreeNode<TreeNode> {

	/** List of children nodes. */
	private List<TreeNode> children;
	/** Parent node. */
	private TreeNode fParent;
	/** Displayed name. */
	private String fDisplayedName;

	/** String used to symbolise node depth in toString(). */
	private static final char PADDING_SYMBOL = '-';

	@Override
	public final void addChild(final TreeNode child) {
		if (this.children == null) {
			this.children = new ArrayList<TreeNode>();
		}
		child.setParent(this);
		this.children.add(child);
	}

	@Override
	public final List<TreeNode> getChildren() {
		if (this.children == null) {
			return Collections.emptyList();
		}
		return this.children;
	}

	@Override
	public final String getDisplayedName() {
		return this.fDisplayedName;
	}

	@Override
	public final void setDisplayedName(final String displayedName) {
		this.fDisplayedName = displayedName;
	}

	@Override
	public final TreeNode getParent() {
		return this.fParent;
	}

	@Override
	public final boolean hasChildren() {
		return (this.children != null && this.children.size() > 0);
	}

	@Override
	public final void setParent(final TreeNode parent) {
		this.fParent = parent;
	}

	@Override
	public final String toString() {
		int paddingDepth = 1;
		return toStringWithFormatting(paddingDepth);
	}

	/**
	 * Format the output.
	 * 
	 * @param paddingDepth
	 *            initially should be 0, then used recursively to add children representations.
	 * @return string representation of the node.
	 */
	private String toStringWithFormatting(final int paddingDepth) {
		StringBuffer sb = new StringBuffer();
		char[] paddingArray = new char[paddingDepth];
		Arrays.fill(paddingArray, PADDING_SYMBOL);
		String padding = new String(paddingArray);
		sb.append(getDisplayedName());
		sb.append('\n');
		if (hasChildren()) {
			for (TreeNode child : getChildren()) {
				sb.append(padding);
				sb.append(child.toStringWithFormatting(paddingDepth + 1));
			}
		}
		return sb.toString();
	}

	@Override
	public final TreeNode getLastChild() {
		TreeNode lastChild = null;
		if (hasChildren()) {
			lastChild = getChildren().get(getChildren().size() - 1);
		}
		return lastChild;
	}

	@Override
	public final TreeNode getLastAddedNode() {
		TreeNode lastAddedNode = null;
		TreeNode lastChild = getLastChild();
		while (lastChild != null) {
			lastAddedNode = lastChild;
			lastChild = lastChild.getLastChild();
		}
		return lastAddedNode;
	}

}
