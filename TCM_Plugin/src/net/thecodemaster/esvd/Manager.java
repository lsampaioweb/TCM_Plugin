package net.thecodemaster.esvd;

import java.util.List;

import net.thecodemaster.esvd.analyzer.Analyzer;
import net.thecodemaster.esvd.analyzer.security.AnalyzerSecurityVulnerability;
import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.reporter.Reporter;
import net.thecodemaster.esvd.reporter.ReporterTextFile;
import net.thecodemaster.esvd.reporter.ReporterView;
import net.thecodemaster.esvd.reporter.ReporterXmlFile;

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

	public Reporter getReporter() {
		return reporter;
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
		boolean httpResponseSplitting = store
				.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_HTTP_RESPONSE_SPLITTING);
		boolean ldapInjection = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_LDAP_INJECTION);
		boolean logForging = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_LOG_FORGING);
		boolean pathTraversal = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_PATH_TRAVERSAL);
		boolean reflectionInjection = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_REFLECTION_INJECTION);
		boolean securityMisconfiguration = store
				.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_SECURITY_MISCONFIGURATION);
		boolean sqlInjection = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_SQL_INJECTION);
		boolean xpathInjection = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_XPATH_INJECTION);

		// If at least one was selected, the analyzer is added to the list.
		if (commandInjection || cookiePoisoning || crossSiteScripting || httpResponseSplitting || ldapInjection
				|| logForging || pathTraversal || reflectionInjection || securityMisconfiguration || sqlInjection
				|| xpathInjection) {
			addAnalyzer(new AnalyzerSecurityVulnerability(commandInjection, cookiePoisoning, crossSiteScripting,
					httpResponseSplitting, ldapInjection, logForging, pathTraversal, reflectionInjection,
					securityMisconfiguration, sqlInjection, xpathInjection));
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
		boolean securityView = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_SECURITY_VIEW);
		boolean textFile = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_TEXT_FILE);
		boolean xmlFile = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_XML_FILE);

		Reporter.reset();
		reporter = Reporter.getInstance();
		if (securityView) {
			getReporter().addReporter(new ReporterView());
		}
		if (textFile) {
			getReporter().addReporter(new ReporterTextFile());
		}
		if (xmlFile) {
			getReporter().addReporter(new ReporterXmlFile());
		}
	}

	/**
	 * Check if the user has selected at least one verifier, if not there is nothing to do.
	 * 
	 * @return True if the verifications should be performed, otherwise false.
	 */
	public boolean shouldPerformVerifications() {
		return (analyzers.size() > 0);
	}

	public void run(List<IResource> resources, CallGraph callGraph, IProgressMonitor monitor) {
		// 01 - With the progress monitor, the report is able to let the user know that the plug-in is working on
		// something.
		getReporter().setProgressMonitor(monitor);

		// 02 - Any Analyzer or its verifiers can add markers or files, so we first need to clean the old values.
		getReporter().clearOldProblems(resources);

		// Iterate over the list of analyzers.
		for (Analyzer analyzer : analyzers) {
			analyzer.run(resources, callGraph, getReporter());
		}
	}

}
