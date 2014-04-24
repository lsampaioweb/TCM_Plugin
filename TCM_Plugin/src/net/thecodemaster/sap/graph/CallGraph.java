package net.thecodemaster.sap.graph;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class CallGraph {

  private Map<MethodDeclaration, List<MethodInvocation>> methods;

  public CallGraph() {
    methods = Creator.newMap();
  }

  public void addMethod(Method method) {
    // if (invocationsForMethods.get(activeMethod) == null) {
    // invocationsForMethods.put(activeMethod, new ArrayList<MethodInvocation>());
    // }
    // invocationsForMethods.get(activeMethod).add(node);
    //
    // if (methods.containsKey(method)) {
    // // Update
    // if (method.getStart() != -1 && method.getEnd() != -1) {
    // Method value = methods.get(method);
    // value.setFile(method.getFile());
    // value.setStart(method.getStart());
    // value.setEnd(method.getEnd());
    // }
    // }
    // else {
    // // Insert
    // methods.put(method, method);
    // }
  }
}
