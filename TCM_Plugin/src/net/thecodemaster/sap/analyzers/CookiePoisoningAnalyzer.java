package net.thecodemaster.sap.analyzers;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class CookiePoisoningAnalyzer extends Analyzer {

  @Override
  public boolean visit(MethodDeclaration node) {
    System.out.println("CookiePoisoningAnalyzer - " + node.getName());

    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("CookiePoisoningAnalyzer - " + node.getName());

    return true;
  }

}
