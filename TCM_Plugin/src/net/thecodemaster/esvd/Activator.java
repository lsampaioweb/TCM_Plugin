package net.thecodemaster.esvd;

import net.thecodemaster.esvd.constant.Constant;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-03-01
 * @Version: 01
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID.
	public static final String	PLUGIN_ID	= "tcm.early.vulnerability.detector"; //$NON-NLS-1$

	// The shared instance.
	private static Activator		plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		setDebugging(Constant.IS_DEBUGGING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *          the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (null != win) {
			return win;
		}

		return getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the view in this page with the specified id. There is at most one view in the page with the specified id.
	 * 
	 * @param: viewId the id of the view extension to use.
	 * @return: the view, or null if none is found.
	 */
	public IViewPart findView(final String viewId) {
		IWorkbenchWindow iWorkbenchWindow = getActiveWorkbenchWindow();
		if (null != iWorkbenchWindow) {
			IWorkbenchPage activePage = iWorkbenchWindow.getActivePage();
			if (null != activePage) {
				return activePage.findView(viewId);
			}
		}

		return null;
	}

	/**
	 * Shows a view in this page with the given id and secondary id. The behavior of this method varies based on the
	 * supplied mode. If VIEW_ACTIVATE is supplied, the view is given focus. If VIEW_VISIBLE is supplied, then it is made
	 * visible but not given focus. Finally, if VIEW_CREATE is supplied the view is created and will only be made visible
	 * if it is not created in a folder that already contains visible views. This allows multiple instances of a
	 * particular view to be created. They are disambiguated using the secondary id. If a secondary id is given, the view
	 * must allow multiple instances by having specified allowMultiple="true" in its extension.
	 * 
	 * @param viewId
	 *          viewId the id of the view extension to use.
	 * @param secondaryId
	 *          the secondary id to use, or null for no secondary id.
	 * @param viewVisible
	 *          the activation mode. Must be VIEW_ACTIVATE, VIEW_VISIBLE or VIEW_CREATE.
	 * @return: a view.
	 * @throws PartInitException
	 *           if the view could not be initialized.
	 */
	public IViewPart showView(String viewId, String secondaryId, int viewVisible) throws PartInitException {
		IWorkbenchWindow iWorkbenchWindow = getActiveWorkbenchWindow();
		if (null != iWorkbenchWindow) {
			IWorkbenchPage activePage = iWorkbenchWindow.getActivePage();
			if (null != activePage) {
				return activePage.showView(viewId, secondaryId, viewVisible);
			}
		}

		return null;
	}

}
