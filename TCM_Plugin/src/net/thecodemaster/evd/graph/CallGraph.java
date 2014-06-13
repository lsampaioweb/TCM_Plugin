package net.thecodemaster.evd.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.ui.enumeration.EnumVariableType;

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

	/**
	 * @param resource
	 * @return
	 */
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

	/**
	 * @param resource
	 * @param method
	 * @param invoker
	 * @return
	 */
	public Context newContext(IResource resource, MethodDeclaration method, Expression invoker) {
		return newContext(getContext(resource), method, invoker);
	}

	/**
	 * @param parentContext
	 * @param method
	 * @param invoker
	 * @return
	 */
	public Context newContext(Context parentContext, MethodDeclaration method, Expression invoker) {
		// 02 - Create a context.
		Context context = new Context(parentContext.getResource(), parentContext);

		// 03 - Set the object that holds the reference to this method declaration.
		context.addMethodDeclaration(method);

		// 04 - Set the invoker of this method.
		context.setInvoker(invoker);

		// 05 - Add this new context as a child of the parent context.
		parentContext.addChildContext(context);

		return context;
	}

	/**
	 * @param resource
	 */
	public void remove(IResource resource) {
		getContexts().remove(resource);
	}

	/** Variables */
	/**
	 * @param variableName
	 * @param type
	 * @param initializer
	 * @return
	 */
	private VariableBinding createVariableBinding(SimpleName variableName, Expression initializer) {
		return new VariableBinding(resolveBinding(variableName), initializer);
	}

	/**
	 * @param node
	 * @return
	 */
	public IBinding resolveBinding(ASTNode node) {
		return BindingResolver.resolveBinding(node);
	}

	/**
	 * @param resource
	 * @param fieldName
	 * @param initializer
	 */
	public void addFieldDeclaration(IResource resource, SimpleName fieldName, Expression initializer) {
		addVariable(getContext(resource), fieldName, initializer);
	}

	/**
	 * @param context
	 * @param variableName
	 * @param initializer
	 * @return
	 */
	public VariableBinding addVariable(Context context, SimpleName variableName, Expression initializer) {
		return context.addVariable(createVariableBinding(variableName, initializer));
	}

	/**
	 * @param context
	 * @param variable
	 * @return
	 */
	public VariableBinding getVariableBinding(Context context, Expression variable) {
		// This variable might belong to the provided context or to another one.
		// We first test on the local context if we do not find a match we go up.
		IBinding binding = resolveBinding(variable);

		// 01 - Get the list of occurrences of this variable.
		List<VariableBinding> vbs = getVariableBindings(context, binding);

		if (vbs.size() > 0) {
			// 02 - Get a parent expression which has a reference to this variable.
			Expression expression = BindingResolver.getParentWhoHasAReference(variable);
			if (null != expression) {

				// 03 - Get the variable binding if the expression matches one of the references of this variable.
				VariableBinding variableBinding = getVariableBindingIfReferenceMatch(vbs, expression);
				if (null != variableBinding) {
					return variableBinding;
				}

				if (vbs.get(0).getType() == EnumVariableType.GLOBAL) {
					// 04 - Get the variable binding if the expression matches one of the
					// references of this variable in another context.
					vbs = getVariableBindingsFromAllContexts(context, binding);

					variableBinding = getVariableBindingIfReferenceMatch(vbs, expression);
					if (null != variableBinding) {
						return variableBinding;
					}
				}
			}

			return getLastReference(vbs);
		}

		return null;
	}

	/**
	 * @param vbs
	 * @param expression
	 * @return
	 */
	private VariableBinding getVariableBindingIfReferenceMatch(List<VariableBinding> vbs, Expression expression) {
		for (VariableBinding variableBinding : vbs) {
			for (Expression currentReference : variableBinding.getReferences()) {
				if (currentReference.equals(expression)) {
					return variableBinding;
				}
			}
		}
		return null;
	}

	/**
	 * @param context
	 * @param variable
	 * @return
	 */
	public VariableBinding getLastReference(Context context, Expression variable) {
		// 01 - Get the binding of this variable.
		// 02 - Get the list of occurrences of this variable.
		// 03 - Return the last element of the list.
		return getLastReference(getVariableBindings(context, resolveBinding(variable)));
	}

	/**
	 * @param context
	 * @param binding
	 * @return
	 */
	private List<VariableBinding> getVariableBindings(Context context, IBinding binding) {
		// We have three cases here.
		// Case 01: This variable belongs to the provided context.
		// Case 02: This variable belongs to a parent context (Employee extends Person).
		// Case 03: This variable belongs to another file (AnotherClass.variableName).

		// 01 - Try to find the variable into the provided context.
		List<VariableBinding> vbs = getVariableBindingsFromContext(context, binding);

		if (0 >= vbs.size()) {
			// 02 - Try to find the variable into a parent context (Employee extends Person).
			vbs = getVariableBindingsFromParentContext(context, binding);

			if (0 >= vbs.size()) {
				// 03 - Try to find the variable into another file (AnotherClass.variableName).
				vbs = getVariableBindingsFromAllContexts(context, binding);
			}
		}

		return vbs;
	}

	/**
	 * @param context
	 * @param binding
	 * @return
	 */
	private List<VariableBinding> getVariableBindingsFromContext(Context context, IBinding binding) {
		// 01 - Get the list of variables in the context.
		Map<IBinding, List<VariableBinding>> variableBindings = context.getVariables();

		// 02 - Get the list of references of this variable.
		List<VariableBinding> vbs = variableBindings.get(binding);

		// 03 - If vbs == null, instead of returning null we return an empty list.
		if (null == vbs) {
			vbs = Creator.newList();
		}

		return vbs;
	}

	/**
	 * @param context
	 * @param binding
	 * @return
	 */
	private List<VariableBinding> getVariableBindingsFromParentContext(Context context, IBinding binding) {
		List<VariableBinding> vbs = Creator.newList();
		// TODO
		return vbs;
	}

	/**
	 * @param binding
	 * @return
	 */
	private List<VariableBinding> getVariableBindingsFromAllContexts(Context context, IBinding binding) {
		List<VariableBinding> vbs = Creator.newList();

		// 01 - Iterate over all the contexts.
		for (Context currentContext : getContexts().values()) {
			// 02 - This context was already searched, avoid unnecessary processing.
			if (context.equals(currentContext)) {
				continue;
			}
			vbs = getVariableBindingsFromContext(currentContext, binding);
			if (null != vbs) {
				break;
			}
		}

		return vbs;
	}

	/**
	 * @param vbs
	 * @return
	 */
	private VariableBinding getLastReference(List<VariableBinding> vbs) {
		// 01 - Return the last element of the list.
		return (0 < vbs.size()) ? vbs.get(vbs.size() - 1) : null;
	}

	/** Methods */
	/**
	 * @param resource
	 * @param method
	 */
	public void addMethodDeclaration(IResource resource, MethodDeclaration method) {
		getContext(resource).addMethodDeclaration(method);
	}

	/**
	 * @param resource
	 * @param caller
	 * @param callee
	 */
	public void addMethodInvocation(IResource resource, MethodDeclaration caller, Expression callee) {
		getContext(resource).addMethodInvocation(caller, callee);
	}

	/**
	 * @param resource
	 * @return
	 */
	public Map<MethodDeclaration, List<Expression>> getMethods(IResource resource) {
		return getContext(resource).getMethods();
	}

	/**
	 * @param mapMethods
	 * @param expr
	 * @return
	 */
	private MethodDeclaration getMethod(Map<MethodDeclaration, List<Expression>> mapMethods, Expression expr) {
		// 01 - Iterate through the list to verify if we have the implementation of this method in our list.
		for (MethodDeclaration methodDeclaration : mapMethods.keySet()) {
			// 02 - Verify if these methods are the same.
			if (BindingResolver.areMethodsEqual(methodDeclaration, expr)) {
				return methodDeclaration;
			}
		}

		return null;
	}

	/**
	 * Get the implementation (MethodDeclaration) of the method.
	 * 
	 * @param resource
	 * @param expr
	 * @return
	 */
	public MethodDeclaration getMethod(IResource resource, Expression expr) {
		// 01 - Get the context of this resource.
		Context context = getContext(resource);

		// 02 - Get all the methods from this resource.
		// 03 - From that list, try to find this method (Expression).
		MethodDeclaration method = getMethod(context.getMethods(), expr);

		// 04 - If method is different from null, it means we found it.
		if (null != method) {
			return method;
		}

		// 05 - If it reaches this point, it means that this method was not implemented into this resource.
		// We now have to try to find its implementation in other resources of this project.
		for (Context currentContext : getContexts().values()) {
			// 06 - This context was already searched, avoid unnecessary processing.
			if (context.equals(currentContext)) {
				continue;
			}

			// 07 - Try to find the method into the current context.
			method = getMethod(currentContext.getMethods(), expr);

			// 08 - If method is different from null, it means we found it.
			if (null != method) {
				return method;
			}
		}

		// We did not find this method into our list of methods. (We do not have this method's implementation).
		return null;
	}

	/**
	 * @param methodToSearch
	 * @return
	 */
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

	public Context getContext(IResource resource, MethodDeclaration method, Expression invoker) {
		// 01 - Get the context of the provided resource.
		Context context = getContext(resource);

		// 02 - Iterate over all the children of this context.
		for (Context childContext : context.getChildrenContexts()) {
			// There are two ways we can find the wanted context.
			// Case 01 : This context has an invoker, this is unique.
			// Case 02 : Check if the method declaration is equal + the invoker.

			// Case 01.
			if ((null != childContext.getInvoker()) && (childContext.getInvoker().equals(invoker))) {
				return childContext;
			}

			// Case 02.
			// 03 - Get the list of methods of this context.
			Map<MethodDeclaration, List<Expression>> methods = childContext.getMethods();

			// 03 - Iterate over each method into the map.
			for (MethodDeclaration currentMethod : methods.keySet()) {
				// 04 - Verify if these methods are the same.
				if (method.equals(currentMethod)) {
					return childContext;
				}
			}

		}

		return null;
	}

}
