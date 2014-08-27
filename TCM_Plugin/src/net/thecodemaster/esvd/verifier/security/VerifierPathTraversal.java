package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierPathTraversal extends Verifier {

	public VerifierPathTraversal() {
		super(Constant.VERIFIER_ID_PATH_TRAVERSAL, Message.Plugin.VERIFIER_NAME_PATH_TRAVERSAL,
				Constant.VERIFIER_PRIORITY_PATH_TRAVERSAL);
	}

}
