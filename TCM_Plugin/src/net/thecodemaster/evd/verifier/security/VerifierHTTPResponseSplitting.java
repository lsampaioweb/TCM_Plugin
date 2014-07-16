package net.thecodemaster.evd.verifier.security;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierHTTPResponseSplitting extends Verifier {

	public VerifierHTTPResponseSplitting() {
		super(Constant.VERIFIER_ID_HTTP_RESPONSE_SPLITTING, Message.Plugin.VERIFIER_NAME_HTTP_RESPONSE_SPLITTING);
	}

}
