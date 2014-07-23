package net.thecodemaster.esvd.finder;

import java.util.List;

import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.graph.CodeVisitor;
import net.thecodemaster.esvd.graph.VariableBinding;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

public class InstanceFinder extends CodeVisitor {

	private final List<Expression>	references;

	public InstanceFinder(CallGraph callGraph, IResource currentResource) {
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

	public Expression getReference(Flow loopControl, Context context, Expression initializer) {
		// 01 - To make sure if this method is invoked more that once, we will not use old values.
		getReferences().clear();

		// 02 - Start the detection on each and every line of this method.
		inspectNode(loopControl, context, new DataFlow(initializer), initializer);

		// 03 - Return the found reference (if any).
		return (!getReferences().isEmpty()) ? getReferences().get(0) : null;
	}

	@Override
	protected Flow addElementToLoopControl(Flow loopControl, ASTNode node) {
		switch (node.getNodeType()) {
			case ASTNode.SIMPLE_NAME: // 42
				loopControl = loopControl.addChild(node);
		}

		return super.addElementToLoopControl(loopControl, node);
	}

	/**
	 * 14
	 */
	@Override
	protected void inspectClassInstanceCreation(Flow loopControl, Context context, DataFlow dataFlow,
			ClassInstanceCreation node) {
		Expression instance = BindingResolver.getInstanceIfItIsAnObject(node);
		if (null != instance) {
			addReference(instance);
		}
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(Flow loopControl, Context context, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBinding variableBinding = getCallGraph().getVariableBinding(context, expression);

		if (null != variableBinding) {
			// 02 - Check if this instance has a context.
			Context instanceContext = getCallGraph().getInstanceContext(context, expression);

			// 03 - If the context is equal it means it does not exist.
			if (!instanceContext.equals(context)) {
				addReference(expression);
				return;
			}
			// 01 - This is the case where we have to go deeper into the variable's path.
			inspectNode(loopControl, context, dataFlow, variableBinding.getInitializer());
		}
	}

}
