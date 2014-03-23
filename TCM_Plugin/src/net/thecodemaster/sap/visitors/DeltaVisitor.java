package net.thecodemaster.sap.visitors;

import net.thecodemaster.sap.utils.PluginLogger;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Luciano Sampaio
 */
public class DeltaVisitor implements IResourceDeltaVisitor {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
    PluginLogger.logInfo("DeltaVisitor - visit " + delta.getKind());
    // IResource resource = delta.getResource();
    switch (delta.getKind()) {
      case IResourceDelta.ADDED:
        break;
      case IResourceDelta.REMOVED:
        break;
      case IResourceDelta.CHANGED:
        break;
    }
    // Return true to continue visiting children.
    return true;
  }

}
