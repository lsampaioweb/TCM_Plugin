package net.thecodemaster.sap.points;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.graph.Parameter;

/**
 * @author Luciano Sampaio
 * 
 *         Package : java.sql.DriverManager
 *         Method : getConnection
 *         Parameters:
 *         [0] : -1; (String url)
 *         [1] : 0; (String user)
 *         [2] : 0; (String password)
 */
public class ExitPoint extends AbstractPoint {

  private Map<Parameter, List<Integer>> parameters;

  public ExitPoint(String qualifiedName, String methodName) {
    super(qualifiedName, methodName);
  }

  public Map<Parameter, List<Integer>> getParameters() {
    return parameters;
  }

  public void setParameters(Map<Parameter, List<Integer>> parameters) {
    this.parameters = parameters;
  }
}
