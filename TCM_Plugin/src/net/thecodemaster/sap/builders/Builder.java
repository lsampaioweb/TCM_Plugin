package net.thecodemaster.sap.builders;

import java.util.Map;

import net.thecodemaster.sap.Manager;
import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.logger.PluginLogger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class Builder extends IncrementalProjectBuilder {

  /**
   * {@inheritDoc}
   */
  @Override
  protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
    throws CoreException {
    try {
      if (kind == FULL_BUILD) {
        fullBuild(monitor);
      }
      else if (kind == CLEAN_BUILD) {
        clean(monitor);
      }
      else {
        IResourceDelta delta = getDelta(getProject());
        if (null == delta) {
          fullBuild(monitor);
        }
        else {
          incrementalBuild(delta, monitor);
        }
      }
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void clean(IProgressMonitor monitor) throws CoreException {
    // Delete markers set and files created.
    getProject().deleteMarkers(Constants.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
  }

  /**
   * @param monitor
   * @throws CoreException
   */
  protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
    getProject().accept(getManager(monitor));
  }

  /**
   * @param delta
   * @param monitor
   * @throws CoreException
   */
  protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
    delta.accept(getManager(monitor));
  }

  /**
   * @param monitor
   * @return An instance of the manager analyzer.
   */
  private Manager getManager(IProgressMonitor monitor) {
    Manager manager = Manager.getInstance();
    manager.setMonitor(monitor);

    return manager;
  }

}
