package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier {

  /**
   * The name of the current verifier.
   */
  private String                 verifierName;

  /**
   * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
   */
  private CallGraph              callGraph;
  /**
   * The report object
   */
  private Reporter               reporter;

  /**
   * List with all the ExitPoints of this verifier.
   */
  private static List<ExitPoint> listExitPoints;

  /**
   * @param name The name of the verifier.
   */
  public Verifier(String name) {
    this.verifierName = name;
  }

  public void run(List<String> resources, CallGraph callGraph, Reporter reporter) {
    this.callGraph = callGraph;
    this.reporter = reporter;

    setSubTask(verifierName);

    // Perform the verifications on the resources.
    run(resources);
  }

  protected void run(List<String> resources) {
    // 01 - Run the vulnerability detection on all the passed resources.
    for (String file : resources) {
      if (callGraph.containsFile(file)) {
        // 02 - Get the list of methods in the current file.
        Map<MethodDeclaration, List<IMethodBinding>> methods = callGraph.getMethods(file);

        // 03 - Get all the method invocations of each method declaration.
        for (List<IMethodBinding> invocations : methods.values()) {

          // 04 - Iterate over all method invocations to verify if it is a ExitPoint.
          for (IMethodBinding method : invocations) {
            ExitPoint exitPoint = isMethodAnExitPoint(method);

            if (null != exitPoint) {
              // 05 - This is an ExitPoint method and it needs to be verified.
              run(method, exitPoint);
            }
          }

        }
      }
    }
  }

  protected abstract void run(IMethodBinding method, ExitPoint exitPoint);

  /**
   * Notifies that a subtask of the main task is beginning.
   * 
   * @param taskName The text that will be displayed to the user.
   */
  protected void setSubTask(String taskName) {
    if ((null != reporter) && (null != reporter.getProgressMonitor())) {
      reporter.getProgressMonitor().subTask(taskName);
    }
  }

  /**
   * @param node
   * @return An ExitPoint object if this node belongs to the list, otherwise null.
   */
  protected ExitPoint isMethodAnExitPoint(IMethodBinding node) {
    // 01 - Get the method name.
    String methodName = BindingResolver.getName(node);

    for (ExitPoint currentExitPoint : getListExitPoints()) {
      // 02 - Verify if this method is in the list of ExitPoints.
      if (currentExitPoint.getMethodName().equals(methodName)) {

        // 03 - Get the qualified name (Package + Class) of this method.
        String qualifiedName = BindingResolver.getQualifiedName(node);

        // 04 - Verify if this is the method really the method we were looking for.
        // Method names can repeat in other classes.
        if (currentExitPoint.getQualifiedName().equals(qualifiedName)) {

          // 05 - Get the expected arguments of this method.
          Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

          // 06 - Get the received arguments of the current method.
          List<ITypeBinding> receivedParameters = BindingResolver.getParameterTypes(node);

          // 07 - It is necessary to check the number of arguments and its types
          // because it may exist methods with the same names but different parameters.
          if (expectedParameters.size() == receivedParameters.size()) {
            boolean isMethodAnExitPoint = true;
            int index = 0;
            for (Parameter expectedParameter : expectedParameters.keySet()) {
              ITypeBinding receivedParameter = receivedParameters.get(index++);

              // Verify if all the parameters are the ones expected.
              if (!expectedParameter.getType().equals(receivedParameter.getQualifiedName())) {
                isMethodAnExitPoint = false;
                break;
              }
            }

            if (isMethodAnExitPoint) {
              return currentExitPoint;
            }
          }
        }
      }
    }

    return null;
  }

  protected static List<ExitPoint> getListExitPoints() {
    if (null == listExitPoints) {
      listExitPoints = Creator.newList();
    }

    return listExitPoints;
  }

}
