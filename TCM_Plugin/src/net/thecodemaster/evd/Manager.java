package net.thecodemaster.evd;

import java.util.List;

import net.thecodemaster.evd.analyzer.Analyzer;
import net.thecodemaster.evd.analyzer.AnalyzerSecurityVulnerability;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.reporter.Reporter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class knows which options (settings, verifiers and etc.) were selected by the developer (our user). <br/>
 * The Manager iterates over all the Analyzers and invokes the run method. <br/>
 * This has to be a singleton class.
 * 
 * @author Luciano Sampaio
 */
public class Manager {

	private static Manager				instance	= null;
	/**
	 * The list with all the implemented analyzers.
	 */
	private final List<Analyzer>	analyzers;
	/**
	 * The object that know where and how to report the found vulnerabilities.
	 */
	private Reporter							reporter;

	/**
	 * Default constructor.
	 */
	private Manager() {
		analyzers = Creator.newList();
	}

	/**
	 * The user has changed some options from the tool. It is necessary to reset the list of analyzers and where to report
	 * the vulnerabilities.
	 */
	public static void reset() {
		instance = null;
	}

	/**
	 * Creates one instance of the Manager class if it was not created before. <br/>
	 * After that always return the same instance of the Manager class.
	 * 
	 * @return Return the same instance of the Manager class.
	 */
	public static Manager getInstance() {
		if (instance == null) {
			synchronized (Manager.class) {
				if (instance == null) {
					instance = new Manager();

					IPreferenceStore store = Activator.getDefault().getPreferenceStore();

					instance.addAnalyzers(store);
					instance.addOutputs(store);
				}
			}
		}
		return instance;
	}

	/**
	 * Based on the options selected by the user, the analyzers are added to the list of analyzers that are going to be
	 * invoked when the plug-in runs.
	 * 
	 * @param store
	 *          The IPreferenceStore interface represents a table mapping named preferences to values.
	 */
	private void addAnalyzers(IPreferenceStore store) {
		// Get the options checked by the developer.
		boolean commandInjection = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_COMMAND_INJECTION);
		boolean cookiePoisoning = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_COOKIE_POISONING);
		boolean crossSiteScripting = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_CROSS_SITE_SCRIPTING);
		boolean pathTraversal = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_PATH_TRAVERSAL);
		boolean securityMisconfiguration = store
				.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_SECURITY_MISCONFIGURATION);
		boolean sqlInjection = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_SQL_INJECTION);
		boolean unvalidatedRedirecting = store
				.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_UNVALIDATED_REDIRECTING);

		// If at least one was selected, the analyzer is added to the list.
		if (commandInjection || cookiePoisoning || crossSiteScripting || pathTraversal || securityMisconfiguration
				|| sqlInjection || unvalidatedRedirecting) {
			addAnalyzer(new AnalyzerSecurityVulnerability(commandInjection, cookiePoisoning, crossSiteScripting,
					pathTraversal, securityMisconfiguration, sqlInjection, unvalidatedRedirecting));
		}
	}

	/**
	 * Add the provided analyzer to the list of analyzers that will be invoked when the plug-in runs.
	 * 
	 * @param analyzer
	 *          The analyzer that will be added to the list.
	 */
	private void addAnalyzer(Analyzer analyzer) {
		analyzers.add(analyzer);
	}

	/**
	 * Based on the options selected by the user, the outputs are added to the reporter. {@link Reporter}
	 * 
	 * @param store
	 *          The IPreferenceStore interface represents a table mapping named preferences to values.
	 */
	private void addOutputs(IPreferenceStore store) {
		boolean problemView = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_PROBLEMS_VIEW);
		boolean textFile = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_TEXT_FILE);
		boolean xmlFile = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_XML_FILE);

		reporter = new Reporter(problemView, textFile, xmlFile);
	}

	/**
	 * @param progressMonitor
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		reporter.setProgressMonitor(progressMonitor);
	}

	/**
	 * @param resources
	 * @param callGraph
	 */
	public void run(List<IResource> resources, CallGraph callGraph) {
		// 01 - Any Analyzer or its verifiers can add markers, so we first need to clean the old values.
		Reporter.clearOldProblems(resources);

		for (Analyzer analyzer : analyzers) {
			analyzer.run(resources, callGraph, reporter);
		}
	}

}
