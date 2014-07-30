package net.thecodemaster.esvd.logger;

import net.thecodemaster.esvd.Activator;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Luciano Sampaio
 * @Date: 2014-05-24
 * @Version: 01
 */
public class PluginLogger {

	public static void logIfDebugging(String message) {
		if (Activator.getDefault().isDebugging()) {
			logInfo(message);
		}
	}

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
		Activator activator = Activator.getDefault();
		if (null != activator) {
			ILog logger = activator.getLog();
			if (null != logger) {
				logger.log(status);
			}
		}
	}
}
