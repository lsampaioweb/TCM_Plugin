package net.thecodemaster.sap.graph;

import java.util.List;

import net.thecodemaster.sap.utils.Creator;

/**
 * @author Luciano Sampaio
 */
public class Method {

  private String       file;
  private String       pkg;
  private String       clazz;
  private String       name;
  private List<String> parameters;
  private int          start     = -1;
  private int          end       = -1;
  private boolean      wasEdited = false;

  public Method() {
    parameters = Creator.newList();
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getPkg() {
    return pkg;
  }

  public void setPkg(String pkg) {
    this.pkg = pkg;
  }

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addParameter(String param) {
    parameters.add(param);
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public boolean wasEdited() {
    return wasEdited;
  }

  public void setWasEdited(boolean wasEdited) {
    this.wasEdited = wasEdited;
  }

  @Override
  public String toString() {
    return String.format("%s.%s.%s(%s);", getPkg(), getClazz(), getName(), getParameters().toString());
  }
}
