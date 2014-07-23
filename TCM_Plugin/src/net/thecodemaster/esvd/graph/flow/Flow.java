package net.thecodemaster.esvd.graph.flow;

import java.util.List;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.jdt.core.dom.ASTNode;

public class Flow {

	private final ASTNode			root;
	private Flow							parent;
	private final List<Flow>	children;
	private boolean						hasLoop;

	public Flow(ASTNode root) {
		this.root = root;
		children = Creator.newList();
		hasLoop = false;
	}

	private Flow(ASTNode root, Flow parent) {
		this(root);
		this.parent = parent;
	}

	public ASTNode getRoot() {
		return root;
	}

	private Flow getParent() {
		return parent;
	}

	public Flow addChild(ASTNode node) {
		Flow flow;

		// If there is nothing to add or the new value is the same as the present value.
		if (null == node) {
			flow = this;
		} else {
			flow = new Flow(node, this);
			children.add(flow);

			setLoop(false);
			checkIfHasLoop(node);
		}

		return flow;
	}

	private void checkIfHasLoop(ASTNode node) {
		Flow current = this;

		while (null != current) {
			// (node.equals(current.getRoot()))) // We have to verify if they are the same instance.
			if ((null != node) && (node == current.getRoot())) {
				setLoop(true);
				break;
			}

			current = current.getParent();
		}
	}

	public void setLoop(boolean hasloop) {
		this.hasLoop = hasloop;
	}

	public boolean hasLoop() {
		if (hasLoop) {
			return hasLoop;
		}

		Flow parent = getParent();

		if (null != parent) {
			return parent.hasLoop();
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result += prime * (null != getRoot() ? getRoot().hashCode() : 1);
		result += prime * (null != getParent() ? getParent().hashCode() : 1);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (null == obj) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		DataFlow other = (DataFlow) obj;
		if ((null != getRoot()) && (getRoot().equals(other.getRoot()))) {
			if ((null != getParent()) && (getParent().equals(other.getParent()))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		String root = (null != getRoot()) ? getRoot().toString() : "";
		String parent = (null != getParent()) ? getParent().toString() : "";
		return String.format("%s - %s", parent, root);
	}

	public String getFullPath(List<String> fullPathAlreadyAdded) {
		// 01 - The first element is the parent.
		Flow currentFlow = this;
		List<String> fullPath = Creator.newList();

		while (null != currentFlow) {
			ASTNode root = currentFlow.getRoot();

			if (null != root) {
				fullPath.add(0, root.toString());
			}

			currentFlow = currentFlow.getParent();
		}

		StringBuilder sb = new StringBuilder();
		for (String value : fullPath) {

			boolean shouldAddElement = true;
			// If this element is already added to the full path. We can stop, the next elements will be repeated ones.
			for (String alreadyAdded : fullPathAlreadyAdded) {
				if (value.equals(alreadyAdded)) {
					shouldAddElement = false;
					break;
				}
			}

			if (shouldAddElement) {
				if (0 != sb.length()) {
					sb.append(Constant.SEPARATOR_FULL_PATH);
				}
				sb.append(value);
			} else {
				break;
			}
		}
		return sb.toString();
	}

}
