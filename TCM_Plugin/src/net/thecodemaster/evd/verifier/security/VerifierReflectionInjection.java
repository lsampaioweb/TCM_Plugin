package net.thecodemaster.evd.verifier.security;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierReflectionInjection extends Verifier {

	public VerifierReflectionInjection() {
		super(Constant.VERIFIER_ID_REFLECTION_INJECTION, Message.Plugin.VERIFIER_NAME_REFLECTION_INJECTION);
	}

}
