package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierCrossSiteScripting extends Verifier {

	public VerifierCrossSiteScripting() {
		super(Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING, Message.Plugin.VERIFIER_NAME_CROSS_SITE_SCRIPTING,
				Constant.VERIFIER_PRIORITY_CROSS_SITE_SCRIPTING);
	}

}
