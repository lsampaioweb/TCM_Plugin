package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierXPathInjection extends Verifier {

	public VerifierXPathInjection() {
		super(Constant.VERIFIER_ID_XPATH_INJECTION, Message.Plugin.VERIFIER_NAME_XPATH_INJECTION,
				Constant.VERIFIER_PRIORITY_XPATH_INJECTION);
	}

}
