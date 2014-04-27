package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.graph.VariableBindingManager;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.points.ExitPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  static {
    loadExitPoints(Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID);
    // These ExitPoints are common to all instances of this verifier.
    // 01 - Create each ExitPoint.
    // ExitPoint exitPointGetConnection = new ExitPoint("java.sql.DriverManager", "getConnection");
    // Map<Parameter, List<Integer>> paramsGetConnection = Creator.newMap();
    // List<Integer> emptyList = Creator.newList();
    // paramsGetConnection.put(new Parameter("java.lang.String"), null); // Anything is valid.
    // paramsGetConnection.put(new Parameter("java.lang.String"), emptyList); // Only sanitized values are valid.
    // paramsGetConnection.put(new Parameter("java.lang.String"), emptyList); // Only sanitized values are valid.
    // exitPointGetConnection.setParameters(paramsGetConnection);
    //
    // // 02 - Add the ExitPoint to the list.
    // getExitPoints().add(exitPointGetConnection);
  }

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_NAME, Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void run(Expression method, ExitPoint exitPoint) {
    // 01 - Get the expected parameters of the ExitPoint method.
    Map<Parameter, List<Integer>> expectedParameters = exitPoint.getParameters();

    // 02 - Get the parameters (received) from the current method.
    List<Expression> receivedParameters = BindingResolver.getParameterTypes(method);

    int index = 0;
    int depth = 0;
    for (List<Integer> rules : expectedParameters.values()) {
      // If the rules are null, it means the expected parameter can be anything. (We do not care for it).
      if (null != rules) {
        checkParameters(rules, receivedParameters.get(index), depth);
      }
      index++;
    }
  }

  private boolean checkParameters(List<Integer> rules, Expression parameter, int depth) {
    // 01 - If the parameter matches the rules (Easy case), the parameter is okay.
    if (!matchRules(rules, parameter)) {

      // To avoid infinitive loop, this check is necessary.
      if (Constants.MAXIMUM_DEPTH == depth) {
        return true;
      }

      // 02 - We need to check the type of the parameter and deal with it accordingly to its type.
      switch (parameter.getNodeType()) {
        case ASTNode.STRING_LITERAL:
        case ASTNode.NUMBER_LITERAL:
          checkLiteral(parameter);
          break;
        case ASTNode.INFIX_EXPRESSION:
          return checkInfixExpression(rules, parameter, ++depth);
        case ASTNode.SIMPLE_NAME:
          return checkSimpleName(rules, parameter, ++depth);
        case ASTNode.METHOD_INVOCATION:
          return checkMethodInvocation(rules, parameter, ++depth);
        case ASTNode.METHOD_DECLARATION:
          return checkMethodDeclaration(rules, parameter, ++depth);
        default:
          PluginLogger.logError("Default Node Type: " + parameter.getNodeType() + " - " + parameter, null);
          return false;
      }
    }

    return true;
  }

  private boolean checkLiteral(Expression expr) {
    String message = null;
    switch (expr.getNodeType()) {
      case ASTNode.STRING_LITERAL:
        message = getMessageLiteral(((StringLiteral) expr).getEscapedValue());
        break;
      case ASTNode.NUMBER_LITERAL:
        message = getMessageLiteral(((NumberLiteral) expr).getToken());
        break;
    }

    // 01 - Inform the reporter about the problem.
    getReporter().addProblem(getVerifierId(), getCurrentResource(), expr, message);

    // 02 - Return false to inform whoever called this method that we have a vulnerability.
    return false;
  }

  private boolean checkInfixExpression(List<Integer> rules, Expression expr, int depth) {
    InfixExpression parameter = (InfixExpression) expr;

    // 01 - Get the elements from the operation.
    Expression leftOperand = parameter.getLeftOperand();
    Expression rightOperand = parameter.getRightOperand();
    List<Expression> extendedOperands = BindingResolver.getParameterTypes(parameter.extendedOperands());

    // 02 - Check each element.
    checkParameters(rules, leftOperand, depth);
    checkParameters(rules, rightOperand, depth);

    for (Expression expression : extendedOperands) {
      checkParameters(rules, expression, depth);
    }

    // 01 - Inform the reporter about the problem.
    // getReporter().addProblem(getVerifierId(), getCurrentResource(), expr, "checkInfixExpression");

    // 02 - Return false to inform whoever called this method that we have a vulnerability.
    return false;
  }

  private boolean checkSimpleName(List<Integer> rules, Expression expr, int depth) {
    IBinding binding = ((SimpleName) expr).resolveBinding();

    // 01 - Try to retrieve the variable from the list of variables.
    VariableBindingManager manager = getCallGraph().getlistVariables().get(binding);
    if (null != manager) {

      // 02 - This is the case where we have to go deeper into the variable's path.
      checkParameters(rules, manager.getInitializer(), depth);
    }

    return false;
  }

  private boolean checkMethodInvocation(List<Integer> rules, Expression expr, int depth) {
    PluginLogger.logInfo("Node Type: " + expr.getNodeType() + " - " + expr);

    return false;
  }

  private boolean checkMethodDeclaration(List<Integer> rules, Expression expr, int depth) {
    PluginLogger.logInfo("Node Type: " + expr.getNodeType() + " - " + expr);

    return false;
  }

  private String getMessageLiteral(String value) {
    return String.format(Messages.SecurityMisconfigurationVerifier.LITERAL, getVerifierName(), value);
  }
}
