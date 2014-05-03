package net.thecodemaster.sap.verifiers;

import java.util.List;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class CookiePoisoningVerifier extends Verifier {

	public CookiePoisoningVerifier(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.COOKIE_POISONING_VERIFIER_NAME, Constants.Plugin.COOKIE_POISONING_VERIFIER_ID, entryPoints);
	}

}
