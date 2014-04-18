package net.thecodemaster.sap.verifiers;

import java.util.List;

import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier extends ASTVisitor {

  /**
   * The name of the current verifier.
   */
  protected String          verifierName;

  /**
   * The report object
   */
  protected Reporter        reporter;
  /**
   * List with all the ExitPoints of this verifier.
   */
  protected List<ExitPoint> listExitPoints;

  /**
   * @param name The name of the verifier.
   */
  public Verifier(String name) {
    this.verifierName = name;

    // 01 - Initialize the list of ExitPoints.
    listExitPoints = Creator.newList();
  }

  public void run(CompilationUnit cu, Reporter reporter) {
    this.reporter = reporter;

    setSubTask(verifierName);

    if (null != cu) {
      cu.accept(this);
    }
  }

  protected void setSubTask(String taskName) {
    if ((null != reporter) && (null != reporter.getProgressMonitor())) {
      reporter.getProgressMonitor().subTask(taskName);
    }
  }

}
