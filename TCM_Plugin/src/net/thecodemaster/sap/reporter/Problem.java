package net.thecodemaster.sap.reporter;

import java.util.List;

/**
 * @author Luciano Sampaio
 */
public class Problem {

  private String         id;
  private String         fileName;
  private String         methodName;
  private int            lineNumber;
  private String         description;
  private List<Solution> solutions;

}
