package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.graph.VariableBinding;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.ui.enumeration.EnumRules;
import net.thecodemaster.esvd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Luciano Sampaio
 */
public class VerifierSecurityMisconfiguration extends Verifier {

	public VerifierSecurityMisconfiguration() {
		super(Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION, Message.Plugin.VERIFIER_NAME_SECURITY_MISCONFIGURATION,
				Constant.VERIFIER_PRIORITY_SECURITY_MISCONFIGURATION);
	}

	@Override
	protected boolean requiredExtraInspection(DataFlow dataFlow) {
		// If the dataflow is ok, we then inspect if there are other types of problems.
		// For example if there is hard coded code.
		return !dataFlow.hasVulnerablePath();
	}

	protected String getMessageLiteral(String value) {
		return String.format(Message.VerifierSecurityVulnerability.LITERAL, value);
	}

	protected String getMessageLiteral(char value) {
		return String.format(Message.VerifierSecurityVulnerability.LITERAL, value);
	}

	protected String getMessageNullLiteral() {
		return String.format(Message.VerifierSecurityVulnerability.NULL_LITERAL);
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
		if ((-1 == getRules()) || (operator.equals(InfixExpression.Operator.PLUS))) {
			super.inspectInfixExpression(loopControl, context, dataFlow, expression);
		} else {
			super.inspectInfixExpression(loopControl, context, new DataFlow(expression), expression);
		}
	}

	@Override
	protected void inspectCharacterLiteral(Flow loopControl, Context context, DataFlow dataFlow, CharacterLiteral node) {
		if (!hasVulnerability(loopControl, dataFlow, node, getMessageLiteral(node.charValue()))) {
			super.inspectCharacterLiteral(loopControl, context, dataFlow, node);
		}
	}

	@Override
	protected void inspectNullLiteral(Flow loopControl, Context context, DataFlow dataFlow, NullLiteral node) {
		if (!hasVulnerability(loopControl, dataFlow, node, getMessageNullLiteral())) {
			super.inspectNullLiteral(loopControl, context, dataFlow, node);
		}
	}

	@Override
	protected void inspectNumberLiteral(Flow loopControl, Context context, DataFlow dataFlow, NumberLiteral node) {
		if (!hasVulnerability(loopControl, dataFlow, node, getMessageLiteral(node.getToken()))) {
			super.inspectNumberLiteral(loopControl, context, dataFlow, node);
		}
	}

	@Override
	protected void inspectStringLiteral(Flow loopControl, Context context, DataFlow dataFlow, StringLiteral node) {
		if (!hasVulnerability(loopControl, dataFlow, node, getMessageLiteral(node.getLiteralValue()))) {
			super.inspectStringLiteral(loopControl, context, dataFlow, node);
		}
	}

	/**
	 * 13, 33, 34, 45
	 */
	private boolean hasVulnerability(Flow loopControl, DataFlow dataFlow, ASTNode node, String message) {
		// 01 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
		if (hasMarkerAtPosition(node)) {
			return false;
		}

		// 02 - Check if this node matches the rules for the current parameter.
		if (!matchRules(getRules(), node)) {
			// 03 - Informs that this node is a vulnerability.
			dataFlow.addNodeToPath((Expression) node).hasVulnerablePath(
					Constant.Vulnerability.SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT, message);
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
		if ((EnumRules.ANYTHING_IS_VALID.value() != getRules())
				&& ((null != variableBinding) && (variableBinding.getStatus().equals(EnumVariableStatus.NOT_VULNERABLE)))) {

			// The SQL Injection verifier also needs to know if the variable has its content from a string concatenation.
			inspectNode(loopControl, context, dataFlow, variableBinding.getInitializer());
		}
	}

}
