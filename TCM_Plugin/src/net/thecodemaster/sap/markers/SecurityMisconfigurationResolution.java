package net.thecodemaster.sap.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationResolution implements IMarkerResolution2 {
  @Override
  public String getLabel() {
    return "Create a new property key";
  }

  @Override
  public String getDescription() {
    return "Append a new property key/value pair to the plugin.properties file.";
  }

  @Override
  public Image getImage() {
    // return Activator.getImageDescriptor(Constants.Icons.SECURITY_VULNERABILITY).createImage();
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(IMarker marker) {
  }

}
