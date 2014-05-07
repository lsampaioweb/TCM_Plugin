package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

	public SecurityMisconfigurationVerifier() {
		super(Messages.Plugin.NAME_VERIFIER_SECURITY_MISCONFIGURATION, Constant.ID_VERIFIER_SECURITY_MISCONFIGURATION);
	}

	@Override
	protected String getMessageLiteral(String value) {
		return String.format(Messages.SecurityVulnerabilitiesVerifier.LITERAL, value);
	}

	@Override
	protected String getMessageNullLiteral() {
		return String.format(Messages.SecurityVulnerabilitiesVerifier.NULL_LITERAL);
	}

	@Override
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.SecurityVulnerabilitiesVerifier.ENTRY_POINT_METHOD, value);
	}

}
