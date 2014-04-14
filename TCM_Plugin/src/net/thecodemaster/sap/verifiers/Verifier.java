package net.thecodemaster.sap.verifiers;

import net.thecodemaster.sap.reporter.Reporter;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier extends ASTVisitor {

  protected Reporter reporter;
  protected String   name;

  /**
   * @param name The name of the verifier.
   */
  public Verifier(String name) {
    this.name = name;
  }

  public void run(CompilationUnit cu, Reporter reporter) {
    this.reporter = reporter;

    if (null != reporter) {
      reporter.getProgressMonitor().subTask(name);
    }

    if (null != cu) {
      cu.accept(this);
    }
  }

}
