package net.thecodemaster.sap.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.sap.utils.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At any
 * given time, we should only have on call graph of the code.
 * 
 * @author Luciano Sampaio
 */
public class CallGraph {

	/**
	 * The file means the current branch that is being analyzed.
	 */
	private String																											currentFile;

	/**
	 * List with all the declared methods of the analyzed code.
	 */
	private final Map<String, Map<MethodDeclaration, List<Expression>>>	methodsPerFile;

	/**
	 * List with all the declared variables of the analyzed code.
	 */
	private final Map<IVariableBinding, VariableBindingManager>					listVariables;

	public CallGraph() {
		methodsPerFile = Creator.newMap();
		listVariables = Creator.newMap();
	}

	private String getResourceName(IResource resource) {
		return resource.getProjectRelativePath().toOSString();
	}

	public void addFile(IResource resource) {
		this.currentFile = getResourceName(resource);
	}

	public boolean containsFile(IResource resource) {
		return methodsPerFile.containsKey(getResourceName(resource));
	}

	public boolean removeFile(IResource resource) {
		return (null != methodsPerFile.remove(getResourceName(resource)));
	}

	public void addMethod(MethodDeclaration method) {
		// 01 - Check if the current file is already in the list.
		if (!methodsPerFile.containsKey(currentFile)) {
			Map<MethodDeclaration, List<Expression>> methods = Creator.newMap();

			methodsPerFile.put(currentFile, methods);
		}

		// 02 - Get the list of methods in the current file.
		Map<MethodDeclaration, List<Expression>> methods = getMethods(currentFile);

		if (!methods.containsKey(method)) {
			List<Expression> invocations = Creator.newList();

			// Create a empty list of method invocations.
			methods.put(method, invocations);
		}
	}

	public void addInvokes(MethodDeclaration caller, Expression callee) {
		// 01 - Get the list of methods in the current file.
		Map<MethodDeclaration, List<Expression>> methods = getMethods(currentFile);

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
		return getMethods(getResourceName(resource));
	}

	private Map<MethodDeclaration, List<Expression>> getMethods(String file) {
		return methodsPerFile.get(file);
	}

	public Map<IVariableBinding, VariableBindingManager> getlistVariables() {
		return listVariables;
	}

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
		for (Entry<String, Map<MethodDeclaration, List<Expression>>> entry : methodsPerFile.entrySet()) {
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

}
