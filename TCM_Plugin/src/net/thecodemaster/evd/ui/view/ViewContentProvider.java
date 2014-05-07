package net.thecodemaster.evd.ui.view;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in adapters
 * or simply return objects as-is. These objects may be sensitive to the current input of the view, or ignore it and
 * always show the same content (like Task List, for example).
 */
class ViewContentProvider implements IStructuredContentProvider {
	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object[] getElements(Object parent) {
		return new String[] { "One", "Two", "Three" };
	}
}
