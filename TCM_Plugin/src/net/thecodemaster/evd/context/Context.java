package net.thecodemaster.evd.context;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class Context {

	private IResource																				resource;
	private Context																					parent;
	private final List<VariableBinding>											variables;
	private final Map<MethodDeclaration, List<Expression>>	methods;

	private final List<Context>															childrenContexts;
	private Expression																			invoker;

	public Context(IResource resource) {
		setResource(resource);
		variables = Creator.newList();
		methods = Creator.newMap();
		childrenContexts = Creator.newList();
	}

	public Context(IResource resource, Context parent) {
		this(resource);

		setParent(parent);
	}

	private void setResource(IResource resource) {
		this.resource = resource;
	}

	private void setParent(Context parent) {
		this.parent = parent;
	}

	public List<VariableBinding> getVariables() {
		return variables;
	}

	public void addVariable(VariableBinding variableBinding) {
		getVariables().add(variableBinding);
	}

	public void addVariableAll(List<VariableBinding> variableBindings) {
		getVariables().addAll(variableBindings);
	}

	public Map<MethodDeclaration, List<Expression>> getMethods() {
		return methods;
	}

	public void addMethodDeclaration(MethodDeclaration method) {
		// 01 - Get the list of methods.
		Map<MethodDeclaration, List<Expression>> methods = getMethods();

		if (!methods.containsKey(method)) {
			List<Expression> invocations = Creator.newList();

			// Create a empty list of method invocations.
			methods.put(method, invocations);
		}
	}

	public void addMethodInvocation(MethodDeclaration caller, Expression callee) {
		// 01 - Add the method invocation for the current method (caller).
		getMethods().get(caller).add(callee);
	}

	public void addChildContext(Context context) {
		getChildrenContexts().add(context);
	}

	public List<Context> getChildrenContexts() {
		return childrenContexts;
	}

	public Expression getInvoker() {
		return invoker;
	}

	public void setInvoker(Expression invoker) {
		this.invoker = invoker;
	}

}
