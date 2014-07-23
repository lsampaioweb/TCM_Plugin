package net.thecodemaster.evd.verifier.security;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierLogForging extends Verifier {

	public VerifierLogForging() {
		super(Constant.VERIFIER_ID_LOG_FORGING, Message.Plugin.VERIFIER_NAME_LOG_FORGING);
	}

}
