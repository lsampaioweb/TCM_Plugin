package net.thecodemaster.evd.verifier.security;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.graph.flow.DataFlow;
import net.thecodemaster.evd.graph.flow.Flow;
import net.thecodemaster.evd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.Verifier;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * @author Luciano Sampaio
 */
public class VerifierSQLInjection extends Verifier {

	public VerifierSQLInjection() {
		super(Constant.VERIFIER_ID_SQL_INJECTION, Message.Plugin.VERIFIER_NAME_SQL_INJECTION);
	}

	@Override
	protected boolean requiredExtraInspection(DataFlow dataFlow) {
		// If the dataflow is ok, we then inspect if there are other types of problems.
		// For example if there is String concatenation.
		return !dataFlow.hasVulnerablePath();
	}

	private String getStringConcatenationMessage() {
		return Message.VerifierSecurityVulnerability.STRING_CONCATENATION;
	}

	/**
	 * 27 <br/>
	 * a + b <br/>
	 * a == b<br/>
	 * a != b
	 */
	@Override
	protected void inspectInfixExpression(Flow loopControl, Context context, DataFlow dataFlow, InfixExpression expression) {
		Operator operator = expression.getOperator();

		if (((null != operator) && (!operator.equals(InfixExpression.Operator.PLUS)))
				|| (!hasVulnerability(loopControl, dataFlow, expression))) {
			super.inspectInfixExpression(loopControl, context, dataFlow, expression);
		}
	}

	private boolean hasVulnerability(Flow loopControl, DataFlow dataFlow, Expression expression) {
		// 01 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
		if (hasMarkerAtPosition(expression)) {
			return false;
		}

		// 02 - Check if this node matches the rules for the current parameter.
		if (!matchRules(getRules(), expression)) {
			// 03 - Informs that this node is a vulnerability.
			dataFlow.addNodeToPath(expression).hasVulnerablePath(Constant.Vulnerability.SQL_INJECTION_STRING_CONCATENATION,
					getStringConcatenationMessage());
			return true;
		}

		return false;
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(Flow loopControl, Context context, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
		if ((null != getRules())
				&& ((null != variableBinding) && (variableBinding.getStatus().equals(EnumVariableStatus.NOT_VULNERABLE)))) {

			// The SQL Injection verifier also needs to know if the variable has its content from a string concatenation.
			inspectNode(loopControl, context, dataFlow, variableBinding.getInitializer());
		}
	}

}