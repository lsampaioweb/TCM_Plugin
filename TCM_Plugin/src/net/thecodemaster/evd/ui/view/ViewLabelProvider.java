package net.thecodemaster.evd.ui.view;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.helper.HelperVerifiers;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

class ViewLabelProvider implements ITableLabelProvider {

	private String getTypeVulnerabilityName(int typeVulnerability) {
		return HelperVerifiers.getTypeVulnerabilityName(typeVulnerability);
	}

	private String getImage(ViewDataModel vdm) {
		int nrChildren = vdm.getChildren().size();

		if (nrChildren == 0) {
			return Constant.Icons.SECURITY_VULNERABILITY_ENTRY;
		} else if (nrChildren == 1) {
			return Constant.Icons.SECURITY_VULNERABILITY_EXIT;
		} else {
			return Constant.Icons.SECURITY_VULNERABILITY_MULTIPLE;
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
			case 0:
				String image = getImage((ViewDataModel) element);
				return Activator.getImageDescriptor(image).createImage();
			default:
				return null;
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		ViewDataModel vdm = (ViewDataModel) element;

		switch (columnIndex) {
			case 0:
				return vdm.getMessage();
			case 1:
				return String.format("%d", vdm.getLineNumber());
			case 2:
				return getTypeVulnerabilityName(vdm.getTypeVulnerability());
			case 3:
				return vdm.getResource().getName();
			case 4:
				return vdm.getFullPath();
			default:
				return null;
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}
}
