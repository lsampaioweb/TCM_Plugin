package net.thecodemaster.evd.visitor;

import java.util.List;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.CodeAnalyzer;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * The main responsibility of this class is to find entry-points and vulnerable variables.
 * 
 * @author Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class VisitorPointsToAnalysis extends CodeAnalyzer {

	@Override
	protected String getSubTaskMessage() {
		return Message.Plugin.VISITOR_POINTS_TO_ANALYSIS_SUB_TASK + getCurrentResource().getName();
	}

	@Override
	public void run(IProgressMonitor monitor, CallGraph callGraph, List<IResource> resources) {
		super.run(monitor, callGraph, resources);
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	@Override
	protected void run(int depth, MethodDeclaration methodDeclaration, Expression invoker) {
		PluginLogger.logIfDebugging("Method:" + methodDeclaration.getName());

		// 02 - TODO -
		// If there is a invoker we have to add the parameters and do more stuff.

		// - Create a context for this method.
		Context context = getCallGraph().newContext(getCurrentResource(), methodDeclaration, invoker);

		// 03 - Start the detection on each and every line of this method.
		// inspectNode(depth, context, new DataFlow(methodDeclaration.getName()), methodDeclaration.getBody());
	}

}
