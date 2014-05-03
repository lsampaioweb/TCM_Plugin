package net.thecodemaster.sap.verifiers;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

	public SecurityMisconfigurationVerifier() {
		super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_NAME,
				Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID);
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
