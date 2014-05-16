package net.thecodemaster.evd.ui.view;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;

public class ViewDataModel {

	private int												typeVulnerability;
	private IResource									resource;
	private Expression								expr;
	private String										message;
	private String										fullPath;
	private int												lineNumber;

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

}
