package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierLogForging extends Verifier {

	public VerifierLogForging() {
		super(Constant.VERIFIER_ID_LOG_FORGING, Message.Plugin.VERIFIER_NAME_LOG_FORGING,
				Constant.VERIFIER_PRIORITY_LOG_FORGING);
	}

}
