package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierCookiePoisoning extends Verifier {

	public VerifierCookiePoisoning(List<EntryPoint> entryPoints) {
		super(Message.Plugin.VERIFIER_NAME_COOKIE_POISONING, Constant.VERIFIER_ID_COOKIE_POISONING, entryPoints);
	}

}
