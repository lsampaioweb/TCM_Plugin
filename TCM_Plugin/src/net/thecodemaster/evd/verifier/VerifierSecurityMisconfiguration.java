package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.VulnerabilityPath;
import net.thecodemaster.evd.ui.l10n.Messages;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Luciano Sampaio
 */
public class VerifierSecurityMisconfiguration extends Verifier {

	public VerifierSecurityMisconfiguration() {
		super(Messages.Plugin.VERIFIER_NAME_SECURITY_MISCONFIGURATION, Constant.ID_VERIFIER_SECURITY_MISCONFIGURATION);
	}

	@Override
	protected String getMessageLiteral(String value) {
		return String.format(Messages.VerifierSecurityVulnerability.LITERAL, value);
	}

	@Override
	protected String getMessageNullLiteral() {
		return String.format(Messages.VerifierSecurityVulnerability.NULL_LITERAL);
	}

	@Override
	protected void checkLiteral(VulnerabilityPath vp, Expression expr) {
		String message = null;
		switch (expr.getNodeType()) {
			case ASTNode.STRING_LITERAL:
				message = getMessageLiteral(((StringLiteral) expr).getLiteralValue());
				break;
			case ASTNode.NUMBER_LITERAL:
				message = getMessageLiteral(((NumberLiteral) expr).getToken());
				break;
			case ASTNode.NULL_LITERAL:
				message = getMessageNullLiteral();
				break;
		}

		// 01 - Informs that this node is a vulnerability.
		vp.foundVulnerability(expr, message);
	}

}
