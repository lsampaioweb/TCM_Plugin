package net.thecodemaster.sap;

import java.util.Collection;

import net.thecodemaster.sap.analyzers.Analyzer;
import net.thecodemaster.sap.analyzers.CodeAnomaliesAnalyzer;
import net.thecodemaster.sap.analyzers.SecurityVulnerabilityAnalyzer;
import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.reporter.Reporter;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This has to be a singleton class.
 * 
 * @author Luciano Sampaio
 */
public class Manager implements IResourceVisitor, IResourceDeltaVisitor {

  // This object controls which analyzers are going to be executed to perform the security vulnerability
  // detection.
  private static volatile Manager instance = null;
  private Collection<Analyzer>    analyzers;
  private Reporter                reporter;

  private Manager() {
  }

  public static void resetManager() {
    instance = null;
  }

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

  private void addAnalyzers(IPreferenceStore store) {
    // Get the options checked by the developer.
    boolean cookiePoisoning = store.getBoolean(Constants.SecurityVulnerabilities.FIELD_COOKIE_POISONING);
    boolean crossSiteScripting =
      store.getBoolean(Constants.SecurityVulnerabilities.FIELD_CROSS_SITE_SCRIPTING);
    boolean sqlInjection = store.getBoolean(Constants.SecurityVulnerabilities.FIELD_SQL_INJECTION);
    boolean securityMisconfiguration =
      store.getBoolean(Constants.SecurityVulnerabilities.FIELD_SECURITY_MISCONFIGURATION);

    // If at least one was selected, the analyzer is added to the list.
    if (cookiePoisoning || crossSiteScripting || sqlInjection || securityMisconfiguration) {
      addAnalyzer(new SecurityVulnerabilityAnalyzer(cookiePoisoning, crossSiteScripting, sqlInjection,
        securityMisconfiguration));
    }

    boolean nameConvention = true;
    // If at least one was selected, the analyzer is added to the list.
    if (nameConvention) {
      addAnalyzer(new CodeAnomaliesAnalyzer(nameConvention));
    }
  }

  private void addOutputs(IPreferenceStore store) {
    boolean problemView = store.getBoolean(Constants.Settings.FIELD_OUTPUT_PROBLEMS_VIEW);
    boolean textFile = store.getBoolean(Constants.Settings.FIELD_OUTPUT_TEXT_FILE);
    boolean xmlFile = store.getBoolean(Constants.Settings.FIELD_OUTPUT_XML_FILE);

    reporter = new Reporter(problemView, textFile, xmlFile);
  }

  /**
   * @param progressMonitor
   */
  public void setProgressMonitor(IProgressMonitor progressMonitor) {
    reporter.setProgressMonitor(progressMonitor);
  }

  /**
   * @param analyzer
   */
  private void addAnalyzer(Analyzer analyzer) {
    if (null == analyzers) {
      analyzers = Creator.newCollection();
    }

    analyzers.add(analyzer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
    IResource resource = delta.getResource();

    switch (delta.getKind()) {
      case IResourceDelta.REMOVED:
        // TODO
        break;
      case IResourceDelta.ADDED:
      case IResourceDelta.CHANGED:
        visit(resource);
        break;
    }
    // Return true to continue visiting children.
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResource resource) throws CoreException {
    for (Analyzer analyzer : analyzers) {
      analyzer.run(resource, reporter);
    }

    // Return true to continue visiting children.
    return true;
  }

}
