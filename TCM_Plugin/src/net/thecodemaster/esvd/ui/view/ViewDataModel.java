package net.thecodemaster.esvd.ui.view;

import java.util.List;

import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.helper.HelperViewDataModel;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Each instance of this class represents a single line into the Security Vulnerability View.
 * 
 * @author Luciano Sampaio
 */
public class ViewDataModel {

	private int												priority;
	private int												typeVulnerability;
	private IResource									resource;
	private Expression								expr;
	private String										message;
	private String										fullPath;
	private int												lineNumber;
	private IMarker										marker;

	/**
	 * If this object is not a exit point, then it has a parent node.
	 */
	private ViewDataModel							parent;
	/**
	 * A vulnerability might have several possible vulnerable paths, so this list contains all these paths.
	 */
	private final List<ViewDataModel>	children;

	public ViewDataModel() {
		children = Creator.newList();
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getTypeVulnerability() {
		return typeVulnerability;
	}

	public void setTypeVulnerability(int typeVulnerability) {
		this.typeVulnerability = typeVulnerability;
	}

	public IResource getResource() {
		return resource;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public Expression getExpr() {
		return expr;
	}

	public void setExpr(Expression expr) {
		this.expr = expr;
	}

	public String getMessage() {
		if (null == message) {
			message = getMessageByNumberOfVulnerablePaths();
		}

		return message;
	}

	public void updateMessage() {
		setMessage(null);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public IMarker getMarker() {
		return marker;
	}

	public void setMarker(IMarker marker) {
		this.marker = marker;
	}

	public void addChildren(ViewDataModel vdm) {
		vdm.addParent(this);
		children.add(vdm);
	}

	public List<ViewDataModel> getChildren() {
		return children;
	}

	private void addParent(ViewDataModel parent) {
		this.parent = parent;
	}

	public ViewDataModel getParent() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result *= prime + getPriority();
		result *= prime + getTypeVulnerability();
		result *= prime + getLineNumber();
		result *= prime + getMessage().hashCode();
		result *= prime + getResource().hashCode();
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

		ViewDataModel other = (ViewDataModel) obj;
		if (getPriority() != other.getPriority()) {
			return false;
		}
		if (getTypeVulnerability() != other.getTypeVulnerability()) {
			return false;
		}
		if (getLineNumber() != other.getLineNumber()) {
			return false;
		}
		if (!getMessage().equals(other.getMessage())) {
			return false;
		}
		if (!getResource().equals(other.getResource())) {
			return false;
		}

		return true;
	}

	public List<ViewDataModel> getBy(IMarker marker) {
		List<ViewDataModel> matches = Creator.newList();

		if ((null != getMarker()) && (getMarker().equals(marker))) {
			matches.add(this);
			return matches;
		}

		for (ViewDataModel vdm : getChildren()) {
			List<ViewDataModel> current = vdm.getBy(marker);
			if (0 < current.size()) {
				matches.addAll(current);
			}
		}

		return matches;
	}

	public void removeMarker(boolean removeChildren) throws CoreException {
		if (null != getMarker()) {
			getMarker().delete();
		}

		if (removeChildren) {
			for (ViewDataModel vdm : getChildren()) {
				vdm.removeMarker(removeChildren);
			}
		}
	}

	public void removeChildren(List<ViewDataModel> childrenToRemove, boolean removeChildren) {
		if (getChildren().containsAll(childrenToRemove)) {
			getChildren().removeAll(childrenToRemove);
			return;
		}

		getChildren().removeAll(childrenToRemove);

		if (removeChildren) {
			// It will iterate only on the children that were not removed.
			for (ViewDataModel vdm : getChildren()) {
				if (vdm.getExpr().equals(childrenToRemove.get(0).getParent().getExpr())) {
					vdm.removeChildren(childrenToRemove, removeChildren);
				}
			}
		}
	}

	private String getMessageByNumberOfVulnerablePaths() {
		return HelperViewDataModel.getMessageByNumberOfVulnerablePaths(getExpr().toString(), getChildren().size());
	}

	@Override
	public String toString() {
		return String.format("%s - %s", getExpr(), getFullPath());
	}

}
