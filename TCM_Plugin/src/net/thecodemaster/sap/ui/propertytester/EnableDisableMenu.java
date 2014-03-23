package net.thecodemaster.sap.ui.propertytester;

import java.util.Collection;

import net.thecodemaster.sap.ui.enumerations.EnumVisibilityMenu;
import net.thecodemaster.sap.utils.UtilProjects;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;

public class EnableDisableMenu extends PropertyTester {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    // I can do (ISelection) receiver because I said this is the only object I accept.
    Collection<IProject> selectedProjects = UtilProjects.getSelectedProjects((ISelection) receiver);

    // The collection of projects that are being monitored by our plug-in.
    Collection<IProject> monitoredProjects = UtilProjects.getMonitoredProjects();

    if (EnumVisibilityMenu.IS_ENABLED.toString().equals(property)) {
      // If ALL selected projects are already being monitored, the enable button should not be displayed.
      for (IProject project : selectedProjects) {
        // Checks if the current selected project is NOT in the list.
        if (!monitoredProjects.contains(project)) {
          return true;
        }
      }
    }
    if (EnumVisibilityMenu.IS_DISABLED.toString().equals(property)) {
      // If ALL selected projects are NOT already being monitored, the disable button should not be displayed.
      for (IProject project : selectedProjects) {
        // Checks if the current selected project is in the list.
        if (monitoredProjects.contains(project)) {
          return true;
        }
      }
    }

    return false;
  }

}
