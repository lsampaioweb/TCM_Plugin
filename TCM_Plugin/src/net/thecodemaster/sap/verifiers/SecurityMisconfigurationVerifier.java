package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.jdt.core.dom.Expression;

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
    getListExitPoints().add(exitPointGetConnection);
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
    for (List<Integer> rules : expectedParameters.values()) {
      checkParameters(rules, receivedParameters.get(index++));
    }
  }

  private void checkParameters(List<Integer> rules, Expression parameter) {
    if (null == rules) {
      // This parameter can be of any type and content.
      return;
    }

    // 01 - If the parameter matches the rules (Easy case), the parameter is okay.
    if (!matchRules(rules, parameter)) {

    }
  }

  protected boolean matchRules(List<Integer> rules, Expression parameter) {
    for (Integer astNodeValue : rules) {
      if (parameter.getNodeType() == astNodeValue) {
        // TODO - We found a vulnerability.
        PluginLogger.logInfo("We have a vulnerability: " + parameter);
        return true;
      }
    }

    return false;
  }

}
