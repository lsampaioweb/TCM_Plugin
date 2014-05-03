package net.thecodemaster.sap.verifiers;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class SQLInjectionVerifier extends Verifier {

	public SQLInjectionVerifier() {
		super(Messages.Plugin.SQL_INJECTION_VERIFIER_NAME, Constants.Plugin.SQL_INJECTION_VERIFIER_ID);
	}

}
