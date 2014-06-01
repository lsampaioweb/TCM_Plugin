package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
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
	protected void inspectLiteral(int depth, DataFlow dataFlow, Expression node) {
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

		// 01 - Informs that this node is a vulnerability.
		dataFlow.isVulnerable(Constant.Vulnerability.SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT, message);
	}

}
