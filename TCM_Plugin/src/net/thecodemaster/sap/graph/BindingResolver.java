package net.thecodemaster.sap.graph;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class BindingResolver {

  private BindingResolver() {
  }

  public static ITypeBinding getDeclaringClass(IMethodBinding methodBinding) {
    return methodBinding.getDeclaringClass();
  }

  public static String getName(IMethodBinding methodBinding) {
    return methodBinding.getName();
  }

  public static String getName(IPackageBinding pkg) {
    return pkg.getName();
  }

  public static String getName(ITypeBinding clazz) {
    return clazz.getName();
  }

  public static IPackageBinding getPackage(ITypeBinding clazz) {
    return clazz.getPackage();
  }

  public static ITypeBinding[] getParameterTypes(IMethodBinding methodBinding) {
    return methodBinding.getParameterTypes();
  }

  public static String getQualifiedName(ITypeBinding iTypeBinding) {
    return iTypeBinding.getQualifiedName();
  }

  public static IMethodBinding resolveBinding(MethodDeclaration node) {
    return (null != node) ? node.resolveBinding() : null;
  }

  public static IMethodBinding resolveMethodBinding(MethodInvocation node) {
    return (null != node) ? node.resolveMethodBinding() : null;
  }

  public static IMethodBinding resolveConstructorBinding(ClassInstanceCreation node) {
    return (null != node) ? node.resolveConstructorBinding() : null;
  }

}
