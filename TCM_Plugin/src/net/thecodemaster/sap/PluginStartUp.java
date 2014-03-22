package net.thecodemaster.sap;

import org.eclipse.ui.IStartup;

public class PluginStartUp implements IStartup {

  @Override
  public void earlyStartup() {
    // On Eclipse start up, the plug-in will start listening changes on resources under the monitored projects.
    Activator.getDefault().startResourceChangeListener();
  }

}
