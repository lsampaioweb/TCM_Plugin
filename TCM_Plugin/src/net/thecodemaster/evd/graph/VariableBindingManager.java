package net.thecodemaster.evd.graph;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class VariableBindingManager {

	private IBinding											binding;
	private Expression										initializer;

	private VariableBindingManager				initializerReference;
	private final List<MethodInvocation>	methods;

	public VariableBindingManager(IBinding binding) {
		this.setBinding(binding);

		methods = Creator.newList();
	}

	public IBinding getBinding() {
		return binding;
	}

	private void setBinding(IBinding binding) {
		this.binding = binding;
	}

	public Expression getInitializer() {
		if (null != initializer) {
			return initializer;
		}

		if (null != initializerReference) {
			return initializerReference.getInitializer();
		}

		return null;
	}

	public void setInitializer(Expression initializer) {
		this.initializer = initializer;
	}

	public void setInitializer(VariableBindingManager initializerReference) {
		this.initializerReference = initializerReference;
	}

	public void addMethod(MethodInvocation method) {
		getMethods().add(method);
	}

	public List<MethodInvocation> getMethods() {
		return methods;
	}

}
