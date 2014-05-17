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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.ViewPart;

public class ViewSecurityVulnerabilities extends ViewPart {

	private static TreeViewer	viewer;

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
		String[] titles = { Message.View.DESCRIPTION, Message.View.VULNERABILITY, Message.View.LOCATION,
				Message.View.RESOURCE, Message.View.PATH };
		int[] bounds = { 450, 150, 55, 180, 1000 };

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
		viewer.getControl().setFocus();
	}

	public void add(int typeVulnerability, IResource resource, List<DataFlow> dataFlows) {
		ViewDataModel rootVdm = new ViewDataModel();

		for (DataFlow df : dataFlows) {
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

		viewer.add(viewer.getInput(), rootVdm.getChildren().toArray());
	}

	public void gotoMarker(IMarker marker) {
		TextEditor textEditor = new TextEditor();
		// setActivePage(1);
		((IGotoMarker) textEditor.getAdapter(IGotoMarker.class)).gotoMarker(marker);
	}

}
