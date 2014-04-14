package net.thecodemaster.sap.analyzers;

import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.verifiers.code.anomalies.NameConventionVerifier;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Luciano Sampaio
 */
public class CodeAnomaliesAnalyzer extends Analyzer {

  public CodeAnomaliesAnalyzer(boolean nameConvention) {
    verifiers = Creator.newCollection();

    if (nameConvention) {
      verifiers.add(new NameConventionVerifier());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean run(IResource resource, Reporter reporter) throws CoreException {
    // Return true to continue visiting children.
    return true;
  }

}
