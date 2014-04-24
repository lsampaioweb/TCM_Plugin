package net.thecodemaster.sap.visitors;

import java.util.Stack;

import net.thecodemaster.sap.graph.CallGraph;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Luciano Sampaio
 */
public class CompilationUnitVisitor extends ASTVisitor {

  private Stack<MethodDeclaration> methodStack;
  private CallGraph                callGraph;

  public CompilationUnitVisitor(CallGraph callGraph) {
    methodStack = new Stack<MethodDeclaration>();

    this.callGraph = callGraph;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    callGraph.addMethod(node);

    // Push the current method into the stack.
    methodStack.push(node);

    return super.visit(node);
  }

  /**
   * Remove the top element from the stack.
   */
  @Override
  public void endVisit(MethodDeclaration node) {
    if (!methodStack.isEmpty()) {
      methodStack.pop();
    }
  }

  @Override
  public boolean visit(MethodInvocation node) {
    addInvokes(node);

    return super.visit(node);
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    addInvokes(node);

    return super.visit(node);
  }

  private void addInvokes(Expression method) {
    if ((null != method) && (!methodStack.isEmpty())) {
      callGraph.addInvokes(methodStack.peek(), method);
    }
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    // Prevents that SimpleName is interpreted as reference.
    return false;
  }

  @Override
  public boolean visit(Assignment node) {
    // Prevents that SimpleName is interpreted as reference.
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    return true;
  }

}
