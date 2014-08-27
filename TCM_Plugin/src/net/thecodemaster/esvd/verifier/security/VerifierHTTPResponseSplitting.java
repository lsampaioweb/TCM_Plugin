package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierHTTPResponseSplitting extends Verifier {

	public VerifierHTTPResponseSplitting() {
		super(Constant.VERIFIER_ID_HTTP_RESPONSE_SPLITTING, Message.Plugin.VERIFIER_NAME_HTTP_RESPONSE_SPLITTING,
				Constant.VERIFIER_PRIORITY_HTTP_RESPONSE_SPLITTING);
	}

}
