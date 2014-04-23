package net.thecodemaster.sap.verifiers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.arguments.Argument;
import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Convert;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.utils.Timer;
import net.thecodemaster.sap.verifiers.helpers.VariableBindingManager;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier extends ASTVisitor {

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

    if (null != cu) {
      Timer timer = (new Timer(verifierName)).start();
      cu.accept(this);
      System.out.println(timer.stop().toString());
    }

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

  @Override
  public boolean visit(MethodDeclaration node) {
    System.out.println("MethodDeclaration - " + node);

    getListMethodDeclarations().put(node.resolveBinding(), node);

    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("MethodInvocation - " + node);

    getListMethodInvocations().put(node.resolveMethodBinding(), node);

    return super.visit(node);
  }

  /**
   * Looks for local variable declarations. For every occurrence of a local
   * variable, a {@link VariableBindingManager} is created and stored in {@link #listLocalVariables} map.
   * 
   * @param node
   *          the node to visit
   * @return static {@code false} to prevent that the simple name in the
   *         declaration is understood by {@link #visit(SimpleName)} as
   *         reference
   */
  @Override
  public boolean visit(VariableDeclarationStatement node) {
    System.out.println("VariableDeclarationStatement - " + node);
    for (Iterator<?> iter = node.fragments().iterator(); iter.hasNext();) {
      VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

      // VariableDeclarationFragment: is the plain variable declaration part.
      // Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
      IVariableBinding binding = fragment.resolveBinding();

      // Creates the manager of the fragment.
      VariableBindingManager manager = new VariableBindingManager(fragment);

      getLocalVariables().put(binding, manager);

      // The first assignment is the initializer.
      manager.variableInitialized(fragment.getInitializer());
    }

    return false; // Prevents that SimpleName is interpreted as reference.
  }

  /**
   * Visits {@link Assignment} AST nodes (e.g. {@code x = 7 + 8} ).
   * Resolves the binding of the left hand side (in the example: {@code x}).
   * If the binding is found in the {@link #listLocalVariables} map, we have an
   * assignment of a local variable.
   * The variable binding manager of this local variable then has to be informed about this assignment.
   * 
   * @param node
   *          the node to visit
   */
  @Override
  public boolean visit(Assignment node) {
    System.out.println("Assignment - " + node);
    if (node.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
      IBinding binding = ((SimpleName) node.getLeftHandSide()).resolveBinding();
      if (getLocalVariables().containsKey(binding)) {
        // It contains the key -> it is an assignment of a local variable.

        VariableBindingManager manager = getLocalVariables().get(binding);

        manager.variableInitialized(node.getRightHandSide());
      }
    }

    return false; // Prevents that SimpleName is interpreted as reference.
  }

  /**
   * Visits {@link SimpleName} AST nodes. Resolves the binding of the simple
   * name and looks for it in the {@link #listLocalVariables} map.
   * If the binding is found, this is a reference to a local variable.
   * The variable binding manager of this local variable then has to be informed about that reference.
   * 
   * @param node
   *          the node to visit
   */
  @Override
  public boolean visit(SimpleName node) {
    IBinding binding = node.resolveBinding();
    if (getLocalVariables().containsKey(binding)) {
      System.out.println("SimpleName - " + node);
      VariableBindingManager manager = getLocalVariables().get(binding);
      manager.variableRefereneced(node);
    }

    return true;
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
