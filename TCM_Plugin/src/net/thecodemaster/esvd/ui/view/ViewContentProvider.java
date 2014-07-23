package net.thecodemaster.esvd.ui.view;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in adapters
 * or simply return objects as-is. These objects may be sensitive to the current input of the view, or ignore it and
 * always show the same content (like Task List, for example).
 */
class ViewContentProvider implements ITreeContentProvider {

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ViewDataModel) {
			ViewDataModel vdm = (ViewDataModel) parentElement;

			return vdm.getChildren().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
