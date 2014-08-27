package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierReflectionInjection extends Verifier {

	public VerifierReflectionInjection() {
		super(Constant.VERIFIER_ID_REFLECTION_INJECTION, Message.Plugin.VERIFIER_NAME_REFLECTION_INJECTION,
				Constant.VERIFIER_PRIORITY_REFLECTION_INJECTION);
	}

}
