package net.thecodemaster.sap.verifiers;

import net.thecodemaster.sap.reporter.Reporter;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier extends ASTVisitor {

  protected Reporter reporter;

  public void run(CompilationUnit cu, Reporter reporter) {
    this.reporter = reporter;

    cu.accept(this);
  }

}
