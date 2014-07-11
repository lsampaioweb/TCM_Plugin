package net.thecodemaster.evd.analyzer;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.verifier.Verifier;
import net.thecodemaster.evd.verifier.VerifierJob;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * The Analyzer is like a category of one or several verifiers. On this first version we only have a
 * SecurityVulnerability Analyzer but in the future we might add others.
 * 
 * @author Luciano Sampaio
 */
public abstract class Analyzer {

	/**
	 * The list of all verifiers of this analyzer.
	 */
	private final List<Verifier>	verifiers;

	public Analyzer() {
		verifiers = Creator.newList();
	}

	protected List<Verifier> getVerifiers() {
		return verifiers;
	}

	/**
	 * Returns whether cancellation of current operation has been requested
	 * 
	 * @param reporter
	 * @return true if cancellation has been requested, and false otherwise.
	 */
	private boolean userCanceledProcess(Reporter reporter) {
		IProgressMonitor monitor = reporter.getProgressMonitor();

		return ((null != monitor) && (monitor.isCanceled()));
	}

	/**
	 * This method will iterate over all the Verifiers of this Analyzer and start invoking each of them.
	 * 
	 * @param reporter
	 *          The object that know where and how to displayed the found vulnerabilities. {@link Reporter}
	 * @param callGraph
	 *          The object that contains the callGraph of all methods and variables of the analyzed source code.
	 *          {@link CallGraph}
	 * @param resources
	 *          The list of modified resources that needs to be verified.
	 */
	public void run(Reporter reporter, CallGraph callGraph, List<IResource> resources) {
		if (getVerifiers().size() > 0) {
			Map<IResource, Map<MethodDeclaration, List<ASTNode>>> resourcesAndMethodsToProcess = getVerifiers().get(0)
					.getMethodsToProcess(callGraph, resources);

			// 04 - Iterate over the list of verifiers.
			for (Verifier verifier : getVerifiers()) {

				// 05 - Before invoking the run method of the current verifier,
				// it is important to check if the user canceled the process.
				if (!userCanceledProcess(reporter)) {
					VerifierJob jobDelta = new VerifierJob(verifier.getName());
					jobDelta.run(verifier, reporter, callGraph, resourcesAndMethodsToProcess);
					// verifier.run(reporter, callGraph, resourcesAndMethodsToProcess);
				}
			}
		}
	}

}
