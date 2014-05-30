package net.thecodemaster.evd.analyzer;

import java.util.List;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.Timer;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.verifier.Verifier;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

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
	 * @param resources
	 *          The list of modified resources that needs to be verified.
	 * @param callGraph
	 *          The object that contains the callGraph of all methods and variables of the analyzed source code.
	 *          {@link CallGraph}
	 * @param reporter
	 *          The object that know where and how to displayed the found vulnerabilities. {@link Reporter}
	 */
	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		// 01 - Iterate over the list of verifiers.
		for (Verifier verifier : getVerifiers()) {

			// 02 - Before invoking the run method of the current verifier, it is important to check if the user canceled the
			// process.
			if (!userCanceledProcess(reporter)) {
				Timer timer = (new Timer("01.3.1 - Verifier: " + verifier.getName())).start();
				verifier.run(resources, callGraph, reporter);
				PluginLogger.logIfDebugging(timer.stop().toString());
			}
		}
	}

}
