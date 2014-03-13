package tcm_plugin.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Luciano Sampaio
 */
public abstract class Utils {

  /**
   * Returns a list of projects that was inside the string passed as parameter.
   * 
   * @param listProjectsNames The string containing the names of all projects separated by the SEPARATOR constant.
   * @param separator The separator that was used between each value.
   * @return A list of projects
   */
  public static List<String> getListFromString(String listProjectsNames, String separator) {
    List<String> projectsNames = new ArrayList<String>();

    if (null != listProjectsNames) {
      projectsNames = Arrays.asList(listProjectsNames.split(separator));
    }

    return projectsNames;
  }
}
