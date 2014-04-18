package net.thecodemaster.sap.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class Convert {

  /**
   * Returns a collection of strings that was inside the string passed as parameter.
   * 
   * @param content The string containing its content separated by the SEPARATOR constant.
   * @param separator The separator that was used between each value.
   * @return A collection of strings;
   */
  public static List<String> fromStringToList(String content, String separator) {
    List<String> collection = Creator.newList();

    if ((null != content) && (content.length() > 0)) {
      collection = Arrays.asList(content.split(separator));
    }

    return collection;
  }

  /**
   * Extract the project where this resource is under.
   * 
   * @param element the element that will be used to extract the project.
   * @return The project where this resource is under.
   */
  public static IProject fromResourceToProject(Object element) {
    if (!(element instanceof IResource)) {
      if (!(element instanceof IAdaptable)) {
        return null;
      }
      element = ((IAdaptable) element).getAdapter(IResource.class);
      if (!(element instanceof IResource)) {
        return null;
      }
    }
    if (!(element instanceof IProject)) {
      element = ((IResource) element).getProject();
      if (!(element instanceof IProject)) {
        return null;
      }
    }

    return (IProject) element;
  }

  /**
   * This method was created because the list returned from the arguments is not generic.
   * 
   * @param arguments The live ordered list of argument expressions in this method invocation expression.
   * @return List<Expression>
   */
  @SuppressWarnings("unchecked")
  public static List<Expression> fromListObjectToListExpression(List<?> arguments) {
    if (null != arguments) {
      return (List<Expression>) arguments;
    }

    return Creator.newList();
  }
}
