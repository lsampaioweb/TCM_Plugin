package net.thecodemaster.sap;

import net.thecodemaster.sap.listeners.JavaChangeListener;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "TheCodeMasterSecurityAnalyzerPlugin"; //$NON-NLS-1$

  // The shared instance
  private static Activator   plugin;

  // This listener will intercept changes on resources under the monitored projects and will invoke the
  // security vulnerability scanner.
  IElementChangedListener    listener;

  /**
   * The constructor
   */
  public Activator() {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;

    startResourceChangeListener();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    stopResourceChangeListener();

    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Start the listener that will monitor any changes on resources under the monitored projects.
   */
  public void startResourceChangeListener() {
    if (null == listener) {
      listener = new JavaChangeListener();
      JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_RECONCILE);
    }
  }

  /**
   * Stop the listener to avoid unnecessary processing.
   */
  public void stopResourceChangeListener() {
    if (null == listener) {
      JavaCore.removeElementChangedListener(listener);
      listener = null;
    }
  }

}
