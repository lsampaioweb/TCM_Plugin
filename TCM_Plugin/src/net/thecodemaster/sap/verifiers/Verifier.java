package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.arguments.Argument;
import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Convert;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.verifiers.helpers.VariableBindingManager;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier {

  /**
   * The name of the current verifier.
   */
  private String                                        verifierName;

  /**
   * The report object
   */
  private Reporter                                      reporter;
  /**
   * List with all the ExitPoints of this verifier.
   */
  private static List<ExitPoint>                        listExitPoints;
  /**
   * 
   */
  private Map<IMethodBinding, MethodDeclaration>        listMethodDeclarations;
  /**
   * 
   */
  private Map<IMethodBinding, MethodInvocation>         listMethodInvocations;
  /**
   * List with all the declared variables of the analyzed code.
   */
  private Map<IVariableBinding, VariableBindingManager> listLocalVariables;

  /**
   * @param name The name of the verifier.
   */
  public Verifier(String name) {
    this.verifierName = name;
  }

  private void initializeVolatileLists() {
    listMethodDeclarations = Creator.newMap();
    listMethodInvocations = Creator.newMap();
    listLocalVariables = Creator.newMap();
  }

  /**
   * @param cu
   * @param reporter
   */
  public void run(CompilationUnit cu, Reporter reporter) {
    this.reporter = reporter;
    initializeVolatileLists();

    setSubTask(verifierName);

    // After the whole file was processed, invoke the run method of each verifier
    // to start the detection.
    run();
  }

  protected abstract void run();

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
  protected ExitPoint isMethodAnExitPoint(MethodInvocation node) {
    // The name of the current method.
    String methodName = node.getName().getIdentifier();

    for (ExitPoint currentExitPoint : getListExitPoints()) {
      if (currentExitPoint.getMethodName().equals(methodName)) {

        Expression expression = node.getExpression();
        if (null != expression) {

          // The fully qualified name.
          String qualifiedName = expression.resolveTypeBinding().getQualifiedName();

          // It is necessary to check against the qualified name
          // because same method names can appear in more than one class.
          if (currentExitPoint.getPackageName().equals(qualifiedName)) {

            Map<Argument, List<Integer>> expectedArguments = currentExitPoint.getArguments();
            List<Expression> receivedArguments = Convert.fromListObjectToListExpression(node.arguments());

            // It is necessary to check the number of arguments and its types
            // because it may exist methods with the same names but different arguments.
            if (expectedArguments.size() == receivedArguments.size()) {
              boolean isMethodAnExitPoint = true;
              int index = 0;
              for (Argument expectedArgument : expectedArguments.keySet()) {
                ITypeBinding receivedArgument = receivedArguments.get(index++).resolveTypeBinding();

                // Verify if all the arguments are the ones expected.
                if (!expectedArgument.getType().equals(receivedArgument.getQualifiedName())) {
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
    }

    return null;
  }

  protected static List<ExitPoint> getListExitPoints() {
    if (null == listExitPoints) {
      listExitPoints = Creator.newList();
    }

    return listExitPoints;
  }

  protected Map<IMethodBinding, MethodDeclaration> getListMethodDeclarations() {
    return listMethodDeclarations;
  }

  protected Map<IMethodBinding, MethodInvocation> getListMethodInvocations() {
    return listMethodInvocations;
  }

  /**
   * Getter for the resulting map.
   * 
   * @return a map with variable bindings as keys and {@link VariableBindingManager} as values.
   */
  protected Map<IVariableBinding, VariableBindingManager> getLocalVariables() {
    return listLocalVariables;
  }

}
