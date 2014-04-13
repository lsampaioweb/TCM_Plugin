package net.thecodemaster.sap.analyzers;

import net.thecodemaster.sap.reporter.Reporter;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Luciano Sampaio
 */
public class CodeAnomaliesAnalyzer extends Analyzer {

  public CodeAnomaliesAnalyzer(boolean nameConvention) {
    verifiers = Creator.newCollection();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean run(IResource resource, Reporter reporter) throws CoreException {
    return false;
  }

}
