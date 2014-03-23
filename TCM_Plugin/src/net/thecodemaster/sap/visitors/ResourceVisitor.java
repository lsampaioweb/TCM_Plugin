package net.thecodemaster.sap.visitors;

import net.thecodemaster.sap.logger.PluginLogger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Luciano Sampaio
 */
public class ResourceVisitor implements IResourceVisitor {

  IProgressMonitor monitor;

  /**
   * @param monitor
   */
  public ResourceVisitor(IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResource resource) throws CoreException {
    if (resource instanceof IFile && resource.getName().endsWith(".java")) {

      PluginLogger.logInfo("ResourceVisitor - visit" + resource);

      // Job job = new Job("Early Vulnerability Detection - Job") {
      // @Override
      // protected IStatus run(IProgressMonitor monitor) {
      // monitor.beginTask("check for new version", 2);
      // monitor.subTask("seconds left = 2");
      // monitor.worked(1);
      // if (monitor.isCanceled()) {
      //
      // }
      // monitor.subTask("seconds left = 1");
      // monitor.worked(1);
      // monitor.done();
      // PluginLogger.logInfo("ResourceVisitor - job run");
      // // try {
      // // }
      // // catch (CoreException e) {
      // // }
      // return Status.OK_STATUS;
      // }
      // };
      // job.schedule();
    }
    // Return true to continue visiting children.
    return true;
  }

}
