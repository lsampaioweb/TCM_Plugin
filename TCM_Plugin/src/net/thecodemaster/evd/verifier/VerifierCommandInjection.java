package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierCommandInjection extends Verifier {

	public VerifierCommandInjection(List<EntryPoint> entryPoints) {
		super(Message.Plugin.VERIFIER_NAME_COMMAND_INJECTION, Constant.VERIFIER_ID_COMMAND_INJECTION, entryPoints);
	}

}
