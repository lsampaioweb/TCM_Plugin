package net.thecodemaster.evd.point;

import java.util.List;

/**
 * @author Luciano Sampaio
 *         Package : javax.servlet.http.HttpServletRequest
 *         Method : getParameter
 *         Parameters:
 *         [0] : java.lang.String
 */
public class EntryPoint extends AbstractPoint {

  private List<String> parameters;

  public EntryPoint(String qualifiedName, String methodName) {
    super(qualifiedName, methodName);
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }
}
