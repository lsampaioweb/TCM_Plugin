package net.thecodemaster.sap.listeners;

import java.util.Collection;

import net.thecodemaster.sap.utils.Utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * An element changed listener receives notification of changes to Java elements maintained by the Java model.
 * 
 * @author Luciano Sampaio
 */
public class JavaChangeListener implements IElementChangedListener {

  @Override
  public void elementChanged(ElementChangedEvent event) {
    // Delta describing the change.
    IJavaElementDelta delta = event.getDelta();
    if (null != delta) {
      // Get the project which the changed resource belongs to.
      IProject resourceProject = (IProject) delta.getElement().getJavaProject().getAdapter(IProject.class);

      // The collection of projects that are being monitored by our plug-in.
      Collection<IProject> monitoredProjects = Utils.getMonitoredProjects();

      // Checks if the project is in the monitored list.
      if (monitoredProjects.contains(resourceProject)) {
        // On perform detection on projects selected by the developer.
        checkTypeOfResourceChange(delta);
      }
    }
  }

  void checkTypeOfResourceChange(IJavaElementDelta delta) {
    switch (delta.getKind()) {
      case IJavaElementDelta.ADDED:
        System.out.println(delta.getElement() + " was added");
        break;
      case IJavaElementDelta.REMOVED:
        System.out.println(delta.getElement() + " was removed");
        break;
      case IJavaElementDelta.CHANGED:
        System.out.println(delta.getElement() + " was changed");
        if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
          System.out.println("The change was in its children");
        }
        if ((delta.getFlags() & IJavaElementDelta.F_CONTENT) != 0) {
          checkForVulnerabilities(delta);
          System.out.println("The change was in its content");
        }
        /* Others flags can also be checked */
        break;
    }
    IJavaElementDelta[] children = delta.getAffectedChildren();
    for (int i = 0; i < children.length; i++) {
      checkTypeOfResourceChange(children[i]);
    }
  }

  private void checkForVulnerabilities(IJavaElementDelta delta) {
    // TODO
    CompilationUnit compilationUnitAST = delta.getCompilationUnitAST();

    if (compilationUnitAST == null) {
      return;
    }

    ICompilationUnit cu = (ICompilationUnit) compilationUnitAST.getJavaElement().getPrimaryElement();

  }

}
