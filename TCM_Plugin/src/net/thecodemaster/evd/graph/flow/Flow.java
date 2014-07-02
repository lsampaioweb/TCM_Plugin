package net.thecodemaster.evd.graph.flow;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.jdt.core.dom.Expression;

public class Flow {

	private final Expression	root;
	private Flow							parent;
	private final List<Flow>	children;
	private boolean						hasLoop;

	public Flow(Expression root) {
		this.root = root;
		children = Creator.newList();
		hasLoop = false;
	}

	private Flow(Expression root, Flow parent) {
		this(root);
		this.parent = parent;
	}

	private Expression getRoot() {
		return root;
	}

	private Flow getParent() {
		return parent;
	}

	public Flow addChild(Expression node) {
		Flow flow;

		// If there is nothing to add or the new value is the same as the present value.
		if ((null == node) || (getRoot().equals(node))) {
			flow = this;
		} else {
			flow = new Flow(node, this);
			children.add(flow);

			setLoop(false);
			checkIfHasLoop(node);
		}

		return flow;
	}

	private void checkIfHasLoop(Expression node) {
		Flow current = this;

		while (null != current) {
			if ((null != node) && (node.equals(current.getRoot()))) {
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

}
