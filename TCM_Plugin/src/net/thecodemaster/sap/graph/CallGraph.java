package net.thecodemaster.sap.graph;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.IMethodBinding;
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
  private String                                                    currentFile;

  /**
   * List with all the declared methods of the analyzed code.
   */
  private Map<String, Map<MethodDeclaration, List<IMethodBinding>>> methodsPerFile;

  public CallGraph() {
    methodsPerFile = Creator.newMap();
  }

  public void addFile(String file) {
    this.currentFile = file;
  }

  public boolean containsFile(String file) {
    return methodsPerFile.containsKey(file);
  }

  public boolean removeFile(String file) {
    return (null != methodsPerFile.remove(file));
  }

  public void addMethod(MethodDeclaration method) {
    // 01 - Check if the current file is already in the list.
    if (!methodsPerFile.containsKey(currentFile)) {
      Map<MethodDeclaration, List<IMethodBinding>> methods = Creator.newMap();

      methodsPerFile.put(currentFile, methods);
    }

    // 02 - Get the list of methods in the current file.
    Map<MethodDeclaration, List<IMethodBinding>> methods = getMethods(currentFile);

    if (!methods.containsKey(method)) {
      List<IMethodBinding> invocations = Creator.newList();

      // Create a empty list of method invocations.
      methods.put(method, invocations);
    }
  }

  public void addInvokes(MethodDeclaration caller, IMethodBinding callee) {
    // 01 - Get the list of methods in the current file.
    Map<MethodDeclaration, List<IMethodBinding>> methods = getMethods(currentFile);

    if (null == methods) {
      return;
    }

    // 02 - If the methods is not in the list, add it.
    if (!methods.containsKey(caller)) {
      addMethod(caller);
    }

    // 03 - Add the method invocation for the current method (caller).
    List<IMethodBinding> invocations = methods.get(caller);
    invocations.add(callee);
  }

  public Map<MethodDeclaration, List<IMethodBinding>> getMethods(String file) {
    return methodsPerFile.get(file);
  }

}
