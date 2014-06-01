package net.thecodemaster.evd.graph;

import java.util.Iterator;
import java.util.List;

import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

/**
 * This class is responsible to hold the complete flow of a vulnerability starting where the vulnerability was
 * introduced until where it was exploited.
 * 
 * @author Luciano Sampaio
 */
public class DataFlow {

	/**
	 * The object that is vulnerable.
	 */
	private Expression									root;
	/**
	 * The parent object because we need to navigate from the parent to its children and also on the opposite direction.
	 */
	private DataFlow										parent;
	/**
	 * The type of the vulnerability.
	 */
	private int													typeProblem;
	/**
	 * The message that will be displayed to the user informing that this object is vulnerable.
	 */
	private String											message;
	/**
	 * All the possible flows that the vulnerability could reach, but some of them might end up being not vulnerable.
	 */
	private List<DataFlow>							children;
	/**
	 * This list holds that actual paths that ARE vulnerable.
	 */
	private final List<List<DataFlow>>	allVulnerablePaths;

	public DataFlow() {
		children = Creator.newList();
		allVulnerablePaths = Creator.newList();
	}

	public DataFlow(Expression root) {
		this();
		this.root = root;
	}

	private DataFlow(Expression root, DataFlow parent) {
		this(root);
		this.parent = parent;
	}

	public Expression getRoot() {
		return root;
	}

	public int getTypeProblem() {
		return typeProblem;
	}

	private void setTypeProblem(int typeProblem) {
		this.typeProblem = typeProblem;
	}

	public String getMessage() {
		return message;
	}

	private void setMessage(String message) {
		this.message = message;
	}

	public List<List<DataFlow>> getAllVulnerablePaths() {
		return allVulnerablePaths;
	}

	public DataFlow addNodeToPath(Expression node) {
		DataFlow nvp;
		if (null == root) {
			root = node;
			nvp = this;
		} else if (root.equals(node)) {
			nvp = this;
		} else {
			nvp = new DataFlow(node, this);
			children.add(nvp);
		}

		return nvp;
	}

	public void isVulnerable(int typeProblem, String message) {
		isVulnerable(null);

		setTypeProblem(typeProblem);
		setMessage(message);
	}

	/**
	 * This method informs to the parent object that a vulnerable path was found.
	 */
	private void isVulnerable(List<DataFlow> childrenList) {
		List<DataFlow> currentList = Creator.newList();
		currentList.add(this);
		if (null != childrenList) {
			currentList.addAll(childrenList);
		}

		allVulnerablePaths.add(currentList);
		if (null != parent) {
			parent.isVulnerable(currentList);
		}

	}

	public boolean isVulnerable() {
		return !allVulnerablePaths.isEmpty();
	}

	public void replace(DataFlow dataFlow) {
		typeProblem = dataFlow.typeProblem;
		message = dataFlow.message;
		children = dataFlow.children;

		if (!dataFlow.allVulnerablePaths.isEmpty()) {
			// Inform the parent that this path is vulnerable.
			for (Iterator<List<DataFlow>> iterator = dataFlow.allVulnerablePaths.iterator(); iterator.hasNext();) {
				List<DataFlow> copyList = Creator.newList(iterator.next());
				// This element will be re-added on the currentList.add(this);
				copyList.remove(0);
				isVulnerable(copyList);
			}
		}
	}

	public void isInfinitiveLoop(Expression expr) {
		PluginLogger.logIfDebugging("Found an Infinitive Loop at expression: " + expr);
	}

	public void isInfinitiveLoop(Statement statement) {
		PluginLogger.logIfDebugging("Found an Infinitive Loop at statement: " + statement);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getRoot().hashCode();
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
		if (!getRoot().equals(other.getRoot())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return getRoot().toString();
	}

}
