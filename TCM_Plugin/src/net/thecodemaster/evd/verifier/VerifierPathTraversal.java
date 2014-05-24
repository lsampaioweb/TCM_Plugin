package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public class VerifierPathTraversal extends Verifier {

	public VerifierPathTraversal(List<EntryPoint> entryPoints) {
		super(Message.Plugin.VERIFIER_NAME_PATH_TRAVERSAL, Constant.VERIFIER_ID_PATH_TRAVERSAL, entryPoints);
	}

}
