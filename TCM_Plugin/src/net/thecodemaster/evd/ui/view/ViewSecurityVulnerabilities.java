package net.thecodemaster.evd.ui.view;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.ViewPart;

public class ViewSecurityVulnerabilities extends ViewPart {

	private TreeViewer	viewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		createColumns(viewer.getTree());

		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setSorter(new ViewSorter());
		viewer.setInput(getViewSite());
		viewer.expandAll();
	}

	/**
	 * Creates columns for the table
	 */
	private void createColumns(Tree tree) {
		String[] titles = { "Description", "Vulnerability", "Location", "Resource", "Path" };
		int[] bounds = { 400, 200, 100, 200, 200 };

		for (int i = 0; i < titles.length; i++) {
			TreeColumn column = new TreeColumn(tree, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(true);
		}
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void add() {
		viewer.setInput(null);
	}

	public void gotoMarker(IMarker marker) {
		TextEditor textEditor = new TextEditor();
		// setActivePage(1);
		((IGotoMarker) textEditor.getAdapter(IGotoMarker.class)).gotoMarker(marker);
	}

}
