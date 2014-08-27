package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierCookiePoisoning extends Verifier {

	public VerifierCookiePoisoning() {
		super(Constant.VERIFIER_ID_COOKIE_POISONING, Message.Plugin.VERIFIER_NAME_COOKIE_POISONING,
				Constant.VERIFIER_PRIORITY_COOKIE_POISONING);
	}

}
