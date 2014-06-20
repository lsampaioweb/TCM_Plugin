package net.thecodemaster.evd.context;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.ui.enumeration.EnumVariableType;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

public class Context {

	private IResource																		resource;
	private Context																			parent;
	private final Map<IBinding, List<VariableBinding>>	variables;
	private final Map<MethodDeclaration, List<ASTNode>>	methods;

	private final List<Context>													childrenContexts;
	private Expression																	instance;
	private ASTNode																			invoker;

	public Context(IResource resource) {
		setResource(resource);
		variables = Creator.newMap();
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

	public IResource getResource() {
		return resource;
	}

	private void setParent(Context parent) {
		this.parent = parent;
	}

	public Context getParent() {
		return parent;
	}

	public Map<IBinding, List<VariableBinding>> getVariables() {
		return variables;
	}

	/**
	 * @param variableBinding
	 * @return
	 */
	public VariableBinding addVariable(VariableBinding variableBinding) {
		// 01 - Get the unique id (binding) of this variable.
		IBinding binding = variableBinding.getBinding();

		// 02 - If list does not contain this binding, it is the first time, so we create it.
		if (!getVariables().containsKey(binding)) {
			List<VariableBinding> vbs = Creator.newList();

			// 03 - Add the binding with an empty list of occurrences.
			getVariables().put(binding, vbs);
		}

		// 04 - Get the list of occurrences of this variable.
		List<VariableBinding> vbs = getVariables().get(binding);

		// 05 - If this is the first occurrences, we have to check if this is a
		// local or global variable.
		updateParentContextIfGlobalVariable(vbs, variableBinding);

		// 06 - Add the variable to the list.
		vbs.add(variableBinding);

		return variableBinding;
	}

	/**
	 * @param vbs
	 * @param variableBinding
	 */
	private void updateParentContextIfGlobalVariable(List<VariableBinding> vbs, VariableBinding variableBinding) {
		if (0 == vbs.size()) {
			if ((variableBinding.getType() != EnumVariableType.GLOBAL) && (isGlobalVariable(variableBinding))) {
				variableBinding.setType(EnumVariableType.GLOBAL);
			}
		} else {
			variableBinding.setType(vbs.get(0).getType());
		}

		// 02 - If this is a reference to a global variable, we also have to update it.
		if (variableBinding.getType() == EnumVariableType.GLOBAL) {
			updateGlobalVariable(variableBinding);
		}
	}

	/**
	 * @param variableBinding
	 * @return
	 */
	private boolean isGlobalVariable(VariableBinding variableBinding) {
		return (getGlobalVariableBindings(variableBinding).size() > 0);
	}

	/**
	 * @param variableBinding
	 * @return
	 */
	private List<VariableBinding> getGlobalVariableBindings(VariableBinding variableBinding) {
		// 01 - Create an empty list.
		List<VariableBinding> emptyList = Creator.newList();

		Context context = this;
		// 02 - Iterate until it reaches the top level parent.
		while (null != context.getParent()) {
			// 03 - Become the parent.
			context = context.getParent();

			// 05 - Get the list of occurrences of this variable.
			List<VariableBinding> vbs = context.getVariables().get(variableBinding.getBinding());

			return (null != vbs) ? vbs : emptyList;
		}

		return emptyList;
	}

	/**
	 * @param variableBinding
	 */
	private void updateGlobalVariable(VariableBinding variableBinding) {
		List<VariableBinding> vbs = getGlobalVariableBindings(variableBinding);

		if (0 < vbs.size()) {
			// 01 - Add the variable to the list.
			vbs.add(variableBinding);
		}
	}

	/**
	 * @return
	 */
	public Map<MethodDeclaration, List<ASTNode>> getMethods() {
		return methods;
	}

	/**
	 * @param method
	 */
	public void addMethodDeclaration(MethodDeclaration method) {
		// 01 - Get the list of methods.
		Map<MethodDeclaration, List<ASTNode>> methods = getMethods();

		if (!methods.containsKey(method)) {
			List<ASTNode> invocations = Creator.newList();

			// Create a empty list of method invocations.
			methods.put(method, invocations);
		}
	}

	/**
	 * @param caller
	 * @param callee
	 */
	public void addMethodInvocation(MethodDeclaration caller, ASTNode callee) {
		// 01 - Get the list of invocations of this method declaration.
		List<ASTNode> invocations = getMethods().get(caller);

		// 02 - If the list is null, it means this method was not saved before.
		if (null == invocations) {
			// 02.1 So, we have to add it.
			addMethodDeclaration(caller);

			// 02.2 Now, we try again to retrieve the list of invocations.
			invocations = getMethods().get(caller);
		}

		// 03 - Add the method invocation for the current method (caller).
		invocations.add(callee);
	}

	public void addChildContext(Context context) {
		getChildrenContexts().add(context);
	}

	public List<Context> getChildrenContexts() {
		return childrenContexts;
	}

	public ASTNode getInvoker() {
		return invoker;
	}

	public void setInvoker(ASTNode invoker) {
		this.invoker = invoker;
	}

	public Expression getInstance() {
		return instance;
	}

	public void setInstance(Expression instance) {
		this.instance = instance;
	}

	public void merge(Context otherContext) {
		mergeVariables(otherContext, 0);
		mergeMethods(otherContext);
	}

	public void mergeVariables(Context otherContext, int type) {
		if (null != otherContext) {
			getVariables().clear();

			getVariables().putAll(getGlobalVariables(otherContext, type));
		}
	}

	public void mergeMethods(Context otherContext) {
		if (null != otherContext) {
			getMethods().clear();

			getMethods().putAll(getGlobalMethods(otherContext));
		}
	}

	private Map<IBinding, List<VariableBinding>> getGlobalVariables(Context otherContext, int type) {
		// 01 - Static variables. I want the last reference.
		// 02 - Global variables. I want the first reference.
		Map<IBinding, List<VariableBinding>> tempVariables = Creator.newMap();

		// 01 - Iterate over all the variables.
		for (Entry<IBinding, List<VariableBinding>> entry : otherContext.getVariables().entrySet()) {

			// 02 - Create a temporary list.
			List<VariableBinding> currentVariableBindings = Creator.newList();

			// 03 - Get all the references of this variable.
			List<VariableBinding> list = entry.getValue();

			// 04 - If this variable is static, I want the last reference.
			if (Modifier.isStatic(entry.getKey().getModifiers())) {
				// 05 - Get the last reference.
				currentVariableBindings.add(list.get((list.size() > 0) ? list.size() - 1 : 0));
			} else {
				if (0 == type) {
					// 05 - Get the first reference.
					currentVariableBindings.add(list.get(0));
				} else {
					// 05 - Get the last reference.
					currentVariableBindings.add(list.get((list.size() > 0) ? list.size() - 1 : 0));
				}
			}

			// 06 - Add the variable to the list that will be returned.
			tempVariables.put(entry.getKey(), currentVariableBindings);
		}

		return tempVariables;
	}

	private Map<MethodDeclaration, List<ASTNode>> getGlobalMethods(Context otherContext) {
		return otherContext.getMethods();
	}

	@Override
	public String toString() {
		Map<MethodDeclaration, List<ASTNode>> methods = getMethods();
		String methodName = "";
		if (0 < methods.size()) {
			methodName = methods.keySet().iterator().next().getName().getIdentifier();
		}

		String strInvoker = (null != getInvoker()) ? getInvoker().toString() : "";
		return String.format("%s - %s", methodName, strInvoker);
	}

}
