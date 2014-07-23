package net.thecodemaster.esvd.ui.propertytester;

import java.util.List;

import net.thecodemaster.esvd.helper.HelperProjects;
import net.thecodemaster.esvd.ui.enumeration.EnumVisibilityMenu;

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
    List<IProject> selectedProjects = HelperProjects.getSelectedProjects((ISelection) receiver);

    // The collection of projects that are being monitored by our plug-in.
    List<IProject> monitoredProjects = HelperProjects.getMonitoredProjects();

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
