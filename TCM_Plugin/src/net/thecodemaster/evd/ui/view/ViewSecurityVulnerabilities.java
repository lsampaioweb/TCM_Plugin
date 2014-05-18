package net.thecodemaster.evd.ui.view;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
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

	private static TreeViewer			viewer;
	private static ViewDataModel	rootVdm;

	public void showView() {
		try {
			Activator.getDefault().showView(Constant.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
		} catch (PartInitException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		rootVdm = new ViewDataModel();

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		ViewSorter sorter = new ViewSorter(viewer);
		createColumns(viewer.getTree(), sorter);

		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setContentProvider(new ViewContentProvider());

		viewer.setSorter(sorter);
		viewer.setInput(rootVdm);
		// viewer.expandAll();
		hookDoubleClick();
	}

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

	/**
	 * Creates columns for the table
	 */
	private void createColumns(Tree tree, ViewSorter sorter) {
		String[] titles = { Message.View.DESCRIPTION, Message.View.VULNERABILITY, Message.View.LOCATION,
				Message.View.RESOURCE, Message.View.PATH };
		int[] bounds = { 450, 150, 55, 180, 1000 };

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

	private String getFullPath(List<DataFlow> listVulnerablePaths) {
		String SEPARATOR = " - ";
		StringBuilder fullPath = new StringBuilder();
		for (DataFlow vulnerablePath : listVulnerablePaths) {
			if (fullPath.length() != 0) {
				fullPath.append(SEPARATOR);
			}
			fullPath.append(vulnerablePath.getRoot().toString());
		}
		return fullPath.toString();
	}

	@Override
	public void setFocus() {
		if (null != viewer) {
			viewer.getControl().setFocus();
		}
	}

	public void add(int typeVulnerability, IResource resource, DataFlow df) {
		ViewDataModel parent = null;
		ViewDataModel currentVdm;
		Expression root = df.getRoot();
		List<List<DataFlow>> allVulnerablePaths = df.getAllVulnerablePaths();

		if (allVulnerablePaths.size() > 1) {
			String message = String.format("%s has %d vulnerabilities.", root.toString(), allVulnerablePaths.size());

			parent = add(typeVulnerability, resource, root, message, null);
			if (null != parent) {
				rootVdm.addChildren(parent);
			}
		}

		for (List<DataFlow> vulnerablePaths : allVulnerablePaths) {
			// The last element is the element that have the vulnerability message.
			DataFlow lastElement = vulnerablePaths.get(vulnerablePaths.size() - 1);

			// The path that lead to the vulnerability.
			String fullPath = getFullPath(vulnerablePaths);

			currentVdm = add(typeVulnerability, resource, lastElement.getRoot(), lastElement.getMessage(), fullPath);
			if (null != currentVdm) {
				if (null != parent) {
					parent.addChildren(currentVdm);
				} else {
					rootVdm.addChildren(currentVdm);
				}
			}
		}

		addToView(rootVdm);
	}

	private ViewDataModel add(int typeVulnerability, IResource resource, Expression expr, String message, String fullPath) {
		try {
			Map<String, Object> markerAttributes = Creator.newMap();
			markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			markerAttributes.put(Constant.Marker.TYPE_SECURITY_VULNERABILITY, typeVulnerability);
			markerAttributes.put(IMarker.MESSAGE, message);

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.findParentCompilationUnit(expr);

			int startPosition = expr.getStartPosition();
			int endPosition = startPosition + expr.getLength();
			int lineNumber = cUnit.getLineNumber(startPosition);

			markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
			markerAttributes.put(IMarker.CHAR_START, startPosition);
			markerAttributes.put(IMarker.CHAR_END, endPosition);

			IMarker marker = resource.createMarker(Constant.MARKER_ID);
			marker.setAttributes(markerAttributes);

			ViewDataModel vdm = new ViewDataModel();
			vdm.setExpr(expr);
			vdm.setMessage(message);
			vdm.setTypeVulnerability(typeVulnerability);
			vdm.setLineNumber(lineNumber);
			vdm.setResource(resource);
			vdm.setFullPath(fullPath);
			vdm.setMarker(marker);

			return vdm;
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
		return null;
	}

	private void addToView(final ViewDataModel rootVdm) {
		if (null == viewer) {
			showView();
		}

		viewer.setInput(rootVdm);
		// viewer.add(viewer.getInput(), rootVdm.getChildren().toArray());
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
}
