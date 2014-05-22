package net.thecodemaster.evd.graph;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * @author Luciano Sampaio
 */
public class VariableBindingManager {

	private IBinding								binding;
	private Expression							initializer;

	private VariableBindingManager	initializerReference;
	private final List<Expression>	expressions;

	public VariableBindingManager(IBinding binding) {
		this.setBinding(binding);

		expressions = Creator.newList();
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

	public void addMethod(Expression expression) {
		getExpressions().add(expression);
	}

	public List<Expression> getExpressions() {
		return expressions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result *= prime + getBinding().hashCode();
		result *= prime + getInitializer().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param obj
	 *          Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (null == obj) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		VariableBindingManager other = (VariableBindingManager) obj;
		if (getBinding() != other.getBinding()) {
			return false;
		}
		if (getInitializer() != other.getInitializer()) {
			return false;
		}

		return true;
	}

}
