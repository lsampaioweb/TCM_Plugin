package net.thecodemaster.esvd.graph;

import org.eclipse.jdt.core.dom.ASTNode;

public class VariableReferenceElement {

	private ASTNode	reference;
	private int			contextId;

	public VariableReferenceElement(ASTNode reference, int contextId) {
		setReference(reference);
		setContextId(contextId);
	}

	public ASTNode getReference() {
		return reference;
	}

	public void setReference(ASTNode reference) {
		this.reference = reference;
	}

	public int getContextId() {
		return contextId;
	}

	public void setContextId(int contextId) {
		this.contextId = contextId;
	}

}
