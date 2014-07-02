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
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * @author Luciano Sampaio
 */
public class VerifierSQLInjection extends Verifier {

	public VerifierSQLInjection() {
		super(Constant.VERIFIER_ID_SQL_INJECTION, Message.Plugin.VERIFIER_NAME_SQL_INJECTION);
	}

	private String getStringConcatenationMessage() {
		return Message.VerifierSecurityVulnerability.STRING_CONCATENATION;
	}

	/**
	 * 27
	 */
	@Override
	protected void inspectInfixExpression(Flow loopControl, Context context, DataFlow dataFlow, InfixExpression expression) {
		processStringConcatenation(dataFlow, expression);
	}

	/**
	 * 37
	 */
	@Override
	protected void inspectPostfixExpression(Flow loopControl, Context context, DataFlow dataFlow,
			PostfixExpression expression) {
		processStringConcatenation(dataFlow, expression);
	}

	/**
	 * 38
	 */
	@Override
	protected void inspectPrefixExpression(Flow loopControl, Context context, DataFlow dataFlow,
			PrefixExpression expression) {
		processStringConcatenation(dataFlow, expression);
	}

	private void processStringConcatenation(DataFlow dataFlow, Expression expression) {
		// 01 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
		if (hasMarkerAtPosition(expression)) {
			return;
		}

		// 01 - Informs that this node is a vulnerability.
		dataFlow.addNodeToPath(expression).hasVulnerablePath(Constant.Vulnerability.SQL_INJECTION_STRING_CONCATENATION,
				getStringConcatenationMessage());
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(Flow loopControl, Context context, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
		if ((null != variableBinding) && (variableBinding.getStatus().equals(EnumVariableStatus.NOT_VULNERABLE))) {
			// The SQL Injection verifier also needs to know if the variable has its content from a string concatenation.
			inspectNode(loopControl, context, dataFlow, variableBinding.getInitializer());
		} else {
			super.inspectSimpleName(loopControl, context, dataFlow, expression, variableBinding);
		}
	}

}