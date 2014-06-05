package net.thecodemaster.evd.ui.view;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class ViewSecurityVulnerabilities extends ViewPart {

	private static TreeViewer	viewer;

	public void showView() {
		try {
			Activator.getDefault().showView(Constant.VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
			// Activator.getDefault().showView(Constant.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
		} catch (PartInitException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		ViewSorter sorter = new ViewSorter(viewer);
		createColumns(viewer.getTree(), sorter);

		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setContentProvider(new ViewContentProvider());

		viewer.setSorter(sorter);
		viewer.setInput(getViewSite());

		hookDoubleClick();
	}

	/**
	 * Creates columns for the table
	 */
	private void createColumns(Tree tree, ViewSorter sorter) {
		String[] titles = { Message.View.DESCRIPTION, Message.View.LOCATION, Message.View.VULNERABILITY,
				Message.View.RESOURCE, Message.View.PATH };
		int[] bounds = { 500, 55, 150, 150, 400 };

		for (int i = 0; i < titles.length; i++) {
			TreeColumn column = new TreeColumn(tree, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(true);
			sorter.addColumn(column);
		}
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		// hookListener(tree);
	}

	// private static void hookListener(Tree tree) {
	// Listener listener = new Listener() {
	// @Override
	// public void handleEvent(Event e) {
	// final TreeItem treeItem = (TreeItem) e.item;
	// // Update the user interface asynchronously.
	// Display.getDefault().asyncExec(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// if ((null != treeItem) && (!treeItem.isDisposed())) {
	// for (TreeColumn tc : treeItem.getParent().getColumns()) {
	// tc.pack();
	// }
	// }
	// } catch (Exception e) {
	// PluginLogger.logError(e);
	// }
	// }
	// });
	// }
	// };
	//
	// tree.addListener(SWT.Collapse, listener);
	// tree.addListener(SWT.Expand, listener);
	// }

	private void hookDoubleClick() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}

				ViewDataModel vdm = (ViewDataModel) selection.toList().get(0);
				gotoMarker(vdm.getMarker());
			}
		});
	}

	private void gotoMarker(IMarker marker) {
		final ITextEditor editor = getTextEditor(marker.getResource());

		if (null != editor) {
			final IGotoMarker gotoMarker = (IGotoMarker) editor.getAdapter(IGotoMarker.class);
			if (gotoMarker != null) {
				gotoMarker.gotoMarker(marker);
			}
		}
	}

	private static ITextEditor getTextEditor(IResource resource) {
		try {
			IWorkbenchPage page = Activator.getDefault().getActiveWorkbenchWindow().getActivePage();

			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(resource.getFullPath());
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
			IEditorPart activeEditor = page.openEditor(new FileEditorInput(file), desc.getId());

			return (ITextEditor) activeEditor.getAdapter(ITextEditor.class);
		} catch (PartInitException e) {
			PluginLogger.logError(e);
		}
		return null;
	}

	@Override
	public void setFocus() {
		if (null != viewer) {
			viewer.getControl().setFocus();
		}
	}

	public void addToView(final ViewDataModel rootVdm) {
		if (null == viewer) {
			showView();
		}

		viewer.setInput(rootVdm);
		updateTitle();
		// viewer.expandAll();
	}

	private void updateTitle() {
		int totalItems = viewer.getTree().getItems().length;

		String messageTemplate = "";
		if (totalItems == 1) {
			messageTemplate = Message.View.SINGLE_TOTAL_NUMBER_OF_VULNERABILITIES;
		} else {
			messageTemplate = Message.View.MULTIPLE_TOTAL_NUMBER_OF_VULNERABILITIES;
		}

		setContentDescription(String.format(messageTemplate, totalItems));
	}
}
