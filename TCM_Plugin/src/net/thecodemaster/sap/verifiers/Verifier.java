package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.points.AbstractPoint;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.points.ExitPoint;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.xmlloaders.ExitPointLoader;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier {

  /**
   * The name of the current verifier.
   */
  private String                  verifierName;
  /**
   * The id of the current verifier.
   */
  private int                     verifierId;
  /**
   * The current resource that is being analyzed.
   */
  private IResource               currentResource;
  /**
   * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
   */
  private CallGraph               callGraph;
  /**
   * The report object
   */
  private Reporter                reporter;
  /**
   * List with all the ExitPoints of this verifier (shared among other instances of this verifier).
   */
  private static List<ExitPoint>  exitPoints;
  /**
   * List with all the EntryPoints (shared among other instances of the verifiers).
   */
  private static List<EntryPoint> entryPoints;

  /**
   * @param name The name of the verifier.
   * @param id The id of the verifier.
   * @param listEntryPoints List with all the EntryPoints methods.
   */
  public Verifier(String name, int id, List<EntryPoint> listEntryPoints) {
    this.verifierName = name;
    this.verifierId = id;
    entryPoints = listEntryPoints;
  }

  public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
    this.callGraph = callGraph;
    this.reporter = reporter;

    setSubTask(verifierName);

    // Perform the verifications on the resources.
    run(resources);
  }

  protected void run(List<IResource> resources) {
    // 01 - Run the vulnerability detection on all the provided resources.
    for (IResource resource : resources) {
      if (callGraph.containsFile(resource)) {
        // 02 - Delete any old markers of this resource.
        getReporter().clearProblems(resource);

        // 03 - Get the list of methods in the current resource.
        Map<MethodDeclaration, List<Expression>> methods = callGraph.getMethods(resource);

        // 04 - Get all the method invocations of each method declaration.
        for (List<Expression> invocations : methods.values()) {

          // 05 - Iterate over all method invocations to verify if it is a ExitPoint.
          for (Expression method : invocations) {
            ExitPoint exitPoint = isMethodAnExitPoint(method);

            if (null != exitPoint) {
              // 06 - Some methods will need to have access to the resource that is currently being analyzed.
              // but we do not want to pass it to all these methods as a parameter.
              setCurrentResource(resource);

              // 07 - This is an ExitPoint method and it needs to be verified.
              run(method, exitPoint);
            }
          }

        }
      }
    }
  }

  /**
   * @param method
   * @return An ExitPoint object if this node belongs to the list, otherwise null.
   */
  protected ExitPoint isMethodAnExitPoint(Expression method) {
    for (ExitPoint currentExitPoint : getExitPoints()) {
      if (methodsHaveSameNameAndPackage(currentExitPoint, method)) {
        // 05 - Get the expected arguments of this method.
        Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

        // 06 - Get the received parameters of the current method.
        List<Expression> receivedParameters = BindingResolver.getParameterTypes(method);

        // 07 - It is necessary to check the number of parameters and its types
        // because it may exist methods with the same names but different parameters.
        if (expectedParameters.size() == receivedParameters.size()) {
          boolean isMethodAnExitPoint = true;
          int index = 0;
          for (Parameter expectedParameter : expectedParameters.keySet()) {
            ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

            // Verify if all the parameters are the ones expected.
            if ((typeBinding == null) || (!expectedParameter.getQualifiedName().equals(typeBinding.getQualifiedName()))) {
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

    return null;
  }

  protected boolean isMethodAnEntryPoint(Expression method) {
    for (EntryPoint currentEntryPoint : getEntryPoints()) {
      if (methodsHaveSameNameAndPackage(currentEntryPoint, method)) {
        // 05 - Get the expected arguments of this method.
        List<String> expectedParameters = currentEntryPoint.getParameters();

        // 06 - Get the received parameters of the current method.
        List<Expression> receivedParameters = BindingResolver.getParameterTypes(method);

        // 07 - It is necessary to check the number of parameters and its types
        // because it may exist methods with the same names but different parameters.
        if (expectedParameters.size() == receivedParameters.size()) {
          int index = 0;
          for (String expectedParameter : expectedParameters) {
            ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

            // Verify if all the parameters are the ones expected.
            if ((typeBinding == null) || (!expectedParameter.equals(typeBinding.getQualifiedName()))) {
              return false;
            }
          }

          return true;
        }
      }
    }

    return false;
  }

  private boolean methodsHaveSameNameAndPackage(AbstractPoint abstractPoint, Expression method) {
    // 01 - Get the method name.
    String methodName = BindingResolver.getName(method);

    // 02 - Verify if this method is in the list of ExitPoints.
    if (abstractPoint.getMethodName().equals(methodName)) {

      // 03 - Get the qualified name (Package + Class) of this method.
      String qualifiedName = BindingResolver.getQualifiedName(method);

      // 04 - Verify if this is really the method we were looking for.
      // Method names can repeat in other classes.
      if (null != qualifiedName) {
        return qualifiedName.matches(abstractPoint.getQualifiedName());
      }
    }

    return false;
  }

  protected boolean isMethodASanitizationPoint(Expression method) {
    return false;
  }

  protected String getVerifierName() {
    return verifierName;
  }

  private int getVerifierId() {
    return verifierId;
  }

  private void setCurrentResource(IResource currentResource) {
    this.currentResource = currentResource;
  }

  protected IResource getCurrentResource() {
    return currentResource;
  }

  protected CallGraph getCallGraph() {
    return callGraph;
  }

  protected Reporter getReporter() {
    return reporter;
  }

  protected static List<ExitPoint> getExitPoints() {
    return exitPoints;
  }

  protected static void loadExitPoints(int verifierId) {
    exitPoints = (new ExitPointLoader(verifierId)).load();
  }

  protected static List<EntryPoint> getEntryPoints() {
    return entryPoints;
  }

  protected abstract void run(Expression method, ExitPoint exitPoint);

  /**
   * Notifies that a subtask of the main task is beginning.
   * 
   * @param taskName The text that will be displayed to the user.
   */
  protected void setSubTask(String taskName) {
    if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
      getReporter().getProgressMonitor().subTask(taskName);
    }
  }

  protected boolean matchRules(List<Integer> rules, Expression parameter) {
    if (null == parameter) {
      // There is nothing we can do to verify it.
      return true;
    }

    for (Integer astNodeValue : rules) {
      if (astNodeValue == parameter.getNodeType()) {
        return true;
      }
    }

    return false;
  }

  protected void foundVulnerability(Expression expr, String message) {
    getReporter().addProblem(getVerifierId(), getCurrentResource(), expr, message);
  }

}
