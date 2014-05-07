package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class CookiePoisoningVerifier extends Verifier {

	public CookiePoisoningVerifier(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.NAME_VERIFIER_COOKIE_POISONING, Constant.ID_VERIFIER_COOKIE_POISONING, entryPoints);
	}

}
