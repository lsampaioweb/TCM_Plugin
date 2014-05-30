package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

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

	// @Override
	// protected void checkInfixExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
	// // 01 - Informs that this node is a vulnerability.
	// df.isVulnerable(Constant.Vulnerability.SQL_INJECTION_STRING_CONCATENATION, getStringConcatenationMessage());
	// }
	//
	// @Override
	// protected void checkPrefixExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
	// // 01 - Informs that this node is a vulnerability.
	// df.isVulnerable(Constant.Vulnerability.SQL_INJECTION_STRING_CONCATENATION, getStringConcatenationMessage());
	// }
}