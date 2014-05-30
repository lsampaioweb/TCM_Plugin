package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierCrossSiteScripting extends Verifier {

	public VerifierCrossSiteScripting() {
		super(Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING, Message.Plugin.VERIFIER_NAME_CROSS_SITE_SCRIPTING);
	}

}
