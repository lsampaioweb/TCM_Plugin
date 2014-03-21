package net.thecodemaster.sap.ui.propertytester;

import java.util.Collection;

import net.thecodemaster.sap.utils.Utils;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;

public class EnableDisableMenu extends PropertyTester {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    IProject project = Utils.extractProjectFromResource(receiver);

    // The collection of projects that are being monitored by our plug-in.
    Collection<IProject> monitoredProjects = Utils.getListOfMonitoredProjects();

    // If the project is already being monitored, the enable button should not be displayed.
    System.out.println(receiver.toString());
    if ("isEnabled".equals(property)) {
      // Checks if the current select project is in the list.
      return !monitoredProjects.contains(project);
    }
    if ("isDisabled".equals(property)) {
      // If the project is not being monitored, the disable button should not be displayed.
      return monitoredProjects.contains(project);
    }

    return false;
  }

}
