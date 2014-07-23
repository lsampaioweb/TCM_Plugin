package net.thecodemaster.esvd.context;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.esvd.graph.VariableBinding;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.ui.enumeration.EnumVariableType;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;

public class Context {

	private IResource																		resource;
	private Context																			parent;
	private Context																			instanceContext;
	private final Map<IBinding, List<VariableBinding>>	variables;
	private final Map<MethodDeclaration, List<ASTNode>>	methods;

	private final List<Context>													childrenContexts;
	private Expression																	instance;
	private ASTNode																			invoker;
	private Type																				superClassName;
	private boolean																			isClassContext;

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
		List<VariableBinding> vbs = getVariableBindings(binding);

		// 05 - If this is the first occurrence, we have to check if this is a
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
		while (null != context) {
			Context instanceContext = context.getInstanceContext();

			if (null != instanceContext) {
				// 05 - Get the list of occurrences of this variable.
				List<VariableBinding> vbs = instanceContext.getVariableBindings(variableBinding.getBinding());

				if (vbs.size() > 0) {
					return vbs;
				}
			}

			// 03 - Become the parent.
			context = context.getParent();

			if (null != context) {
				// 05 - Get the list of occurrences of this variable.
				List<VariableBinding> vbs = context.getVariableBindings(variableBinding.getBinding());

				if (vbs.size() > 0) {
					return vbs;
				}
			}
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
		// If the method is null, there is nothing to do.
		if (null == method) {
			return;
		}

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

		if (null != invocations) {
			// 03 - Add the method invocation for the current method (caller).
			invocations.add(callee);
		}
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

	public void addSuperClass(Type superClassName) {
		this.superClassName = superClassName;
	}

	public Type getSuperClass() {
		return superClassName;
	}

	public void setIsClassContext(boolean isClassContext) {
		this.isClassContext = isClassContext;
	}

	public boolean isClassContext() {
		return isClassContext;
	}

	public void addInstanceContext(Context instanceContext) {
		this.instanceContext = instanceContext;
	}

	public Context getInstanceContext() {
		return instanceContext;
	}

	public void merge(Context otherContext) {
		mergeVariables(otherContext, 0);
		mergeMethods(otherContext);
	}

	/**
	 * @param otherContext
	 * @param type
	 *          0 - Get the first reference. <br/>
	 *          1 - Get the last reference.
	 */
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

	/**
	 * @param otherContext
	 * @param type
	 *          0 - Get the first reference. <br/>
	 *          1 - Get the last reference.
	 * @return
	 */
	private Map<IBinding, List<VariableBinding>> getGlobalVariables(Context otherContext, int type) {
		// 01 - Static variables. I want the last reference.
		// 02 - Global variables. I want the first reference.
		Map<IBinding, List<VariableBinding>> tempVariables = Creator.newMap();

		// 01 - Iterate over all the variables.
		for (Entry<IBinding, List<VariableBinding>> entry : otherContext.getVariables().entrySet()) {

			// 02 - If the getKey is null, we probably have a syntax error. We have to be prepared for that.
			if (null == entry.getKey()) {
				continue;
			}

			// 02 - Create a temporary list.
			List<VariableBinding> currentVariableBindings = Creator.newList();

			// 03 - Get all the references of this variable.
			List<VariableBinding> list = entry.getValue();

			// 04 - If this variable is static, I want the last reference.
			if (Modifier.isStatic(entry.getKey().getModifiers())) {
				if (list.size() > 0) {
					// 05 - Get the last reference.
					currentVariableBindings.add(list.get(list.size() - 1));
				}
			} else {
				if (list.size() > 0) {
					if (0 == type) {
						// 05 - Get the first reference.
						currentVariableBindings.add(list.get(0));
					} else {
						// 05 - Get the last reference.
						currentVariableBindings.add(list.get(list.size() - 1));
					}
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
			MethodDeclaration methodDeclaration = methods.keySet().iterator().next();
			if (null != methodDeclaration) {
				methodName = methodDeclaration.getName().getIdentifier();
			}
		}

		String strInvoker = (null != getInvoker()) ? getInvoker().toString() : "";
		return String.format("%s - %s", methodName, strInvoker);
	}

	public List<VariableBinding> getVariableBindings(IBinding binding) {
		// 01 - Get the list of variables in the context.
		List<VariableBinding> vbs = Creator.newList();

		for (Entry<IBinding, List<VariableBinding>> entry : getVariables().entrySet()) {
			if ((null != entry.getKey()) && (entry.getKey().isEqualTo(binding))) {
				vbs = entry.getValue();
				break;
			}
		}

		if (null == vbs) {
			// 02 - If vbs == null, instead of returning null we return an empty list.
			vbs = Creator.newList();
		}

		return vbs;
	}

}
