package net.thecodemaster.esvd.graph;

import java.util.List;

import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.jdt.core.dom.ASTNode;

public class ReferenceManager {

	private final List<VariableReferenceElement>	references;

	public ReferenceManager() {
		references = Creator.newList();
	}

	public void add(ASTNode reference, int contextId) {
		references.add(new VariableReferenceElement(reference, contextId));
	}

	public boolean hasReference(ASTNode reference, int contextId) {
		for (VariableReferenceElement current : references) {
			if ((current.getReference().equals(reference)) && (current.getContextId() == contextId)) {
				return true;
			}
		}

		return false;
	}

}
