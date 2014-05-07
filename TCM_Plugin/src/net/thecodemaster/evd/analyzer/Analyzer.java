package net.thecodemaster.evd.analyzer;

import java.util.List;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.Timer;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.verifier.Verifier;
import net.thecodemaster.evd.xmlloader.LoaderEntryPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Luciano Sampaio
 */
public abstract class Analyzer {

	/**
	 * The list of all verifiers of this analyzer.
	 */
	private final List<Verifier>		verifiers;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static List<EntryPoint>	entryPoints;

	public Analyzer() {
		verifiers = Creator.newList();
	}

	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		for (Verifier verifier : getVerifiers()) {
			if (!userCanceledProcess(reporter)) {
				Timer timer = (new Timer("01.2.1 - Verifier: " + verifier.getName())).start();
				verifier.run(resources, callGraph, reporter);
				PluginLogger.logInfo(timer.stop().toString());
			}
		}
	}

	protected List<Verifier> getVerifiers() {
		return verifiers;
	}

	protected static List<EntryPoint> getEntryPoints() {
		if (null == entryPoints) {
			// Loads all the EntryPoints.
			loadEntryPoints();
		}

		return entryPoints;
	}

	protected static void loadEntryPoints() {
		entryPoints = (new LoaderEntryPoint()).load();
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

}
