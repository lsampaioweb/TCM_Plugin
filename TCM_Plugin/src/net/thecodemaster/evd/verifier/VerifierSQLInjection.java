package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class VerifierSQLInjection extends Verifier {

	public VerifierSQLInjection(List<EntryPoint> entryPoints) {
		super(Message.Plugin.VERIFIER_NAME_SQL_INJECTION, Constant.VERIFIER_ID_SQL_INJECTION, entryPoints);
	}

	@Override
	protected void checkInfixExpression(DataFlow df, List<Integer> rules, Expression expr, int depth) {
		// 01 - Informs that this node is a vulnerability.
		df.isVulnerable(expr, "SQL concatenation");
	}

	@Override
	protected void checkPrefixExpression(DataFlow df, List<Integer> rules, Expression expr, int depth) {
		// 01 - Informs that this node is a vulnerability.
		df.isVulnerable(expr, "SQL concatenation");
	}

}
