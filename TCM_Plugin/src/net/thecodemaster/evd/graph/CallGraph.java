package net.thecodemaster.evd.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At any
 * given time, we should only have on call graph of the code.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class CallGraph {

	/**
	 * The current file that is being analyzed.
	 */
	private IResource																													currentResource;

	/**
	 * List with all the declared methods of the analyzed code.
	 */
	private final Map<IResource, Map<MethodDeclaration, List<Expression>>>		methodsPerFile;

	/**
	 * List with all the declared variables of the analyzed code.
	 */
	private final Map<IResource, Map<IBinding, List<VariableBindingManager>>>	variablesPerFile;

	public CallGraph() {
		methodsPerFile = Creator.newMap();
		variablesPerFile = Creator.newMap();
	}

	public IResource getCurrentResource() {
		return currentResource;
	}

	public void setCurrentResource(IResource resource) {
		this.currentResource = resource;
	}

	public boolean contains(IResource resource) {
		return (methodsPerFile.containsKey(resource) || variablesPerFile.containsKey(resource));
	}

	public void remove(IResource resource) {
		methodsPerFile.remove(resource);
		variablesPerFile.remove(resource);
	}

	public void addMethod(MethodDeclaration method) {
		// 01 - Check if the current file is already in the list.
		if (!methodsPerFile.containsKey(getCurrentResource())) {
			Map<MethodDeclaration, List<Expression>> methods = Creator.newMap();

			methodsPerFile.put(getCurrentResource(), methods);
		}

		// 02 - Get the list of methods in the current file.
		Map<MethodDeclaration, List<Expression>> methods = getMethods(getCurrentResource());

		if (!methods.containsKey(method)) {
			List<Expression> invocations = Creator.newList();

			// Create a empty list of method invocations.
			methods.put(method, invocations);
		}
	}

	public void addMethodInvocation(MethodDeclaration caller, Expression callee) {
		// 01 - Get the list of methods in the current file.
		Map<MethodDeclaration, List<Expression>> methods = getMethods(getCurrentResource());

		if (null == methods) {
			return;
		}

		// 02 - If the methods is not in the list, add it.
		if (!methods.containsKey(caller)) {
			addMethod(caller);
		}

		// 03 - Add the method invocation for the current method (caller).
		List<Expression> invocations = methods.get(caller);
		invocations.add(callee);
	}

	public Map<MethodDeclaration, List<Expression>> getMethods(IResource resource) {
		Map<MethodDeclaration, List<Expression>> emptyList = Creator.newMap();

		Map<MethodDeclaration, List<Expression>> methods = methodsPerFile.get(resource);

		return (null != methods) ? methods : emptyList;
	}

	/**
	 * Get the implementation (MethodDeclaration) of the method.
	 * 
	 * @param resource
	 * @param expr
	 * @return
	 */
	public MethodDeclaration getMethod(IResource resource, Expression expr) {
		// 01 - Get all the methods from this resource.
		// 02 - From that list, try to find this method (expr).
		MethodDeclaration method = getMethod(getMethods(resource), expr);

		// 03 - If method is different from null, it means we found it.
		if (null != method) {
			return method;
		}

		// 04 - If it reaches this point, it means that this method was not implemented into this resource.
		// We now have to try to find its implementation in other resources of this project.
		for (Entry<IResource, Map<MethodDeclaration, List<Expression>>> entry : methodsPerFile.entrySet()) {
			method = getMethod(entry.getValue(), expr);

			// 05 - If method is different from null, it means we found it.
			if (null != method) {
				return method;
			}
		}

		// We did not find this method into our list of methods. (We do not have this method's implementation)
		return null;
	}

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

	public Map<MethodDeclaration, List<Expression>> getInvokers(MethodDeclaration methodToSearch) {
		Map<MethodDeclaration, List<Expression>> invokers = Creator.newMap();

		// 01 - Iterate over all the values and check which methods invoke the provided method.
		for (Map<MethodDeclaration, List<Expression>> entryMap : methodsPerFile.values()) {
			for (Entry<MethodDeclaration, List<Expression>> currentMethod : entryMap.entrySet()) {
				for (Expression expression : currentMethod.getValue()) {

					// 02 - Verify if these methods are the same.
					if (BindingResolver.areMethodsEqual(methodToSearch, expression)) {
						invokers.put(currentMethod.getKey(), currentMethod.getValue());
						break;
					}
				}
			}
		}

		return invokers;
	}

	public VariableBindingManager addVariableToCallGraph(Expression variable, Expression initializer) {
		return addVariableToCallGraph(BindingResolver.getResource(variable), variable, initializer);
	}

	public VariableBindingManager addVariableToCallGraph(IResource resource, Expression variable, Expression initializer) {
		IBinding binding = BindingResolver.resolveBinding(variable);

		if (null != binding) {
			VariableBindingManager variableBinding = new VariableBindingManager(binding);
			variableBinding.setInitializer(initializer);

			addVariable(resource, variableBinding);

			return variableBinding;
		}

		return null;
	}

	private void addVariable(IResource resource, VariableBindingManager variableBinding) {
		// 01 - Check if the current file is already in the list.
		if (!variablesPerFile.containsKey(resource)) {
			Map<IBinding, List<VariableBindingManager>> variableBindings = Creator.newMap();

			variablesPerFile.put(resource, variableBindings);
		}

		// 02 - Get the list of variables in the current file.
		Map<IBinding, List<VariableBindingManager>> variableBindings = getVariables(resource);

		IBinding binding = variableBinding.getBinding();
		if (!variableBindings.containsKey(binding)) {
			List<VariableBindingManager> vbms = Creator.newList();

			// Create a empty list of method invocations.
			variableBindings.put(binding, vbms);
		}

		// 03 - Add the variable to the list.
		List<VariableBindingManager> vbms = variableBindings.get(binding);
		if (!vbms.contains(variableBinding)) {
			vbms.add(variableBinding);
		}
	}

	private Map<IBinding, List<VariableBindingManager>> getVariables(IResource resource) {
		Map<IBinding, List<VariableBindingManager>> emptyList = Creator.newMap();

		Map<IBinding, List<VariableBindingManager>> variables = variablesPerFile.get(resource);

		return (null != variables) ? variables : emptyList;
	}

	private List<VariableBindingManager> getVariableBindings(IBinding binding) {
		// 01 - Get the list of variables in the current file.
		Map<IBinding, List<VariableBindingManager>> variableBindings = getVariables(getCurrentResource());

		// 02 - Get the list of references of this variable.
		List<VariableBindingManager> vbms = variableBindings.get(binding);

		// 03 - If the list is null, this variable belongs to another file.
		if ((null == vbms) && (null != binding)) {
			for (Entry<IResource, Map<IBinding, List<VariableBindingManager>>> entry : variablesPerFile.entrySet()) {
				vbms = getVariableBindings(entry.getValue(), binding);

				// 04 - If the list is different from null, it means we found it.
				if (vbms.size() > 0) {
					break;
				}
			}
		}

		// 05 - Return the last element of the list.
		return vbms;
	}

	private List<VariableBindingManager> getVariableBindings(Map<IBinding, List<VariableBindingManager>> mapVariables,
			IBinding binding) {
		List<VariableBindingManager> variableBindings = Creator.newList();

		// 01 - Iterate through the list to verify if we have the implementation of this method in our list.
		for (Entry<IBinding, List<VariableBindingManager>> entry : mapVariables.entrySet()) {
			// 02 - Verify if these methods are the same.
			if (entry.getKey().equals(binding)) {
				variableBindings = entry.getValue();
				break;
			}
		}

		return variableBindings;
	}

	public VariableBindingManager getVariableBinding(SimpleName simpleName) {
		// 01 - Get the list of references of this variable.
		List<VariableBindingManager> vbms = getVariableBindings(simpleName.resolveBinding());

		if (vbms.size() > 0) {
			// 02 - Get a parent expression which has a reference to this variable.
			Expression expression = BindingResolver.getParentWhoHasAReference(simpleName);
			if (null != expression) {
				for (VariableBindingManager variableBindingManager : vbms) {
					for (Expression currentMethod : variableBindingManager.getReferences()) {
						if (currentMethod == expression) {
							return variableBindingManager;
						}
					}
				}
			}
		}

		return null;
	}

	public VariableBindingManager getLastReference(SimpleName simpleName) {
		return getLastReference(simpleName.resolveBinding());
	}

	public VariableBindingManager getLastReference(IBinding binding) {
		// 01 - Get the list of references of this variable.
		List<VariableBindingManager> vbms = getVariableBindings(binding);

		// 02 - Return the last element of the list.
		return getLastReference(vbms);
	}

	private VariableBindingManager getLastReference(List<VariableBindingManager> vbms) {
		// 01 - Return the last element of the list.
		return (vbms.size() > 0) ? vbms.get(vbms.size() - 1) : null;
	}

}
