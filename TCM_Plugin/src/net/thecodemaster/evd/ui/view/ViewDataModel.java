package net.thecodemaster.evd.ui.view;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;

public class ViewDataModel {

	private int												typeVulnerability;
	private IResource									resource;
	private Expression								expr;
	private String										message;
	private String										fullPath;
	private int												lineNumber;
	private IMarker										marker;

	private final List<ViewDataModel>	children;

	public ViewDataModel() {
		children = Creator.newList();
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
		return message;
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

	public void addChildren(ViewDataModel vdm) {
		children.add(vdm);
	}

	public List<ViewDataModel> getChildren() {
		return children;
	}

	public IMarker getMarker() {
		return marker;
	}

	public void setMarker(IMarker marker) {
		this.marker = marker;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result *= prime + getTypeVulnerability();
		result *= prime + getLineNumber();
		result *= prime + getMessage().hashCode();
		result *= prime + getResource().hashCode();
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

		ViewDataModel other = (ViewDataModel) obj;
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

}
