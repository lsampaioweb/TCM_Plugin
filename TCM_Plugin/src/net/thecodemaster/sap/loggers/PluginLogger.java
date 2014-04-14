package net.thecodemaster.sap.loggers;

import net.thecodemaster.sap.Activator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Luciano Sampaio
 */
public class PluginLogger {
  public static void logInfo(String message) {
    log(IStatus.INFO, IStatus.OK, message, null);
  }

  public static void logError(Throwable exception) {
    logError("Unexpected Exception", exception);
  }

  public static void logError(String message, Throwable exception) {
    log(IStatus.ERROR, IStatus.OK, message, exception);
  }

  private static void log(int severity, int code, String message, Throwable exception) {
    log(createStatus(severity, code, message, exception));
  }

  private static IStatus createStatus(int severity, int code, String message, Throwable exception) {
    return new Status(severity, Activator.PLUGIN_ID, code, message, exception);
  }

  private static void log(IStatus status) {
    Activator.getDefault().getLog().log(status);
  }
}
