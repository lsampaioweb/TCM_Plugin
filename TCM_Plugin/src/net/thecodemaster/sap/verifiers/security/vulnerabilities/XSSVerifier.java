package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class XSSVerifier extends Verifier {

  @Override
  public boolean visit(MethodDeclaration node) {
    System.out.println("SecurityVulnerabilityAnalyzer - MethodDeclaration " + node.getName());

    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("SecurityVulnerabilityAnalyzer - MethodDeclaration " + node.getName());

    return true;
  }

}
