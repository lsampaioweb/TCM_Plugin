package net.thecodemaster.sap.points;

/**
 * @author Luciano Sampaio
 */
public abstract class AbstractPoint {

  private String qualifiedName;
  private String methodName;

  public AbstractPoint(String qualifiedName, String methodName) {
    this.qualifiedName = qualifiedName;
    this.methodName = methodName;
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public String getMethodName() {
    return methodName;
  }

}