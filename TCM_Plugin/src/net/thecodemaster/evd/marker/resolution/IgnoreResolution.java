package net.thecodemaster.evd.marker.resolution;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.ui.view.ViewDataModel;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * @author Luciano Sampaio
 */
public class IgnoreResolution extends AbstractResolution {

	public IgnoreResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(Message.VerifierSecurityVulnerability.LABEL_RESOLUTION_IGNORE_RESOLUTION);
		setDescription(Message.VerifierSecurityVulnerability.DESCRIPTION_RESOLUTION_IGNORE_RESOLUTION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IMarker marker) {
		try {
			// 01 - Get the ViewDataModel of this marker.
			ViewDataModel vdm = getViewDataModelFromMarker(marker);

			if (null != vdm) {
				// 02 - Get the ICompilationUnit which this marker belongs to.
				ICompilationUnit cu = getCompilationUnit(marker);

				if (null != cu) {
					// 03 - Add an annotation to the code.
					ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
					ITextFileBuffer buffer = manager.getTextFileBuffer(cu.getPath(), LocationKind.IFILE);
					if (null != buffer) {
						Annotation annotation = new Annotation(Constant.MARKER_ID_ANNOTATION_INVISIBLE, false, null);

						int offset = marker.getAttribute(IMarker.CHAR_START, -1);
						int length = marker.getAttribute(IMarker.CHAR_END, -1) - offset;
						IAnnotationModel model = buffer.getAnnotationModel();
						model.addAnnotation(annotation, new Position(offset, length));

						// 04 - Delete the Markers and the lines from our Security Vulnerability View.
						Reporter.clearProblem(marker);
					}
				}
			}
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}

}
