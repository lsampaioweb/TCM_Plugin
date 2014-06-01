package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

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

	@Override
	protected void inspectInfixExpression(int depth, DataFlow dataFlow, InfixExpression expression) {
		// 01 - Informs that this node is a vulnerability.
		dataFlow.isVulnerable(Constant.Vulnerability.SQL_INJECTION_STRING_CONCATENATION, getStringConcatenationMessage());
	}

	@Override
	protected void inspectPrefixExpression(int depth, DataFlow dataFlow, PrefixExpression expression) {
		// 01 - Informs that this node is a vulnerability.
		dataFlow.isVulnerable(Constant.Vulnerability.SQL_INJECTION_STRING_CONCATENATION, getStringConcatenationMessage());
	}

}