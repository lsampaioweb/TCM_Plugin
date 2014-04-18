package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.arguments.Argument;
import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Convert;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER);

    // 02 - Create each ExitPoint.
    ExitPoint exitPointGetConnection = new ExitPoint("java.sql.DriverManager", "getConnection");
    Map<Argument, List<Integer>> argumentsGetConnection = Creator.newMap();
    argumentsGetConnection.put(new Argument("java.lang.String", "url"), null);
    argumentsGetConnection.put(new Argument("java.lang.String", "userName"),
      Arrays.asList(ASTNode.STRING_LITERAL, ASTNode.INFIX_EXPRESSION));
    argumentsGetConnection.put(new Argument("java.lang.String", "password"),
      Arrays.asList(ASTNode.STRING_LITERAL, ASTNode.INFIX_EXPRESSION));
    exitPointGetConnection.setArguments(argumentsGetConnection);

    // 03 - Add the ExitPoint to the list.
    listExitPoints.add(exitPointGetConnection);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    // Verify if the current method belongs to the list of ExitPoints.
    ExitPoint exitPoint = isMethodAnExitPoint(listExitPoints, node);

    if (null != exitPoint) {
      // This is an important NODE and it needs to be verified.
      validateNode(node, exitPoint);
    }

    return super.visit(node);
  }

  private ExitPoint isMethodAnExitPoint(List<ExitPoint> listExitPoints, MethodInvocation node) {
    // The name of the current method.
    String methodName = node.getName().getIdentifier();

    for (ExitPoint currentExitPoint : listExitPoints) {
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

            if (expectedArguments.size() == receivedArguments.size()) {
              boolean isMethodAnExitPoint = true;
              int index = 0;
              for (Argument expectedArgument : expectedArguments.keySet()) {
                ITypeBinding receivedArgument = receivedArguments.get(index++).resolveTypeBinding();

                // Verify if all the arguments are the ones expected.
                if (!expectedArgument.getType().equals(receivedArgument.getQualifiedName())) {
                  isMethodAnExitPoint = false;
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

  private void validateNode(MethodInvocation node, ExitPoint exitPoint) {
    List<Expression> arguments = Convert.fromListObjectToListExpression(node.arguments());
    Map<Argument, List<Integer>> mapArguments = exitPoint.getArguments();

    for (Expression argument : arguments) {
      System.out.println("Argument " + argument);
      if (argument.getNodeType() == ASTNode.STRING_LITERAL) {

      }
    }

  }

}
