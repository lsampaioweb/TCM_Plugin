package net.thecodemaster.sap.visitors;

import java.util.Stack;

import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.graph.Method;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Luciano Sampaio
 */
public class CompilationUnitVisitor extends ASTVisitor {

  private Stack<Method>   methodStack;
  private String          file;
  private CompilationUnit cUnit;
  private CallGraph       callGraph;
  private BindingResolver bindingResolver;

  public CompilationUnitVisitor(String file, CompilationUnit cUnit, CallGraph callGraph, BindingResolver bindingResolver) {
    methodStack = new Stack<Method>();
    this.file = file;
    this.cUnit = cUnit;
    this.callGraph = callGraph;
    this.bindingResolver = bindingResolver;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    Method method = createMethodFromBinding(node, bindingResolver.resolveBinding(node));

    if (null != method) {
      callGraph.addMethod(method);

      // Push the current method into the stack.
      methodStack.push(method);
    }

    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
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

  private Method createMethodFromBinding(MethodDeclaration node, IMethodBinding methodBinding) {
    if (null == methodBinding) {
      return null;
    }

    Method method = new Method();
    method.setName(bindingResolver.getName(methodBinding));

    if (null != node) {
      // Handle working directory.
      method.setFile(file);
      method.setStart(cUnit.getLineNumber(node.getStartPosition()));
      method.setEnd(cUnit.getLineNumber(node.getStartPosition() + node.getLength()));
    }

    // Parameters.
    ITypeBinding[] parameters = bindingResolver.getParameterTypes(methodBinding);
    if (parameters.length > 0) {
      for (int i = 0; i < parameters.length; i++) {
        if (null != parameters[i])
          method.addParameter(bindingResolver.getQualifiedName(parameters[i]));
      }
    }

    // Class and Package.
    ITypeBinding clazz = bindingResolver.getDeclaringClass(methodBinding);
    if (null != clazz) {
      method.setClazz(bindingResolver.getName(clazz));

      IPackageBinding pkg = bindingResolver.getPackage(clazz);
      if (null != pkg)
        method.setPkg(bindingResolver.getName(pkg));
    }

    return method;
  }
}
