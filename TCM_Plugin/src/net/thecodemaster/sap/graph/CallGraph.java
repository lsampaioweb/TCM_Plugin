package net.thecodemaster.sap.graph;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * @author Luciano Sampaio
 */
public class CallGraph {

  /**
   * List with all the declared methods of the analyzed code.
   */
  private Map<MethodDeclaration, List<IMethodBinding>>   listMethods;
  /**
   * List with all the declared variables of the analyzed code.
   */
  private Map<MethodDeclaration, List<IVariableBinding>> listVariables;

  public CallGraph() {
    listMethods = Creator.newMap();
    listVariables = Creator.newMap();
  }

  public synchronized void addMethod(MethodDeclaration method) {
    if (!listMethods.containsKey(method)) {
      List<IMethodBinding> listInvocations = Creator.newList();
      listMethods.put(method, listInvocations);
    }
  }

  public synchronized void addInvokes(MethodDeclaration caller, IMethodBinding callee) {
    if (listMethods.containsKey(caller)) {
      List<IMethodBinding> listInvocations = listMethods.get(caller);
      listInvocations.add(callee);
    }
    else {
      List<IMethodBinding> listInvocations = Creator.newList();
      listInvocations.add(callee);
      listMethods.put(caller, listInvocations);
    }
  }
}
