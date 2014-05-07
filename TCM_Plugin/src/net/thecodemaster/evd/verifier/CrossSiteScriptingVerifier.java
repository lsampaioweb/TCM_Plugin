package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class CrossSiteScriptingVerifier extends Verifier {

	public CrossSiteScriptingVerifier(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.NAME_VERIFIER_CROSS_SITE_SCRIPTING, Constant.ID_VERIFIER_CROSS_SITE_SCRIPTING, entryPoints);
	}

	@Override
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.SecurityVulnerabilitiesVerifier.ENTRY_POINT_METHOD, value);
	}

}
