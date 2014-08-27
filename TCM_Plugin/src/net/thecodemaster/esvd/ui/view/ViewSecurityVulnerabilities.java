package net.thecodemaster.esvd.ui.view;

import net.thecodemaster.esvd.Activator;
import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
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

	private static TreeViewer				viewer;
	private CopyToClipBoardHandler	copyToClipBoardHandler;

	public void showView() {
		try {
			Activator.getDefault().showView(Constant.VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
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

		copyToClipBoardHandler = new CopyToClipBoardHandler(parent, this, viewer);

		hookDoubleClick();
		hookSelectionListener();
		hookContextMenu();
	}

	/**
	 * Creates columns for the table
	 */
	private void createColumns(Tree tree, ViewSorter sorter) {
		String[] titles = { Message.View.DESCRIPTION, Message.View.PRIORITY, Message.View.LINE, Message.View.VULNERABILITY,
				Message.View.RESOURCE, Message.View.PATH };
		int[] bounds = { 500, 60, 40, 150, 190, 550 };

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
	}

	private void hookDoubleClick() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if ((selection == null) || (selection.isEmpty())) {
					return;
				}

				ViewDataModel vdm = (ViewDataModel) selection.toList().get(0);
				gotoMarker(vdm.getMarker());
			}
		});
	}

	private void hookSelectionListener() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if ((selection != null) && (!selection.isEmpty()) && (selection instanceof TreeSelection)) {
					TreeSelection treeSelection = (TreeSelection) selection;
					int size = treeSelection.getPaths().length;
					if (size > 0) {
						IStatusLineManager slManager = getViewSite().getActionBars().getStatusLineManager();

						slManager.setMessage(getMessageSelectionListener(size));
					}
				}
			}

			private String getMessageSelectionListener(int totalItems) {
				String messageTemplate = "";
				if (totalItems == 1) {
					messageTemplate = Message.View.SINGLE_SELECTION;
				} else {
					messageTemplate = Message.View.MULTIPLE_SELECTION;
				}

				return String.format(messageTemplate, totalItems);
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(copyToClipBoardHandler);
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
