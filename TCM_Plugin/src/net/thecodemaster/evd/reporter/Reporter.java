package net.thecodemaster.evd.reporter;

import java.util.List;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.view.ViewSecurityVulnerabilities;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Luciano Sampaio
 */
public class Reporter {

	private IProgressMonitor	progressMonitor;
	private final boolean			problemView;
	private final boolean			textFile;
	private final boolean			xmlFile;

	public Reporter(boolean problemView, boolean textFile, boolean xmlFile) {
		this.problemView = problemView;
		this.textFile = textFile;
		this.xmlFile = xmlFile;
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public void clearOldProblems(List<IResource> resources) {
		for (IResource resource : resources) {
			clearOldProblems(resource);
		}
	}

	private void clearOldProblems(IResource resource) {
		if (problemView) {
			clearMarkers(resource);
		}
		if (textFile) {
			// TODO
		}
		if (xmlFile) {
			// TODO
		}
	}

	public void addProblem(int typeVulnerability, IResource resource, DataFlow df) {
		if (problemView) {
			addMarker(typeVulnerability, resource, df);
		}
		if (textFile) {
			// TODO
		}
		if (xmlFile) {
			// TODO
		}
	}

	private boolean clearMarkers(IResource resource) {
		try {
			resource.deleteMarkers(Constant.MARKER_ID, true, IResource.DEPTH_INFINITE);
			return true;
		} catch (CoreException e) {
			PluginLogger.logError(e);
			return false;
		}
	}

	private void addMarker(final int typeVulnerability, final IResource resource, final DataFlow df) {
		// Update the user interface asynchronously.
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				ViewSecurityVulnerabilities view = (ViewSecurityVulnerabilities) Activator.getDefault().findView(
						Constant.VIEW_ID);
				if (null == view) {
					view = createView();
				}

				view.add(typeVulnerability, resource, df);
			}
		});
	}

	private ViewSecurityVulnerabilities createView() {
		ViewSecurityVulnerabilities view = new ViewSecurityVulnerabilities();

		view = new ViewSecurityVulnerabilities();
		view.createPartControl(new Shell(Display.getDefault()));
		view.showView();

		return view;
	}

}
