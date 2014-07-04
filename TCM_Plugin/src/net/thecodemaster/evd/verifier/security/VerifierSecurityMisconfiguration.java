package net.thecodemaster.evd.verifier.security;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.graph.flow.DataFlow;
import net.thecodemaster.evd.graph.flow.Flow;
import net.thecodemaster.evd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.Verifier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Luciano Sampaio
 */
public class VerifierSecurityMisconfiguration extends Verifier {

	public VerifierSecurityMisconfiguration() {
		super(Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION, Message.Plugin.VERIFIER_NAME_SECURITY_MISCONFIGURATION);
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

	@Override
	protected void inspectCharacterLiteral(Flow loopControl, Context context, DataFlow dataFlow, CharacterLiteral node) {
		inspectLiteral(dataFlow, node, getMessageLiteral(node.charValue()));
	}

	@Override
	protected void inspectNullLiteral(Flow loopControl, Context context, DataFlow dataFlow, NullLiteral node) {
		inspectLiteral(dataFlow, node, getMessageNullLiteral());
	}

	@Override
	protected void inspectNumberLiteral(Flow loopControl, Context context, DataFlow dataFlow, NumberLiteral node) {
		inspectLiteral(dataFlow, node, getMessageLiteral(node.getToken()));
	}

	@Override
	protected void inspectStringLiteral(Flow loopControl, Context context, DataFlow dataFlow, StringLiteral node) {
		inspectLiteral(dataFlow, node, getMessageLiteral(node.getLiteralValue()));
	}

	/**
	 * 13, 33, 34, 45
	 */
	private void inspectLiteral(DataFlow dataFlow, ASTNode node, String message) {
		// 01 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
		if (hasMarkerAtPosition(node)) {
			return;
		}

		// 02 - Informs that this node is a vulnerability.
		dataFlow.addNodeToPath((Expression) node).hasVulnerablePath(
				Constant.Vulnerability.SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT, message);
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
