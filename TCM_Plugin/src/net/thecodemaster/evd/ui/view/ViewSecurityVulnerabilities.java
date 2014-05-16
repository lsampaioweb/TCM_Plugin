package net.thecodemaster.evd.ui.view;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;

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

	public void add(int typeVulnerability, IResource resource, DataFlow df) {
		try {
			Expression root = df.getRoot();
			List<List<DataFlow>> allVulnerablePaths = df.getAllVulnerablePaths();

			for (List<DataFlow> listVulnerablePaths : allVulnerablePaths) {
				Map<String, Object> markerAttributes = Creator.newMap();
				markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				markerAttributes.put(Constant.Marker.TYPE_SECURITY_VULNERABILITY, typeVulnerability);

				String fullPath = getFullPath(listVulnerablePaths);
				System.out.println(fullPath);

				int indexLastElement = listVulnerablePaths.size() - 1;
				DataFlow lastElement = listVulnerablePaths.get(indexLastElement);
				Expression expr = lastElement.getRoot();
				String message = lastElement.getMessage();
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

				// TODO
				viewer.setInput(null);
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	private String getFullPath(List<DataFlow> listVulnerablePaths) {
		StringBuilder fullPath = new StringBuilder();
		for (DataFlow vulnerablePath : listVulnerablePaths) {
			if (fullPath.length() != 0) {
				fullPath.append(" - ");
			}
			fullPath.append(vulnerablePath.getRoot().toString());
		}
		return fullPath.toString();
	}

	public void gotoMarker(IMarker marker) {
		TextEditor textEditor = new TextEditor();
		// setActivePage(1);
		((IGotoMarker) textEditor.getAdapter(IGotoMarker.class)).gotoMarker(marker);
	}

}
