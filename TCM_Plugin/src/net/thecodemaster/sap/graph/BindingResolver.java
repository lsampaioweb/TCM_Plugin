package net.thecodemaster.sap.graph;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * @author Luciano Sampaio
 */
public class BindingResolver {

  public ITypeBinding getDeclaringClass(IMethodBinding methodBinding) {
    return methodBinding.getDeclaringClass();
  }

  public String getName(IMethodBinding methodBinding) {
    return methodBinding.getName();
  }

  public String getName(IPackageBinding pkg) {
    return pkg.getName();
  }

  public String getName(ITypeBinding clazz) {
    return clazz.getName();
  }

  public IPackageBinding getPackage(ITypeBinding clazz) {
    return clazz.getPackage();
  }

  public ITypeBinding[] getParameterTypes(IMethodBinding methodBinding) {
    return methodBinding.getParameterTypes();
  }

  public String getQualifiedName(ITypeBinding iTypeBinding) {
    return iTypeBinding.getQualifiedName();
  }

  public IMethodBinding resolveBinding(MethodDeclaration node) {
    return node.resolveBinding();
  }

}
