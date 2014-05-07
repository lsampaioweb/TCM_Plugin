package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Messages;

/**
 * @author Luciano Sampaio
 */
public class VerifierSQLInjection extends Verifier {

	public VerifierSQLInjection(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.VERIFIER_NAME_SQL_INJECTION, Constant.ID_VERIFIER_SQL_INJECTION, entryPoints);
	}

	@Override
	protected String getMessageEntryPoint(String value) {
		return String.format(Messages.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

}
