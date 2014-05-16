package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierUnvalidatedRedirecting extends Verifier {

	public VerifierUnvalidatedRedirecting(List<EntryPoint> entryPoints) {
		super(Message.Plugin.VERIFIER_NAME_UNVALIDATED_REDIRECTING, Constant.VERIFIER_ID_UNVALIDATED_REDIRECTING,
				entryPoints);
	}

}
