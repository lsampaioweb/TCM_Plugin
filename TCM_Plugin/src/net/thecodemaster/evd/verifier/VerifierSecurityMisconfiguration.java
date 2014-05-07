package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Messages;

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
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

}
