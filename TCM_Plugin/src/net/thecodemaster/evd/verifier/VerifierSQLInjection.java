package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.VulnerabilityPath;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Messages;

import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class VerifierSQLInjection extends Verifier {

	public VerifierSQLInjection(List<EntryPoint> entryPoints) {
		super(Messages.Plugin.VERIFIER_NAME_SQL_INJECTION, Constant.ID_VERIFIER_SQL_INJECTION, entryPoints);
	}

	@Override
	protected void checkInfixExpression(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
		// 01 - Informs that this node is a vulnerability.
		vp.foundVulnerability(expr, "SQL concatenation");
	}

}
