package net.thecodemaster.sap.reporter;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Luciano Sampaio
 */
public class Reporter {

  private List<Problem>    problems;
  private IProgressMonitor progressMonitor;

  /**
   * @param problemView
   * @param textFile
   * @param xmlFile
   */
  public Reporter(boolean problemView, boolean textFile, boolean xmlFile) {
  }

  /**
   * @param progressMonitor
   */
  public void setProgressMonitor(IProgressMonitor progressMonitor) {
    this.progressMonitor = progressMonitor;
  }

}
