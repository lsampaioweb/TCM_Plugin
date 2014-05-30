package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierUnvalidatedRedirecting extends Verifier {

	public VerifierUnvalidatedRedirecting() {
		super(Constant.VERIFIER_ID_UNVALIDATED_REDIRECTING, Message.Plugin.VERIFIER_NAME_UNVALIDATED_REDIRECTING);
	}

}
