package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class SQLInjectionVerifier extends Verifier {

  public SQLInjectionVerifier() {
    super(Messages.Plugin.SQL_INJECTION_VERIFIER);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    System.out.println("SQLInjectionVerifier - MethodDeclaration " + node.getName());

    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("SQLInjectionVerifier - MethodInvocation " + node.getName());

    return true;
  }
}
