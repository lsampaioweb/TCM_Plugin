package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

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

	// @Override
	// protected void checkLiteral(DataFlow df, Expression expr) {
	// String message = null;
	// switch (expr.getNodeType()) {
	// case ASTNode.STRING_LITERAL:
	// message = getMessageLiteral(((StringLiteral) expr).getLiteralValue());
	// break;
	// case ASTNode.CHARACTER_LITERAL:
	// message = getMessageLiteral(((CharacterLiteral) expr).charValue());
	// break;
	// case ASTNode.NUMBER_LITERAL:
	// message = getMessageLiteral(((NumberLiteral) expr).getToken());
	// break;
	// case ASTNode.NULL_LITERAL:
	// message = getMessageNullLiteral();
	// break;
	// }
	//
	// // 01 - Informs that this node is a vulnerability.
	// df.isVulnerable(Constant.Vulnerability.SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT, message);
	// }

}
