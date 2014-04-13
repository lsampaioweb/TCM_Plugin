package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  @Override
  public boolean visit(MethodDeclaration node) {
    reporter.getProgressMonitor().setTaskName("SecurityMisconfigurationVerifier - " + node.getName());
    System.out.println("SecurityMisconfigurationVerifier - MethodDeclaration " + node.getName());

    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("SecurityMisconfigurationVerifier - MethodInvocation " + node.getName());

    return true;
  }
}
