package net.thecodemaster.esvd.graph.flow;

import java.util.Iterator;
import java.util.List;

import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.jdt.core.dom.Expression;

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
	private final Expression						root;
	/**
	 * The parent object because we need to navigate from the parent to its children and also on the opposite direction.
	 */
	private DataFlow										parent;
	/**
	 * The priority of the vulnerability.
	 */
	private int													priority;
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
	private Flow												fullPath;

	public DataFlow(Expression root) {
		this.root = root;
		children = Creator.newList();
		allVulnerablePaths = Creator.newList();
	}

	private DataFlow(Expression root, DataFlow parent) {
		this(root);
		this.parent = parent;
	}

	public Expression getRoot() {
		return root;
	}

	public DataFlow getParent() {
		return parent;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getTypeProblem() {
		return typeProblem;
	}

	public void setTypeProblem(int typeProblem) {
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

	public Flow getFullPath() {
		return fullPath;
	}

	public void setFullPath(Flow fullPath) {
		this.fullPath = fullPath;
	}

	public DataFlow addNodeToPath(Expression node) {
		DataFlow nvp;

		// If there is nothing to add or the new value is the same as the present value.
		if ((null == node) || (getRoot().equals(node))) {
			nvp = this;
		} else {
			nvp = new DataFlow(node, this);
			children.add(nvp);
		}

		return nvp;
	}

	public void hasVulnerablePath(int typeProblem, String message) {
		hasVulnerablePath(null);

		setTypeProblem(typeProblem);
		setMessage(message);
	}

	/**
	 * This method informs to the parent object that a vulnerable path was found.
	 */
	private void hasVulnerablePath(List<DataFlow> childrenList) {
		List<DataFlow> currentList = Creator.newList();
		currentList.add(this);
		if (null != childrenList) {
			currentList.addAll(childrenList);
		}

		allVulnerablePaths.add(currentList);
		if (null != getParent()) {
			getParent().hasVulnerablePath(currentList);
		}

	}

	public boolean hasVulnerablePath() {
		return !allVulnerablePaths.isEmpty();
	}

	public void replace(DataFlow dataFlow) {
		if (null != dataFlow) {
			typeProblem = dataFlow.typeProblem;
			message = dataFlow.message;
			children = dataFlow.children;

			if (!dataFlow.allVulnerablePaths.isEmpty()) {

				List<List<DataFlow>> copyVulnerablePaths = Creator.newList(dataFlow.allVulnerablePaths);

				// Inform the parent that this path is vulnerable.
				for (Iterator<List<DataFlow>> iterator = copyVulnerablePaths.iterator(); iterator.hasNext();) {
					List<DataFlow> copyList = Creator.newList(iterator.next());
					// This element will be re-added on the currentList.add(this);
					copyList.remove(0);
					hasVulnerablePath(copyList);
				}
			}
		}
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
