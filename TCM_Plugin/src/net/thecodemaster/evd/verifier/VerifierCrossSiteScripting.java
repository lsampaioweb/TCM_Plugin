package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierCrossSiteScripting extends Verifier {

	public VerifierCrossSiteScripting(List<EntryPoint> entryPoints) {
		super(Message.Plugin.VERIFIER_NAME_CROSS_SITE_SCRIPTING, Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING, entryPoints);
	}

}
