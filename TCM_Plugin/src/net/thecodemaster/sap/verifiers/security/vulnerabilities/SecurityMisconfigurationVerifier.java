package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import java.util.List;

import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    // System.out.println("SecurityMisconfigurationVerifier - MethodDeclaration " + node.getName());

    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    // System.out.println("SecurityMisconfigurationVerifier - MethodInvocation " + node.getName());

    // IMethodBinding binding = node.resolveMethodBinding();
    // ITypeBinding type = binding.getDeclaringClass();
    Expression expression = node.getExpression();
    if (null != expression) {
      ITypeBinding typeBinding = expression.resolveTypeBinding();
      String className = typeBinding.getName();

      if (className.equals("DriverManager")) {
        System.out.println("Type: " + typeBinding.getName());

        SimpleName simpleName = node.getName();
        if (simpleName.getIdentifier().equals("getConnection")) {
          doVisitChildren(node.arguments());
        }
      }
    }

    return super.visit(node);
  }

  private void doVisitChildren(List<?> elements) {
    for (Object currentParameter : elements) {
      if (currentParameter instanceof SimpleName) {
        SimpleName parameter = (SimpleName) currentParameter;
        System.out.println("SimpleName " + parameter);
      }
      else if (currentParameter instanceof StringLiteral) {
        StringLiteral parameter = (StringLiteral) currentParameter;
        System.out.println("StringLiteral " + parameter);
      }
      else if (currentParameter instanceof InfixExpression) {
        InfixExpression parameter = (InfixExpression) currentParameter;
        System.out.println("InfixExpression " + parameter);
      }
    }
  }

}
