package net.thecodemaster.sap.jobs;

import net.thecodemaster.sap.Manager;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.ui.l10n.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Luciano Sampaio
 */
public class ManagerJob extends Job {

  private Manager        manager;
  private IProject       project;
  private IResourceDelta delta;

  private ManagerJob(String name, Manager manager) {
    super(name);

    this.manager = manager;
  }

  public ManagerJob(String name, Manager manager, IProject project) {
    this(name, manager);

    this.project = project;
  }

  public ManagerJob(String name, Manager manager, IResourceDelta delta) {
    this(name, manager);

    this.delta = delta;
  }

  public void run() {
    schedule();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    manager.setProgressMonitor(monitor);

    try {
      monitor.beginTask(Messages.Plugin.TASK, IProgressMonitor.UNKNOWN);

      if (null != delta) {
        delta.accept(manager);
      }

      if (null != project) {
        project.accept(manager);
      }
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
    }
    finally {
      if (monitor != null) {
        monitor.done();
      }
    }

    return Status.OK_STATUS;
  }

}
