package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.graph.VariableBindingManager;
import net.thecodemaster.sap.graph.VulnerabilityPath;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.points.ExitPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  static {
    // 01 - Loads all the ExitPoints of this verifier.
    loadExitPoints(Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID);
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
    Expression expr;
    VulnerabilityPath vp;
    for (List<Integer> rules : expectedParameters.values()) {
      // If the rules are null, it means the expected parameter can be anything. (We do not care for it).
      if (null != rules) {
        expr = receivedParameters.get(index);
        vp = new VulnerabilityPath(expr);

        checkExpression(vp, rules, expr, depth);
        if (!vp.isEmpty()) {
          showVulnerability(vp);
        }
      }
      index++;
    }
  }

  private void checkExpression(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
    // 01 - If the parameter matches the rules (Easy case), the parameter is okay, otherwise we need to check for more things.
    if (!matchRules(rules, expr)) {

      // To avoid infinitive loop, this check is necessary.
      if (Constants.MAXIMUM_DEPTH == depth) {
        return;
      }

      // 02 - We need to check the type of the parameter and deal with it accordingly to its type.
      switch (expr.getNodeType()) {
        case ASTNode.STRING_LITERAL:
        case ASTNode.NUMBER_LITERAL:
          checkLiteral(vp, expr);
          break;
        case ASTNode.INFIX_EXPRESSION:
          checkInfixExpression(vp, rules, expr, ++depth);
          break;
        case ASTNode.SIMPLE_NAME:
          checkSimpleName(vp, rules, expr, ++depth);
          break;
        case ASTNode.METHOD_INVOCATION:
          checkMethodInvocation(vp, rules, expr, ++depth);
          break;
        default:
          PluginLogger.logError("Default Node Type: " + expr.getNodeType() + " - " + expr, null);
      }
    }
  }

  private void checkLiteral(VulnerabilityPath vp, Expression expr) {
    String message = null;
    switch (expr.getNodeType()) {
      case ASTNode.STRING_LITERAL:
        message = getMessageLiteral(((StringLiteral) expr).getLiteralValue());
        break;
      case ASTNode.NUMBER_LITERAL:
        message = getMessageLiteral(((NumberLiteral) expr).getToken());
        break;
    }

    // 01 - Informs that this node is a vulnerability.
    vp.foundVulnerability(expr, message);
  }

  private void checkInfixExpression(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
    InfixExpression parameter = (InfixExpression) expr;

    // 01 - Get the elements from the operation.
    Expression leftOperand = parameter.getLeftOperand();
    Expression rightOperand = parameter.getRightOperand();
    List<Expression> extendedOperands = BindingResolver.getParameterTypes(parameter.extendedOperands());

    // 02 - Check each element.
    checkExpression(vp.addNodeToPath(leftOperand), rules, leftOperand, depth);
    checkExpression(vp.addNodeToPath(rightOperand), rules, rightOperand, depth);

    for (Expression expression : extendedOperands) {
      checkExpression(vp.addNodeToPath(expression), rules, expression, depth);
    }
  }

  private void checkSimpleName(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
    IBinding binding = ((SimpleName) expr).resolveBinding();

    // 01 - Try to retrieve the variable from the list of variables.
    VariableBindingManager manager = getCallGraph().getlistVariables().get(binding);
    if (null != manager) {

      // 02 - This is the case where we have to go deeper into the variable's path.
      Expression initializer = manager.getInitializer();
      checkExpression(vp.addNodeToPath(initializer), rules, initializer, depth);
    }
  }

  private void checkMethodInvocation(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
    // 01 - Check if this method is a Sanitization-Point.

    // 02 - Check if this method is a Entry-Point.

    // 03 - Follow the data flow of this method and try to identify what is the return from it.

    // Get the implementation of this method. If the return is NULL it means this is a library that the developer
    // do not own the source code.
    MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), expr);

    if (null != methodDeclaration) {
      Block block = methodDeclaration.getBody();

      List<?> statements = block.statements();
      for (Object object : statements) {
        Statement statement = (Statement) object;
        checkStatement(vp, rules, statement, depth);
      }

    }
  }

  private void checkStatement(VulnerabilityPath vp, List<Integer> rules, Statement statement, int depth) {
    if (statement.getNodeType() == ASTNode.RETURN_STATEMENT) {
      ReturnStatement a = (ReturnStatement) statement;
      Expression expr = a.getExpression();
      checkExpression(vp.addNodeToPath(expr), rules, expr, depth);

      PluginLogger.logInfo("Return Statement - " + statement);
    }
    else if (statement.getNodeType() == ASTNode.IF_STATEMENT) {
      PluginLogger.logInfo("If Statement - " + statement);
    }
  }

  private String getMessageLiteral(String value) {
    return String.format(Messages.SecurityMisconfigurationVerifier.LITERAL, value);
  }

  private void showVulnerability(VulnerabilityPath vp) {
    PluginLogger.logInfo("VulnerabilityPath: " + vp.toString());
  }
}
