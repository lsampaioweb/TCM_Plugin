package net.thecodemaster.evd.verifier.security;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierCookiePoisoning extends Verifier {

	public VerifierCookiePoisoning() {
		super(Constant.VERIFIER_ID_COOKIE_POISONING, Message.Plugin.VERIFIER_NAME_COOKIE_POISONING);
	}

}
