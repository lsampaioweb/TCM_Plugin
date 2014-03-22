package net.thecodemaster.sap.utils;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

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
  public static Collection<String> fromStringToList(String content, String separator) {
    Collection<String> collection = Creator.newCollection();

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
}
