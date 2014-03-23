package net.thecodemaster.sap.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * @author Luciano Sampaio
 */
public class MethodDeclarationVisitor extends ASTVisitor {

  @Override
  public boolean visit(MethodDeclaration node) {
    System.out.println(node.getName());

    node.accept(new MethodInvocationVisitor());

    return super.visit(node);
  }
}
