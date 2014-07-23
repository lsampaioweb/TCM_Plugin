package net.thecodemaster.esvd.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.esvd.ui.enumeration.EnumVariableType;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

/**
 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At any
 * given time, we should only have one call graph of the code.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class CallGraph {

	/**
	 * Each resource has one top level context.
	 */
	private final Map<IResource, Context>	contexts;

	public CallGraph() {
		contexts = Creator.newMap();
	}

	private Map<IResource, Context> getContexts() {
		return contexts;
	}

	private IResource getResource(MethodDeclaration method, ASTNode invoker, Context parentContext) {
		IResource resource = null;
		if (null != method) {
			resource = BindingResolver.getResource(method);
		} else if ((null != invoker) && (invoker.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION)) {
			// If the method is null, it means this is a Constructor without source code.
			// We have two cases, a library or one of our classes without a constructor.
			// If it is a library the resource will be null.
			resource = BindingResolver.getResource(this, ((ClassInstanceCreation) invoker).getType());
		}

		if ((null == resource) && (null != parentContext)) {
			// This is our last hope.
			resource = parentContext.getResource();
		}
		return resource;
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

	public Context getContext(IResource resource, MethodDeclaration method, ASTNode invoker) {
		return getContext(getContext(resource), method, invoker);
	}

	public Context getContext(Context context, MethodDeclaration method, ASTNode invoker) {
		// 01 - Iterate over all the children of this context.
		for (Context childContext : context.getChildrenContexts()) {
			// There are three ways we can find the wanted context.
			// Case 01 : Check if the method declaration is equal + the invoker.
			// Case 02 : This context has an invoker, this is unique.

			// Case 02.
			// 02 - Get the list of methods of this context.
			Map<MethodDeclaration, List<ASTNode>> methods = childContext.getMethods();

			for (MethodDeclaration currentMethodDeclaration : methods.keySet()) {
				// 04 - Verify if these methods are the same.
				if ((null != currentMethodDeclaration) && (currentMethodDeclaration.equals(method))) {
					// // Case 01.
					ASTNode childInvoker = childContext.getInvoker();
					if ((null != childInvoker) && (childInvoker.equals(invoker))) {
						return childContext;
					} else if ((null == childInvoker) && (null == invoker)) {
						return childContext;
					}
				}
			}

		}

		return context;
	}

	public Context getInstanceContext(Context context, Expression instance) {
		if (null == instance) {
			return context;
		}

		IBinding otherBinding = resolveBinding(instance);

		Context currentContext = context;
		while (null != currentContext) {
			// 01 - Iterate over all the children of this context.
			for (Context childContext : currentContext.getChildrenContexts()) {
				if (!childContext.isClassContext()) {
					continue;
				}

				// Check if the instance of this context is the one I am looking for.
				Expression currentInstance = childContext.getInstance();
				if (null != currentInstance) {
					IBinding currentBinding = resolveBinding(currentInstance);

					if ((null != currentBinding) && (currentBinding.equals(otherBinding))) {
						return childContext;
					}
				}

			}
			currentContext = currentContext.getParent();
		}

		return context;
	}

	public Context getInstanceContext(Context parentContext, MethodDeclaration method, ASTNode invoker,
			Expression instance) {
		// 01 - Get the context of the instance object.
		Context instanceContext = getInstanceContext(parentContext, instance);

		// 02 - Get the context of the method.
		return getContext(instanceContext, method, invoker);
	}

	public Context getClassContext(Context parentContext, Expression instance) {
		// 01 - Get the context of the instance object.
		return getInstanceContext(parentContext, instance);
	}

	public Context getStaticContext(Context parentContext, MethodDeclaration method, ASTNode invoker) {
		// 01 - Get the static context of that class.
		Context classStaticContext = getContext(BindingResolver.getResource(method));

		// 02 - Get the context of the method.
		return getContext(classStaticContext, method, invoker);
	}

	public Context getStaticContext(IResource resource) {
		return getContext(resource);
	}

	public Context newContext(IResource resource, MethodDeclaration method, ASTNode invoker) {
		return newContext(getContext(resource), method, invoker);
	}

	public Context newContext(Context parentContext, MethodDeclaration method, ASTNode invoker) {
		// 01 - Get the resource of this context.
		IResource resource = getResource(method, invoker, parentContext);

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

	public Context newClassContext(Context parentContext, MethodDeclaration method, ASTNode invoker, Expression instance) {
		// This method declaration is a constructor from a class. So, we need to get
		// that context, but without the extra stuff.
		// We only need global variables and methods.

		// 01 - Create a context.
		Context classContext = newContext(parentContext, method, invoker);

		// 02 - Set the object that will hold this context.
		classContext.setInstance(instance);

		// 03 - Set that this context is a class context.
		classContext.setIsClassContext(true);

		// 04 - Get the resource of this constructor.
		IResource resource = getResource(method, invoker, null);

		if (null != resource) {
			// 05 - Get the context (top level) of that resource.
			// 06 - Copy the variables and methods from the classContext to this new context.
			// If the resource is null, we don't have the source code, so there is no point
			// in copying the methods and variables.
			classContext.merge(getContext(resource));
		}
		// 07 - Create the context for the constructor method.
		Context constructorContext = newInstanceContext(parentContext, method, invoker, instance);

		return constructorContext;
	}

	public Context newInstanceContext(Context parentContext, MethodDeclaration method, ASTNode invoker,
			Expression instance) {
		// 01 - Get the context of the instance object.
		Context instanceContext = getInstanceContext(parentContext, instance);

		// 02 - Create a context.
		Context context = newContext(parentContext, method, invoker);

		// 03 - Set the object that will hold this context.
		context.setInstance(instance);

		// 04 - Copy the variables and methods from the classContext to this new context.
		context.mergeVariables(instanceContext, 1);

		// 05 - Add this new context as a child to the instanceContext.
		instanceContext.addChildContext(context);

		// 06 - This context is part of the instanceContext.
		context.addInstanceContext(instanceContext);

		return context;
	}

	public Context newStaticContext(Context parentContext, MethodDeclaration method, ASTNode invoker) {
		// 01 - Get the static context of that class.
		Context classStaticContext = getContext(BindingResolver.getResource(method));

		// 02 - Add this new context as a child of the parent context.
		parentContext.addChildContext(classStaticContext);

		// 03 - Create a context.
		Context context = newContext(classStaticContext, method, invoker);

		return context;
	}

	public void remove(IResource resource) {
		getContexts().remove(resource);
	}

	public void removeChildContexts(IResource resource) {
		// 01 - Get the context of the provided resource.
		Context context = getContexts().get(resource);

		if (null != context) {
			context.getChildrenContexts().clear();
		}
		// Reset the global variables.
		resetGlobalVariables(context);
	}

	private void resetGlobalVariables(Context context) {
		for (Entry<IBinding, List<VariableBinding>> entry : context.getVariables().entrySet()) {
			List<VariableBinding> vbs = entry.getValue();

			// If there is only one reference, we just reset the status.
			if ((null == vbs) || (vbs.size() == 0)) {
				continue;
			}

			// If there several reference, we keep the first one and delete the others.
			VariableBinding vb = vbs.get(0);

			// Clear all the other.
			vbs.clear();

			// Reset to UNKNOWN.
			vb.setStatus(EnumVariableStatus.UNKNOWN);

			// Re-add to the list.
			vbs.add(vb);
		}
	}

	private VariableBinding createVariableBinding(Expression variableName, EnumVariableType type, Expression initializer) {
		return new VariableBinding(resolveBinding(variableName), type, initializer);
	}

	/**
	 * @param node
	 * @return
	 */
	public IBinding resolveBinding(ASTNode node) {
		return BindingResolver.resolveBinding(node);
	}

	/**
	 * @param context
	 * @param variableName
	 * @param type
	 * @param initializer
	 * @return
	 */
	private VariableBinding addVariable(Context context, Expression variableName, EnumVariableType type,
			Expression initializer) {
		return context.addVariable(createVariableBinding(variableName, type, initializer));
	}

	public VariableBinding addFieldDeclaration(IResource resource, Expression fieldName, Expression initializer) {
		return addVariable(getContext(resource), fieldName, EnumVariableType.GLOBAL, initializer);
	}

	public VariableBinding addFieldDeclaration(Context context, Expression fieldName, Expression initializer) {
		return addVariable(context, fieldName, EnumVariableType.GLOBAL, initializer);
	}

	/**
	 * @param context
	 * @param parameterName
	 * @param initializer
	 * @return
	 */
	public VariableBinding addParameter(Context context, Expression parameterName, Expression initializer) {
		return addVariable(context, parameterName, EnumVariableType.PARAMETER, initializer);
	}

	/**
	 * @param context
	 * @param variableName
	 * @param initializer
	 * @return
	 */
	public VariableBinding addVariable(Context context, Expression variableName, Expression initializer) {
		return addVariable(context, variableName, EnumVariableType.LOCAL, initializer);
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
			ASTNode expression = BindingResolver.getParentWhoHasAReference(variable);
			if (null != expression) {

				// 03 - Get the variable binding if the expression matches one of the references of this variable.
				VariableBinding variableBinding = getVariableBindingIfReferenceMatch(vbs, expression, context);
				if (null != variableBinding) {
					return variableBinding;
				}
			}
		}

		return null;
	}

	/**
	 * @param vbs
	 * @param expression
	 * @return
	 */
	private VariableBinding getVariableBindingIfReferenceMatch(List<VariableBinding> vbs, ASTNode expression,
			Context context) {
		for (VariableBinding variableBinding : vbs) {
			ReferenceManager referenceManager = variableBinding.getReferenceManager();
			if (referenceManager.hasReference(expression, context.hashCode())) {
				return variableBinding;
			}
		}
		return null;
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

			// if (0 >= vbs.size()) {
			// // 03 - Try to find the variable into another file (AnotherClass.variableName).
			// vbs = getVariableBindingsFromAllContexts(context, binding);
			// }
		}

		return vbs;
	}

	/**
	 * @param context
	 * @param binding
	 * @return
	 */
	private List<VariableBinding> getVariableBindingsFromContext(Context context, IBinding binding) {
		return context.getVariableBindings(binding);
	}

	/**
	 * @param context
	 * @param binding
	 * @return
	 */
	private List<VariableBinding> getVariableBindingsFromParentContext(Context context, IBinding binding) {
		List<VariableBinding> vbs = Creator.newList();

		Context parentContext = context.getParent();
		while (null != parentContext) {
			vbs = getVariableBindingsFromContext(parentContext, binding);
			if (0 < vbs.size()) {
				break;
			}

			parentContext = parentContext.getParent();
		}

		return vbs;
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
		addMethodInvocation(getContext(resource), caller, callee);
	}

	/**
	 * @param resource
	 * @param caller
	 * @param callee
	 */
	public void addMethodInvocation(Context context, MethodDeclaration caller, ASTNode callee) {
		context.addMethodInvocation(caller, callee);
	}

	public void addSuperClass(IResource resource, Type superClassName) {
		Context context = getContext(resource);

		context.addSuperClass(superClassName);
	}

	public Type getSuperClass(IResource resource) {
		return getContext(resource).getSuperClass();
	}

	/**
	 * @param resource
	 * @return
	 */
	public Map<MethodDeclaration, List<ASTNode>> getMethods(IResource resource) {
		return getContext(resource).getMethods();
	}

	/**
	 * @param methodToSearch
	 * @return
	 */
	public Map<MethodDeclaration, List<ASTNode>> getInvokers(MethodDeclaration methodToSearch) {
		Map<MethodDeclaration, List<ASTNode>> invokers = Creator.newMap();

		// 01 - Iterate over all the contexts and check which methods invoke the provided method.
		for (Context context : getContexts().values()) {

			// 02 - Get the list of methods of this resource.
			Map<MethodDeclaration, List<ASTNode>> methods = context.getMethods();

			// 03 - Iterate over each method into the map.
			for (Entry<MethodDeclaration, List<ASTNode>> currentMethod : methods.entrySet()) {

				// 04 - Iterate over each method invocation of the current method.
				for (ASTNode invocation : currentMethod.getValue()) {

					// 05 - Verify if these methods are the same.
					if (BindingResolver.areMethodsEqual(methodToSearch, invocation)) {
						if (!invokers.containsKey(currentMethod.getKey())) {
							List<ASTNode> invocations = Creator.newList();

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

	public IResource getResourceFromPackageName(String packageName) {
		if (null != packageName) {
			String packageNameAndFile = String.format("%s.%s", packageName, Constant.RESOURCE_TYPE_TO_PERFORM_DETECTION);

			// 01 - Iterate over all the contexts
			for (Context context : getContexts().values()) {
				IResource resource = context.getResource();

				if (null == resource) {
					continue;
				}

				String relativePath = resource.getProjectRelativePath().toOSString().replaceAll("/", ".");

				if (relativePath.endsWith(packageNameAndFile)) {
					return resource;
				}

			}
		}

		return null;
	}

}
