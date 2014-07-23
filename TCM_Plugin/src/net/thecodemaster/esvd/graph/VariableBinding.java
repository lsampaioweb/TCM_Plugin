package net.thecodemaster.esvd.graph;

import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.esvd.ui.enumeration.EnumVariableType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * @author Luciano Sampaio
 */
public class VariableBinding {

	private IBinding										binding;
	private Expression									initializer;
	private EnumVariableStatus					status;
	private EnumVariableType						type;
	private DataFlow										dataFlow;

	private final ReferenceManager	referenceManager;

	public VariableBinding(IBinding binding, EnumVariableType type, Expression initializer) {
		setBinding(binding);
		setType(type);
		setInitializer(initializer);
		setStatus(EnumVariableStatus.UNKNOWN);

		referenceManager = new ReferenceManager();
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

	private void setInitializer(Expression initializer) {
		this.initializer = initializer;
	}

	public void addReference(ASTNode reference, int contextId) {
		getReferenceManager().add(reference, contextId);
	}

	public ReferenceManager getReferenceManager() {
		return referenceManager;
	}

	public EnumVariableStatus getStatus() {
		return status;
	}

	public VariableBinding setStatus(EnumVariableStatus status) {
		this.status = status;

		return this;
	}

	public EnumVariableType getType() {
		return type;
	}

	public void setType(EnumVariableType type) {
		this.type = type;
	}

	public DataFlow getDataFlow() {
		return dataFlow;
	}

	public VariableBinding setDataFlow(DataFlow dataFlow) {
		this.dataFlow = dataFlow;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result *= prime + ((null != getBinding()) ? getBinding().hashCode() : 1);
		result *= prime + ((null != getInitializer()) ? getInitializer().hashCode() : 2);
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

		VariableBinding other = (VariableBinding) obj;
		if (!getBinding().equals(other.getBinding())) {
			return false;
		}
		if (!getInitializer().equals(other.getInitializer())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		String binding = (null != getBinding()) ? getBinding().toString() : "";
		String initializer = (null != getInitializer()) ? getInitializer().toString() : "";

		return String.format("%s - %s - %s - %s", binding, getType(), initializer, getStatus());
	}

}
