package net.thecodemaster.evd.marker;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;

/**
 * @author Luciano Sampaio
 */
public class IgnoreResolution implements IMarkerResolution2 {

  public IgnoreResolution(IMarker marker) {
  }

  @Override
  public String getLabel() {
    return Message.VerifierSecurityVulnerability.LABEL_RESOLUTION;
  }

  @Override
  public String getDescription() {
    return Message.VerifierSecurityVulnerability.DESCRIPTION_RESOLUTION;
  }

  @Override
  public Image getImage() {
    return Activator.getImageDescriptor(Constant.Icons.SECURITY_VULNERABILITY).createImage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(IMarker marker) {
  }

}
