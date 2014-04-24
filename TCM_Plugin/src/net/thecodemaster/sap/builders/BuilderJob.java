package net.thecodemaster.sap.builders;

import java.util.List;

import net.thecodemaster.sap.Manager;
import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.utils.Timer;
import net.thecodemaster.sap.visitors.CallGraphVisitor;

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
public class BuilderJob extends Job {

  private IProject         project;
  private IResourceDelta   delta;

  /**
   * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
   * At any given time, we should only have on call graph of the code.
   */
  private static CallGraph callGraph;

  private BuilderJob(String name) {
    super(name);

    synchronized (BuilderJob.class) {
      if (callGraph == null) {
        callGraph = new CallGraph();
      }
    }
  }

  public BuilderJob(String name, IProject project) {
    this(name);

    this.project = project;
  }

  public BuilderJob(String name, IResourceDelta delta) {
    this(name);

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
    try {
      monitor.beginTask(Messages.Plugin.TASK, IProgressMonitor.UNKNOWN);

      CallGraphVisitor callGraphVisitor = new CallGraphVisitor(callGraph);
      List<String> updatedResources = Creator.newList();

      if (null != delta) {
        Timer timer = (new Timer("Call Graph Delta: ")).start();
        updatedResources = callGraphVisitor.run(delta);
        PluginLogger.logInfo(timer.stop().toString());
      }

      if (null != project) {
        Timer timer = (new Timer("Call Graph Project: ")).start();
        updatedResources = callGraphVisitor.run(project);
        PluginLogger.logInfo(timer.stop().toString());
      }

      if ((null != monitor) && (!monitor.isCanceled())) {
        Timer timer = (new Timer("Plugin verifications: ")).start();
        Manager manager = Manager.getInstance();
        manager.setProgressMonitor(monitor);
        manager.run(updatedResources, callGraph);
        PluginLogger.logInfo(timer.stop().toString());
      }
      else {
        // The user canceled the operation.
        return Status.CANCEL_STATUS;
      }
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
      return e.getStatus();
    }
    finally {
      if (monitor != null) {
        monitor.done();
      }
    }

    return Status.OK_STATUS;
  }
}
