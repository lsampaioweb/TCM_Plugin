package net.thecodemaster.sap.exitpoints;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.graph.Parameter;

/**
 * @author Luciano Sampaio
 *         Package : java.sql.DriverManager
 *         Method : getConnection
 *         Parameters:
 *         [0] : null; (String url)
 *         [1] : StringLiteral;InfixExpression; (String user)
 *         [2] : StringLiteral;InfixExpression; (String password)
 */
public class ExitPoint {

  private String                        qualifiedName;
  private String                        methodName;
  private Map<Parameter, List<Integer>> parameters;

  public ExitPoint(String qualifiedName, String methodName) {
    this.qualifiedName = qualifiedName;
    this.methodName = methodName;
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public String getMethodName() {
    return methodName;
  }

  public Map<Parameter, List<Integer>> getParameters() {
    return parameters;
  }

  public void setParameters(Map<Parameter, List<Integer>> parameters) {
    this.parameters = parameters;
  }
}
