package net.thecodemaster.sap.builders;

import java.util.Map;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.utils.PluginLogger;
import net.thecodemaster.sap.visitors.DeltaVisitor;
import net.thecodemaster.sap.visitors.ResourceVisitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class Builder extends IncrementalProjectBuilder {

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
   * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
    throws CoreException {
    if (kind == FULL_BUILD) {
      fullBuild(monitor);
    }
    else {
      IResourceDelta delta = getDelta(getProject());
      if (delta == null) {
        fullBuild(monitor);
      }
      else {
        incrementalBuild(delta, monitor);
      }
    }
    return null;
  }

  @Override
  protected void clean(IProgressMonitor monitor) throws CoreException {
    // Delete markers set and files created.
    getProject().deleteMarkers(Constants.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
  }

  protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
    try {
      getProject().accept(new ResourceVisitor());
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
    }
  }

  protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
    delta.accept(new DeltaVisitor());
  }
}
