package net.thecodemaster.sap.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.sap.utils.Creator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
 * At any given time, we should only have on call graph of the code.
 * 
 * @author Luciano Sampaio
 */
public class CallGraph {

  /**
   * The file means the current branch that is being analyzed.
   */
  private String                                                currentFile;

  /**
   * List with all the declared methods of the analyzed code.
   */
  private Map<String, Map<MethodDeclaration, List<Expression>>> methodsPerFile;

  /**
   * List with all the declared variables of the analyzed code.
   */
  private Map<IVariableBinding, VariableBindingManager>         listVariables;

  public CallGraph() {
    methodsPerFile = Creator.newMap();
    listVariables = Creator.newMap();
  }

  private String getResourceName(IResource resource) {
    return resource.getProjectRelativePath().toOSString();
  }

  public void addFile(IResource resource) {
    this.currentFile = getResourceName(resource);
  }

  public boolean containsFile(IResource resource) {
    return methodsPerFile.containsKey(getResourceName(resource));
  }

  public boolean removeFile(IResource resource) {
    return (null != methodsPerFile.remove(getResourceName(resource)));
  }

  public void addMethod(MethodDeclaration method) {
    // 01 - Check if the current file is already in the list.
    if (!methodsPerFile.containsKey(currentFile)) {
      Map<MethodDeclaration, List<Expression>> methods = Creator.newMap();

      methodsPerFile.put(currentFile, methods);
    }

    // 02 - Get the list of methods in the current file.
    Map<MethodDeclaration, List<Expression>> methods = getMethods(currentFile);

    if (!methods.containsKey(method)) {
      List<Expression> invocations = Creator.newList();

      // Create a empty list of method invocations.
      methods.put(method, invocations);
    }
  }

  public void addInvokes(MethodDeclaration caller, Expression callee) {
    // 01 - Get the list of methods in the current file.
    Map<MethodDeclaration, List<Expression>> methods = getMethods(currentFile);

    if (null == methods) {
      return;
    }

    // 02 - If the methods is not in the list, add it.
    if (!methods.containsKey(caller)) {
      addMethod(caller);
    }

    // 03 - Add the method invocation for the current method (caller).
    List<Expression> invocations = methods.get(caller);
    invocations.add(callee);
  }

  public Map<MethodDeclaration, List<Expression>> getMethods(IResource resource) {
    return getMethods(getResourceName(resource));
  }

  private Map<MethodDeclaration, List<Expression>> getMethods(String file) {
    return methodsPerFile.get(file);
  }

  public Map<IVariableBinding, VariableBindingManager> getlistVariables() {
    return listVariables;
  }

  public MethodDeclaration getMethod(IResource resource, Expression expr) {
    // 01 - Get all the methods from this resource.
    // 02 - From that list, try to find this method (expr).
    MethodDeclaration method = getMethod(getMethods(resource), expr);

    // 03 - If method is different from null, it means we found it.
    if (null != method) {
      return method;
    }

    // 04 - If it reaches this point, it means that this method was not implemented into this resource.
    // We now have to try to find its implementation in other resources of this project.
    for (Entry<String, Map<MethodDeclaration, List<Expression>>> entry : methodsPerFile.entrySet()) {
      method = getMethod(entry.getValue(), expr);

      // 05 - If method is different from null, it means we found it.
      if (null != method) {
        return method;
      }
    }

    // We did not find this method into our list of methods. (We do not have this method's implementation)
    return null;
  }

  private MethodDeclaration getMethod(Map<MethodDeclaration, List<Expression>> mapMethods, Expression expr) {
    // 01 - Iterate through the list to verify if we have the implementation of this method in our list.
    for (MethodDeclaration methodDeclaration : mapMethods.keySet()) {
      // 02 - Verify if these methods are the same.
      if (areMethodsEqual(methodDeclaration, expr)) {
        return methodDeclaration;
      }
    }

    return null;
  }

  private boolean areMethodsEqual(MethodDeclaration method, Expression other) {
    String methodName = BindingResolver.getName(method);
    String otherName = BindingResolver.getName(other);

    // 02 - Verify if they have the same name.
    if (methodName.equals(otherName)) {

      // 03 - Get the qualified name (Package + Class) of these methods.
      String qualifiedName = BindingResolver.getQualifiedName(method);
      String otherQualifiedName = BindingResolver.getQualifiedName(other);

      // 04 - Verify if they are from the same package and class.
      // Method names can repeat in other classes.
      if (qualifiedName.equals(otherQualifiedName)) {

        // 05 - Get their parameters.
        List<ITypeBinding> methodParameters = BindingResolver.getParameterTypes(method);
        List<Expression> otherParameters = BindingResolver.getParameterTypes(other);

        // 06 - It is necessary to check the number of parameters and its types
        // because it may exist methods with the same names but different parameters.
        if (methodParameters.size() == otherParameters.size()) {
          int index = 0;
          for (ITypeBinding currentParameter : methodParameters) {
            ITypeBinding otherTypeBinding = otherParameters.get(index++).resolveTypeBinding();

            // 07 - Verify if all the parameters are the ones expected.
            if ((otherTypeBinding == null) || (!currentParameter.getQualifiedName().equals(otherTypeBinding.getQualifiedName()))) {
              return false;
            }
          }

          return true;
        }
      }
    }

    return false;
  }
}
