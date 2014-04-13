package net.thecodemaster.sap.verifiers.code.anomalies;

import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class NameConventionVerifier extends Verifier {

  @Override
  public boolean visit(MethodDeclaration node) {
    System.out.println("NameConventionVerifier - MethodDeclaration " + node.getName());

    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("NameConventionVerifier - MethodInvocation " + node.getName());

    return true;
  }

}