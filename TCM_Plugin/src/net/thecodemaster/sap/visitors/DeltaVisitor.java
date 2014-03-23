package net.thecodemaster.sap.visitors;

import net.thecodemaster.sap.logger.PluginLogger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Luciano Sampaio
 */
public class DeltaVisitor implements IResourceDeltaVisitor {

  IProgressMonitor monitor;

  /**
   * @param monitor
   */
  public DeltaVisitor(IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
    IResource resource = delta.getResource();
    if (resource instanceof IFile && resource.getName().endsWith(".java")) {
      PluginLogger.logInfo("DeltaVisitor - visit " + delta);
      IFile file = (IFile) resource;
      switch (delta.getKind()) {
        case IResourceDelta.ADDED:
          break;
        case IResourceDelta.REMOVED:
          break;
        case IResourceDelta.CHANGED:
          break;
      }
    }
    // Return true to continue visiting children.
    return true;
  }

}
