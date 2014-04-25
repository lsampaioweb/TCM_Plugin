package net.thecodemaster.sap.markers;

import net.thecodemaster.sap.Activator;
import net.thecodemaster.sap.constants.Constants;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

public class ImageProvider implements IAnnotationImageProvider {

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getManagedImage(Annotation annotation) {
    return Activator.getImageDescriptor(Constants.Icons.SECURITY_VULNERABILITY).createImage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImageDescriptor getImageDescriptor(String imageDescriptorId) {
    return Activator.getImageDescriptor(Constants.Icons.SECURITY_VULNERABILITY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getImageDescriptorId(Annotation annotation) {
    return null;
  }

}
