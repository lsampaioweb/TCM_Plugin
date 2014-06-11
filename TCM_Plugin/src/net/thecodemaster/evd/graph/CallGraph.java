package net.thecodemaster.evd.graph;

import java.util.Map;

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

	private final Map<IResource, Context>	fileContexts;

	public CallGraph() {
		fileContexts = Creator.newMap();
	}

	private Map<IResource, Context> getFileContexts() {
		return fileContexts;
	}

	public void remove(IResource resource) {
		getFileContexts().remove(resource);
	}

	private Context getContext(IResource resource) {
		// 01 - Get the context of the provided resource.
		Context context = getFileContexts().get(resource);

		// 02 - It means this resource does not have a context yet.
		if (null == context) {
			// 02.1 - Create a new context.
			context = new Context(resource);

			// 02.2 - Add it to the list.
			getFileContexts().put(resource, context);
		}

		return context;
	}

	public IBinding resolveBinding(ASTNode node) {
		return BindingResolver.resolveBinding(node);
	}

	private VariableBinding createVariableBinding(SimpleName variableName, Expression initializer) {
		return new VariableBinding(resolveBinding(variableName), initializer);
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

}
