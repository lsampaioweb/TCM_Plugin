package net.thecodemaster.esvd.marker.resolution;

import java.util.List;

import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

public class ResolutionManager {

	protected IMarkerResolution[] getCommandInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getCookiePoisoningResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getCrossSiteScriptingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getHTTPResponseSplittingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getLDAPInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getLogForgingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getPathTraversalResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getReflectionInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getSecurityMisconfigurationResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getSQLInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getXPathInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}
}