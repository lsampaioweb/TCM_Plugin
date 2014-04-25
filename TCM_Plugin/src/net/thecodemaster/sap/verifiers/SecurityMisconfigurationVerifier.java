package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER);
  }

  static {
    // These ExitPoints are common to all instances of this verifier.
    // 01 - Create each ExitPoint.
    ExitPoint exitPointGetConnection = new ExitPoint("java.sql.DriverManager", "getConnection");
    Map<Parameter, List<Integer>> paramsGetConnection = Creator.newMap();
    List<Integer> emptyList = Creator.newList();
    paramsGetConnection.put(new Parameter("java.lang.String"), null); // Anything is valid.
    paramsGetConnection.put(new Parameter("java.lang.String"), emptyList); // Only sanitized values are valid.
    paramsGetConnection.put(new Parameter("java.lang.String"), emptyList); // Only sanitized values are valid.
    exitPointGetConnection.setParameters(paramsGetConnection);

    // 02 - Add the ExitPoint to the list.
    getExitPoints().add(exitPointGetConnection);
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

  private void checkParameters(List<Integer> rules, Expression parameter, int depth) {
    // 01 - If the parameter matches the rules (Easy case), the parameter is okay.
    if (!matchRules(rules, parameter)) {

      // To avoid infinitive loop, this check is necessary.
      if (Constants.MAXIMUM_DEPTH == depth) {
        return;
      }

      PluginLogger.logInfo("Node Type: " + parameter.getNodeType() + " - " + parameter);
      // 02 - We need to check the type of the parameter and deal with it accordingly to its type.
      switch (parameter.getNodeType()) {
        case ASTNode.STRING_LITERAL:
          checkStringLiteral(rules, parameter);
          break;
        case ASTNode.INFIX_EXPRESSION:
          checkInfixExpression(rules, parameter, ++depth);
          break;
        case ASTNode.SIMPLE_NAME:

          break;
        case ASTNode.METHOD_INVOCATION:

          break;
        case ASTNode.METHOD_DECLARATION:

          break;
      }
    }
  }

  private void checkStringLiteral(List<Integer> rules, Expression parameter) {
    addMarker(getCurrentResource(), "(StringLiteral) - We have a vulnerability: ", parameter);
  }

  private void checkInfixExpression(List<Integer> rules, Expression expr, int depth) {
    InfixExpression parameter = (InfixExpression) expr;
    System.out.println("(InfixExpression) - We have a vulnerability: " + parameter);
  }

}
