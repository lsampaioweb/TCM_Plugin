package net.thecodemaster.evd.verifier;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Timer;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.reporter.Reporter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * This class performs its operations in a different(new) thread from the UI Thread. So the user will not be blocked.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class VerifierJob extends Job {

	private Verifier																							verifier;
	private Reporter																							reporter;
	private CallGraph																							callGraph;
	private Map<IResource, Map<MethodDeclaration, List<ASTNode>>>	resourcesAndMethodsToProcess;

	public VerifierJob(String name) {
		super(name);
	}

	public void run(Reporter reporter, CallGraph callGraph,
			Map<IResource, Map<MethodDeclaration, List<ASTNode>>> resourcesAndMethodsToProcess, Verifier verifier) {
		setReporter(reporter);
		setCallGraph(callGraph);
		setResourcesAndMethodsToProcess(resourcesAndMethodsToProcess);
		setVerifier(verifier);

		schedule();
	}

	protected Verifier getVerifier() {
		return verifier;
	}

	protected void setVerifier(Verifier verifier) {
		this.verifier = verifier;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	protected void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	protected CallGraph getCallGraph() {
		return callGraph;
	}

	protected void setCallGraph(CallGraph callGraph) {
		this.callGraph = callGraph;
	}

	protected Map<IResource, Map<MethodDeclaration, List<ASTNode>>> getResourcesAndMethodsToProcess() {
		return resourcesAndMethodsToProcess;
	}

	protected void setResourcesAndMethodsToProcess(
			Map<IResource, Map<MethodDeclaration, List<ASTNode>>> resourcesAndMethodsToProcess) {
		this.resourcesAndMethodsToProcess = resourcesAndMethodsToProcess;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			Timer timer = (new Timer("01.3.1 - Verifier: " + getVerifier().getName())).start();
			// getVerifier().run(getResourcesAndMethodsToProcess(), getCallGraph(), getReporter());
			PluginLogger.logIfDebugging(timer.stop().toString());
		} catch (Exception e) {
			PluginLogger.logError(e);
			return Status.CANCEL_STATUS;
		} finally {
			if (null != monitor) {
				monitor.done();
			}
		}

		return Status.OK_STATUS;
	}

}
