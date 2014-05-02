package net.thecodemaster.sap.analyzers;

import java.util.List;

import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.utils.Timer;
import net.thecodemaster.sap.verifiers.Verifier;
import net.thecodemaster.sap.xmlloaders.EntryPointLoader;

import org.eclipse.core.resources.IResource;

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
			Timer timer = (new Timer("01.2.1 - Verifier: " + verifier.getName())).start();
			verifier.run(resources, callGraph, reporter);
			PluginLogger.logInfo(timer.stop().toString());
		}
	}

	protected List<Verifier> getVerifiers() {
		return verifiers;
	}

	protected static List<EntryPoint> getEntryPoints() {
		return entryPoints;
	}

	protected static void loadEntryPoints() {
		entryPoints = (new EntryPointLoader()).load();
	}

}
