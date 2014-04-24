package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.arguments.Argument;
import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Convert;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.verifiers.Verifier;
import net.thecodemaster.sap.verifiers.helpers.VariableBindingManager;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  static {
    // These ExitPoints are common to all instances of this verifier.
    // 01 - Create each ExitPoint.
    ExitPoint exitPointGetConnection = new ExitPoint("java.sql.DriverManager", "getConnection");
    Map<Argument, List<Integer>> argsGetConnection = Creator.newMap();
    argsGetConnection.put(new Argument("java.lang.String"), null);
    argsGetConnection.put(new Argument("java.lang.String"), Arrays.asList(ASTNode.STRING_LITERAL, ASTNode.INFIX_EXPRESSION));
    argsGetConnection.put(new Argument("java.lang.String"), Arrays.asList(ASTNode.STRING_LITERAL, ASTNode.INFIX_EXPRESSION));
    exitPointGetConnection.setArguments(argsGetConnection);

    // 02 - Add the ExitPoint to the list.
    getListExitPoints().add(exitPointGetConnection);
  }

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER);
  }

  @Override
  protected void run() {
    for (MethodInvocation node : getListMethodInvocations().values()) {
      PluginLogger.logInfo("run - " + node);
      // Verify if the current method belongs to the list of ExitPoints.
      ExitPoint exitPoint = isMethodAnExitPoint(node);

      if (null != exitPoint) {
        // This is an important NODE and it needs to be verified.
        validateNode(node, exitPoint);
      }
    }
  }

  private void validateNode(MethodInvocation node, ExitPoint exitPoint) {
    Map<Argument, List<Integer>> expectedArguments = exitPoint.getArguments();
    List<Expression> receivedArguments = Convert.fromListObjectToListExpression(node.arguments());

    int index = 0;
    for (List<Integer> rules : expectedArguments.values()) {
      validadeArgument(rules, receivedArguments.get(index++));
    }
  }

  private void validadeArgument(List<Integer> rules, Expression argument) {
    if (null == rules) {
      // This argument can be of any type and content.
      return;
    }

    validadeArgumentPath(rules, argument);
  }

  private void validadeArgumentPath(List<Integer> rules, ASTNode argument) {
    if ((null != argument) && (argument.getNodeType() != ASTNode.METHOD_DECLARATION)) {
      // // 01 - This is the case where the arguments match the rules. (Easy case).
      if (!matchRules(rules, argument)) {

        if (argument.getNodeType() == ASTNode.SIMPLE_NAME) {
          IBinding binding = ((SimpleName) argument).resolveBinding();
          PluginLogger.logInfo(argument.toString());
          for (IVariableBinding vBinding : getLocalVariables().keySet()) {
            PluginLogger.logInfo(vBinding.toString());
          }

          if (getLocalVariables().containsKey(binding)) {
            PluginLogger.logInfo("1");
            VariableBindingManager manager = getLocalVariables().get(binding);

            // 02 - This is the case where we have to go deeper into the argument's path.
            validadeArgumentPath(rules, manager.getInitializer());
          }
        }
        else if (argument.getNodeType() == ASTNode.METHOD_INVOCATION) {
          IMethodBinding binding = ((MethodInvocation) argument).resolveMethodBinding();
          if (getListMethodDeclarations().containsKey(binding)) {
            PluginLogger.logInfo("METHOD_DECLARATION - " + argument);

            MethodDeclaration method2 = getListMethodDeclarations().get(binding);

            Block block = method2.getBody();

            List<?> statements = block.statements();
            for (Object object : statements) {
              Statement statement = (Statement) object;
              PluginLogger.logInfo("statement - " + statement);
            }

            validadeArgumentPath(rules, block);
          }

        }
      }
    }
  }

  private boolean matchRules(List<Integer> rules, ASTNode argument) {
    for (Integer astNodeValue : rules) {
      if (argument.getNodeType() == astNodeValue) {
        // TODO - We found a vulnerability.
        PluginLogger.logInfo("We have a vulnerability: " + argument);
        return true;
      }
    }

    return false;
  }

}
