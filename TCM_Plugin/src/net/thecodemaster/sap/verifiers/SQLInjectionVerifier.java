package net.thecodemaster.sap.verifiers;

import java.util.List;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class SQLInjectionVerifier extends Verifier {

	public SQLInjectionVerifier(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.SQL_INJECTION_VERIFIER_NAME, Constants.Plugin.SQL_INJECTION_VERIFIER_ID, entryPoints);
	}

	@Override
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.SecurityVulnerabilitiesVerifier.ENTRY_POINT_METHOD, value);
	}

}
