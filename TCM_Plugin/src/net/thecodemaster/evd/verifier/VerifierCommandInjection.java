package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierCommandInjection extends Verifier {

	public VerifierCommandInjection() {
		super(Constant.VERIFIER_ID_COMMAND_INJECTION, Message.Plugin.VERIFIER_NAME_COMMAND_INJECTION);
	}

}
