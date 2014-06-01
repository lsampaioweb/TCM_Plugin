package net.thecodemaster.evd.graph;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.ui.enumeration.EnumStatusVariable;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * @author Luciano Sampaio
 */
public class VariableBindingManager {

	private IBinding								binding;
	private Expression							initializer;
	private EnumStatusVariable			status;
	private DataFlow								dataFlow;

	private final List<Expression>	references;

	public VariableBindingManager(IBinding binding) {
		setBinding(binding);
		this.status = EnumStatusVariable.UNKNOWN;

		references = Creator.newList();
	}

	public IBinding getBinding() {
		return binding;
	}

	private void setBinding(IBinding binding) {
		this.binding = binding;
	}

	public Expression getInitializer() {
		return initializer;
	}

	public void setInitializer(Expression initializer) {
		this.initializer = initializer;
	}

	public void addReferences(Expression reference) {
		getReferences().add(reference);
	}

	public List<Expression> getReferences() {
		return references;
	}

	public EnumStatusVariable status() {
		return status;
	}

	public void setStatus(DataFlow dataFlow, EnumStatusVariable status) {
		this.status = status;
		setDataFlow(dataFlow);
	}

	public DataFlow getDataFlow() {
		return dataFlow;
	}

	private void setDataFlow(DataFlow dataFlow) {
		this.dataFlow = dataFlow;
	}

	/**
	 * {@inheritDoc}
	 */
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

	@Override
	public String toString() {
		return getBinding().toString() + " - " + getInitializer().toString();
	}

}
