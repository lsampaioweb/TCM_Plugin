package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
  private static List<ExitPoint> exitPoints;
  /**
   * The current resource that is being analyzed.
   */
  private IResource              currentResource;

  /**
   * @param name The name of the verifier.
   */
  public Verifier(String name) {
    this.verifierName = name;
  }

  public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
    this.callGraph = callGraph;
    this.reporter = reporter;

    setSubTask(verifierName);

    // Perform the verifications on the resources.
    run(resources);
  }

  protected void run(List<IResource> resources) {
    // 01 - Run the vulnerability detection on all the passed resources.
    for (IResource resource : resources) {
      if (callGraph.containsFile(resource)) {
        // 02 - Delete any old markers of this resource.
        deleteMarkers(resource);

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

  protected boolean deleteMarkers(IResource resource) {
    try {
      resource.deleteMarkers(Constants.MARKER_ID, false, IResource.DEPTH_INFINITE);
      return true;
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
      return false;
    }
  }

  protected boolean addMarker(IResource resource, String message, Expression expr) {
    try {
      IMarker marker = resource.createMarker(Constants.MARKER_ID);
      marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
      marker.setAttribute(IMarker.MESSAGE, message);

      // Get the Compilation Unit of this resource.
      CompilationUnit cUnit = BindingResolver.findParentCompilationUnit(expr);

      int startPosition = expr.getStartPosition();
      int endPosition = startPosition + expr.getLength();
      int lineNumber = cUnit.getLineNumber(startPosition);

      marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
      marker.setAttribute(IMarker.CHAR_START, startPosition);
      marker.setAttribute(IMarker.CHAR_END, endPosition);
      return true;
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
      return false;
    }
  }

  protected abstract void run(Expression method, ExitPoint exitPoint);

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
   * @param method
   * @return An ExitPoint object if this node belongs to the list, otherwise null.
   */
  protected ExitPoint isMethodAnExitPoint(Expression method) {
    // 01 - Get the method name.
    String methodName = BindingResolver.getName(method);

    for (ExitPoint currentExitPoint : getExitPoints()) {
      // 02 - Verify if this method is in the list of ExitPoints.
      if (currentExitPoint.getMethodName().equals(methodName)) {

        // 03 - Get the qualified name (Package + Class) of this method.
        String qualifiedName = BindingResolver.getQualifiedName(method);

        // 04 - Verify if this is the method really the method we were looking for.
        // Method names can repeat in other classes.
        if (currentExitPoint.getQualifiedName().equals(qualifiedName)) {

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
    }

    return null;
  }

  protected boolean matchRules(List<Integer> rules, Expression parameter) {
    for (Integer astNodeValue : rules) {
      if (parameter.getNodeType() == astNodeValue) {
        return true;
      }
    }

    return false;
  }

  protected static List<ExitPoint> getExitPoints() {
    if (null == exitPoints) {
      exitPoints = Creator.newList();
    }

    return exitPoints;
  }

  private void setCurrentResource(IResource currentResource) {
    this.currentResource = currentResource;
  }

  protected IResource getCurrentResource() {
    return currentResource;
  }

}
