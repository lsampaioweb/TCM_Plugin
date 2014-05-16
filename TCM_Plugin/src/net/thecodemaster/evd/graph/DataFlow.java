package net.thecodemaster.evd.graph;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

/**
 * @author Luciano Sampaio
 */
public class DataFlow {

	private final Expression										root;
	private DataFlow										parent;
	private final List<DataFlow>				children;
	private String															message;

	private final List<List<DataFlow>>	allVulnerablePaths;

	private DataFlow(Expression root, DataFlow parent) {
		this(root);
		this.parent = parent;
	}

	public DataFlow(Expression root) {
		this.root = root;
		children = Creator.newList();
		allVulnerablePaths = Creator.newList();
	}

	public Expression getRoot() {
		return root;
	}

	public String getMessage() {
		return message;
	}

	public List<List<DataFlow>> getAllVulnerablePaths() {
		return allVulnerablePaths;
	}

	public DataFlow addNodeToPath(Expression node) {
		DataFlow nvp = new DataFlow(node, this);
		children.add(nvp);

		return nvp;
	}

	public void isVulnerable(Expression expr, String message) {
		isVulnerable(null);

		this.message = message;
	}

	/**
	 * This method set the foundVulnerability to true on the parent's path.
	 */
	private void isVulnerable(List<DataFlow> childrenList) {
		List<DataFlow> currentList = Creator.newList();
		currentList.add(this);
		if (null != childrenList) {
			currentList.addAll(childrenList);
		}

		if (null != parent) {
			parent.isVulnerable(currentList);
		} else {
			allVulnerablePaths.add(currentList);
		}

	}

	public void foundInfinitiveLoop(Expression expr) {
		PluginLogger.logIfDebugging("Found an Infinitive Loop at expression: " + expr);
	}

	public void foundInfinitiveLoop(Statement statement) {
		PluginLogger.logIfDebugging("Found an Infinitive Loop at statement: " + statement);
	}

	public boolean isEmpty() {
		return allVulnerablePaths.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getRoot().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param obj
	 *          Object
	 * @return boolean
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
		if (!getRoot().equals(other.getRoot())) {
			return false;
		}

		return true;
	}

}
