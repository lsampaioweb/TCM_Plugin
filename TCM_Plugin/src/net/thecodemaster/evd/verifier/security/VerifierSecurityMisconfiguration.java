package net.thecodemaster.evd.verifier.security;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.Verifier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
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

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(int depth, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
		if ((null != variableBinding) && (variableBinding.status().equals(EnumVariableStatus.NOT_VULNERABLE))) {
			// The SQL Injection verifier also needs to know if the variable has its content from a string concatenation.
			inspectNode(depth, dataFlow, variableBinding.getInitializer());
		} else {
			super.inspectSimpleName(depth, dataFlow, expression, variableBinding);
		}
	}

	/**
	 * 13, 33, 34, 45
	 */
	@Override
	protected void inspectLiteral(int depth, DataFlow dataFlow, Expression node) {
		// 01 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
		if (hasMarkerAtPosition(node)) {
			return;
		}

		String message = null;
		switch (node.getNodeType()) {
			case ASTNode.STRING_LITERAL:
				message = getMessageLiteral(((StringLiteral) node).getLiteralValue());
				break;
			case ASTNode.CHARACTER_LITERAL:
				message = getMessageLiteral(((CharacterLiteral) node).charValue());
				break;
			case ASTNode.NUMBER_LITERAL:
				message = getMessageLiteral(((NumberLiteral) node).getToken());
				break;
			case ASTNode.NULL_LITERAL:
				message = getMessageNullLiteral();
				break;
		}

		// 02 - Informs that this node is a vulnerability.
		dataFlow.addNodeToPath(node).isVulnerable(Constant.Vulnerability.SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT,
				message);
	}

}
