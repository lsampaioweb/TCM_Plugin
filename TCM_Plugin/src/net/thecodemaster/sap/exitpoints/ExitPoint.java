package net.thecodemaster.sap.exitpoints;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.arguments.Argument;

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

  private String                       packageName;
  private String                       methodName;
  private Map<Argument, List<Integer>> arguments;

  public ExitPoint(String packageName, String methodName) {
    this.packageName = packageName;
    this.methodName = methodName;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public Map<Argument, List<Integer>> getArguments() {
    return arguments;
  }

  public void setArguments(Map<Argument, List<Integer>> arguments) {
    this.arguments = arguments;
  }
}
