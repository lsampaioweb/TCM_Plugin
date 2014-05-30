package net.thecodemaster.evd.verifier;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierPathTraversal extends Verifier {

	public VerifierPathTraversal() {
		super(Constant.VERIFIER_ID_PATH_TRAVERSAL, Message.Plugin.VERIFIER_NAME_PATH_TRAVERSAL);
	}

}
