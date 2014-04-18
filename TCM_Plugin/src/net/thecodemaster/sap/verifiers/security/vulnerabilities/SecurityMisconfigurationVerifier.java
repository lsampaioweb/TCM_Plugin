package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER);
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    System.out.println("VariableDeclarationStatement " + node);

    for (Iterator<?> iter = node.fragments().iterator(); iter.hasNext();) {
      VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

      listVariables.put(fragment.getName().getIdentifier(), fragment);
      System.out.println("VariableDeclarationStatement - fragments - " + fragment.getName().getIdentifier());
    }
    return super.visit(node);
    // return false; // prevent that SimpleName is interpreted as reference
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("MethodInvocation " + node.getName());

    SimpleName simpleName = node.getName();
    if (simpleName.getIdentifier().equals("getConnection")) {
      // IMethodBinding methodBinding = node.resolveMethodBinding();
      // ITypeBinding returnTypeBinding = methodBinding.getReturnType();
      // String qualifiedName = returnTypeBinding.getQualifiedName();
      // ITypeBinding type = methodBinding.getDeclaringClass();

      Expression expression = node.getExpression();
      if (null != expression) {
        ITypeBinding typeBinding = expression.resolveTypeBinding();
        String className = typeBinding.getName();

        if (className.equals("DriverManager")) {
          System.out.println("Type: " + typeBinding.getName());

          doVisitChildren(node.arguments());
        }
      }
    }

    return super.visit(node);
  }

  private HashMap<String, VariableDeclarationFragment> listVariables =
                                                                       new HashMap<String, VariableDeclarationFragment>();

  private void doVisitChildren(List<?> elements) {
    for (Object currentParameter : elements) {
      if (currentParameter instanceof StringLiteral) {
        StringLiteral parameter = (StringLiteral) currentParameter;
        System.out.println("(StringLiteral) - We have a vulnerability: " + parameter);
      }
      else if (currentParameter instanceof InfixExpression) {
        InfixExpression parameter = (InfixExpression) currentParameter;
        System.out.println("(InfixExpression) - We have a vulnerability: " + parameter);
      }
      else if (currentParameter instanceof SimpleName) {
        SimpleName parameter = (SimpleName) currentParameter;

        for (Entry<String, VariableDeclarationFragment> entry : listVariables.entrySet()) {
          String key = entry.getKey();

          if (parameter.getIdentifier().equals(key)) {
            VariableDeclarationFragment fragment = entry.getValue();

            Expression expression = fragment.getInitializer();
            if (null != expression) {
              if (expression.getNodeType() == ASTNode.STRING_LITERAL) {
                System.out.println("(StringLiteral) - We have a vulnerability: " + parameter);
              }
              else if (expression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
                System.out.println("(InfixExpression) - We have a vulnerability: " + parameter);
              }
              else if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
                // ASTNode parentNode = parameter.getParent();
                // while (parentNode.getNodeType() != ASTNode.METHOD_DECLARATION) {
                // parentNode = parentNode.getParent();
                // }
              }
            }
          }
        }

        // System.out.println("SimpleName " + parameter);
      }
    }
  }

}
