package net.thecodemaster.sap.verifiers;

import java.util.List;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class XSSVerifier extends Verifier {

	static {
		// 01 - Loads all the ExitPoints of this verifier.
		loadExitPoints(Constants.Plugin.XSS_VERIFIER_ID);
	}

	public XSSVerifier(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.XSS_VERIFIER_NAME, Constants.Plugin.XSS_VERIFIER_ID, entryPoints);
	}

	@Override
	protected String getMessageLiteral(String value) {
		return "";
	}

	@Override
	protected String getMessageNullLiteral() {
		return "";
	}

	@Override
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.SecurityVulnerabilitiesVerifier.ENTRY_POINT_METHOD, value);
	}

}
