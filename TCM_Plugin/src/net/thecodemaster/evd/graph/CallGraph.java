package net.thecodemaster.evd.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At any
 * given time, we should only have one call graph of the code.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class CallGraph {

	private final Map<IResource, Context>	contexts;

	public CallGraph() {
		contexts = Creator.newMap();
	}

	private Map<IResource, Context> getContexts() {
		return contexts;
	}

	private Context getContext(IResource resource) {
		// 01 - Get the context of the provided resource.
		Context context = getContexts().get(resource);

		// 02 - It means this resource does not have a context yet.
		if (null == context) {
			// 02.1 - Create a new context.
			context = new Context(resource);

			// 02.2 - Add it to the list.
			getContexts().put(resource, context);
		}

		return context;
	}

	private VariableBinding createVariableBinding(SimpleName variableName, Expression initializer) {
		return new VariableBinding(resolveBinding(variableName), initializer);
	}

	public IBinding resolveBinding(ASTNode node) {
		return BindingResolver.resolveBinding(node);
	}

	public void addFieldDeclaration(IResource resource, SimpleName fieldName, Expression initializer) {
		getContext(resource).addVariable(createVariableBinding(fieldName, initializer));
	}

	public void addMethodDeclaration(IResource resource, MethodDeclaration method) {
		getContext(resource).addMethodDeclaration(method);
	}

	public void addMethodInvocation(IResource resource, MethodDeclaration caller, Expression callee) {
		getContext(resource).addMethodInvocation(caller, callee);
	}

	public void remove(IResource resource) {
		getContexts().remove(resource);
	}

	public Map<MethodDeclaration, List<Expression>> getMethods(IResource resource) {
		return getContext(resource).getMethods();
	}

	public Map<MethodDeclaration, List<Expression>> getInvokers(MethodDeclaration methodToSearch) {
		Map<MethodDeclaration, List<Expression>> invokers = Creator.newMap();

		// 01 - Iterate over all the contexts and check which methods invoke the provided method.
		for (Context context : getContexts().values()) {

			// 02 - Get the list of methods of this resource.
			Map<MethodDeclaration, List<Expression>> methods = context.getMethods();

			// 03 - Iterate over each method into the map.
			for (Entry<MethodDeclaration, List<Expression>> currentMethod : methods.entrySet()) {

				// 04 - Iterate over each method invocation of the current method.
				for (Expression invocation : currentMethod.getValue()) {

					// 05 - Verify if these methods are the same.
					if (BindingResolver.areMethodsEqual(methodToSearch, invocation)) {
						if (!invokers.containsKey(currentMethod.getKey())) {
							List<Expression> invocations = Creator.newList();

							// Create a empty list of method invocations.
							invokers.put(currentMethod.getKey(), invocations);
						}

						// 06 - This method should be processed, add it to the list.
						invokers.get(currentMethod.getKey()).add(invocation);
					}
				}
			}
		}

		return invokers;
	}

	public Context newContext(IResource resource, MethodDeclaration method, Expression invoker) {
		// 01 - Get the top level (the class) parent context.
		Context parentContext = getContext(resource);

		// 02 - Create a context.
		Context context = new Context(resource, parentContext);

		// 03 - Set the object that holds the reference to this method declaration.
		context.addMethodDeclaration(method);

		// 04 - Set the invoker of this method.
		context.setInvoker(invoker);

		// 05 - Add this new context as a child of the parent context.
		parentContext.addChildContext(context);

		return context;
	}

}
