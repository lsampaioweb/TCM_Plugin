package net.thecodemaster.sap.verifiers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  static {
    // These ExitPoints are common to all instances of this verifier.
    // 01 - Create each ExitPoint.
    ExitPoint exitPointGetConnection = new ExitPoint("java.sql.DriverManager", "getConnection");
    Map<Parameter, List<Integer>> argsGetConnection = Creator.newMap();
    argsGetConnection.put(new Parameter("java.lang.String"), null);
    argsGetConnection.put(new Parameter("java.lang.String"), Arrays.asList(ASTNode.STRING_LITERAL, ASTNode.INFIX_EXPRESSION));
    argsGetConnection.put(new Parameter("java.lang.String"), Arrays.asList(ASTNode.STRING_LITERAL, ASTNode.INFIX_EXPRESSION));
    exitPointGetConnection.setParameters(argsGetConnection);

    // 02 - Add the ExitPoint to the list.
    getListExitPoints().add(exitPointGetConnection);
  }

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void run(IMethodBinding method, ExitPoint exitPoint) {
  }

}
