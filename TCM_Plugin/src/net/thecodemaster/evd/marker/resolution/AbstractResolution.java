package net.thecodemaster.evd.marker.resolution;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.ui.view.ViewDataModel;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;

public abstract class AbstractResolution implements IMarkerResolution2 {

	private final int			position;
	private final IMarker	marker;
	private String				label;
	private String				description;

	public AbstractResolution(int position, IMarker marker) {
		this.position = position;
		this.marker = marker;
	}

	private int getPosition() {
		return position;
	}

	protected String getStrPosition() {
		return String.format("%02d - ", getPosition());
	}

	protected final void setLabel(String label) {
		this.label = label;
	}

	private String getLocalLabel() {
		return label;
	}

	@Override
	public String getLabel() {
		return getStrPosition() + getLocalLabel();
	}

	@Override
	public Image getImage() {
		return Activator.getImageDescriptor(Constant.Icons.SECURITY_VULNERABILITY_QUICK_FIX_OPTION).createImage();
	}

	protected final void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Get the ViewDataModel of this marker.
	 * 
	 * @param marker
	 *          The marker that will be used to retrieve the ViewDataModel.
	 * @return the ViewDataModel of this marker.
	 */
	protected ViewDataModel getViewDataModelFromMarker(IMarker marker) {
		return Reporter.getViewDataModel(marker);
	}

	/**
	 * Add our invisible annotation into the source code.
	 * 
	 * @param marker
	 *          The marker that will be used to add the annotation.
	 */
	protected void addInvisibleAnnotation(ASTNode node) {
		Reporter.addInvisibleAnnotation(node);
	}

	protected void clearProblem(ViewDataModel vdm, boolean removeChildren) {
		Reporter.clearProblem(vdm, removeChildren);
	}

}