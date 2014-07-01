package net.thecodemaster.evd.finder;

import java.util.List;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.CodeVisitor;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

public class ReferenceFinder extends CodeVisitor {

	private final List<Expression>	references;

	public ReferenceFinder(CallGraph callGraph, IResource currentResource) {
		references = Creator.newList();

		setCallGraph(callGraph);
		setCurrentResource(currentResource);
	}

	private void addReference(Expression reference) {
		this.references.add(reference);
	}

	private List<Expression> getReferences() {
		return references;
	}

	public Expression getReference(int depth, Context context, Expression initializer) {
		// 01 - To make sure if this method is invoked more that once, we will not use old values.
		getReferences().clear();

		// 02 - Start the detection on each and every line of this method.
		inspectNode(depth, context, new DataFlow(initializer), initializer);

		// 03 - Return the found reference (if any).
		return (!getReferences().isEmpty()) ? getReferences().get(0) : null;
	}

	/**
	 * 14
	 */
	@Override
	protected void inspectClassInstanceCreation(int depth, Context context, DataFlow dataFlow, ClassInstanceCreation node) {
		addReference(node);
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(int depth, Context context, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBinding variableBinding = getCallGraph().getVariableBinding(context, expression);

		if (null != variableBinding) {
			// 02 - This is the case where we have to go deeper into the variable's path.
			inspectNode(depth, context, dataFlow, variableBinding.getInitializer());
		}
	}

}
