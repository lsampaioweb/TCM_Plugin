package net.thecodemaster.sap;

import org.eclipse.ui.IStartup;

public class PluginStartUp implements IStartup {

  @Override
  public void earlyStartup() {
    System.out.println("earlyStartup");
  }

}
