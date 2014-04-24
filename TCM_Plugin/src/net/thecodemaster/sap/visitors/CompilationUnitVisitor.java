package net.thecodemaster.sap.visitors;

import java.util.Stack;

import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.CallGraph;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Luciano Sampaio
 */
public class CompilationUnitVisitor extends ASTVisitor {

  private Stack<MethodDeclaration> methodStack;
  private String                   file;
  private CompilationUnit          cUnit;
  private CallGraph                callGraph;

  public CompilationUnitVisitor(String file, CompilationUnit cUnit, CallGraph callGraph) {
    methodStack = new Stack<MethodDeclaration>();
    this.file = file;
    this.cUnit = cUnit;
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
    IMethodBinding method = BindingResolver.resolveMethodBinding(node);

    if (!methodStack.isEmpty()) {
      callGraph.addInvokes(methodStack.peek(), method);
    }

    return super.visit(node);
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    IMethodBinding method = BindingResolver.resolveConstructorBinding(node);

    if ((null != method) && (!methodStack.isEmpty())) {
      callGraph.addInvokes(methodStack.peek(), method);
    }

    return super.visit(node);
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
