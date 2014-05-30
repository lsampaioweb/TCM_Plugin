package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierSQLInjection extends Verifier {

	public VerifierSQLInjection(List<EntryPoint> entryPoints) {
		super(Message.Plugin.VERIFIER_NAME_SQL_INJECTION, Constant.VERIFIER_ID_SQL_INJECTION, entryPoints);
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