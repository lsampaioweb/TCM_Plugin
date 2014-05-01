package net.thecodemaster.sap.verifiers;

import java.util.List;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

	static {
		// 01 - Loads all the ExitPoints of this verifier.
		loadExitPoints(Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID);
	}

	public SecurityMisconfigurationVerifier(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_NAME,
				Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID, entryPoints);
	}

	@Override
	protected String getMessageLiteral(String value) {
		return String.format(Messages.SecurityMisconfigurationVerifier.LITERAL, value);
	}

	@Override
	protected String getMessageNullLiteral() {
		return String.format(Messages.SecurityMisconfigurationVerifier.NULL_LITERAL);
	}

	@Override
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.SecurityMisconfigurationVerifier.ENTRY_POINT_METHOD, value);
	}

}
