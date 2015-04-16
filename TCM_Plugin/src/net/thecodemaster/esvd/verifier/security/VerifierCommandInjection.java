package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierCommandInjection extends Verifier {

	public VerifierCommandInjection() {
		super(Constant.VERIFIER_ID_COMMAND_INJECTION, Message.Plugin.VERIFIER_NAME_COMMAND_INJECTION,
				Constant.VERIFIER_PRIORITY_COMMAND_INJECTION);
	}

}
