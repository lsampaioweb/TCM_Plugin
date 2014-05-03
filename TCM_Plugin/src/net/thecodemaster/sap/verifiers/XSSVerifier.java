package net.thecodemaster.sap.verifiers;

import java.util.List;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class XSSVerifier extends Verifier {

	public XSSVerifier(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.XSS_VERIFIER_NAME, Constants.Plugin.XSS_VERIFIER_ID, entryPoints);
	}

	@Override
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.SecurityVulnerabilitiesVerifier.ENTRY_POINT_METHOD, value);
	}

}
