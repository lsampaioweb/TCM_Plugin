package net.thecodemaster.sap.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class MethodInvocationVisitor extends ASTVisitor {

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println(node.getName());

    return true;
  }
}
