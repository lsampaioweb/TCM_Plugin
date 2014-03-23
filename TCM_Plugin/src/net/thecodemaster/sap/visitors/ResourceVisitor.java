package net.thecodemaster.sap.visitors;

import net.thecodemaster.sap.utils.PluginLogger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Luciano Sampaio
 */
public class ResourceVisitor implements IResourceVisitor {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResource resource) throws CoreException {
    PluginLogger.logInfo("ResourceVisitor - visit");
    // Return true to continue visiting children.
    return true;
  }

}
