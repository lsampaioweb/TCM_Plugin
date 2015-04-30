package net.thecodemaster.esvd.marker.resolution;

import java.util.List;

import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution2;

public class ResolutionManager {

	private final List<IMarkerResolution2>	resolutions;

	public ResolutionManager(ResolutionMessage resolutionMessage, IMarker marker) {
		resolutions = Creator.newList();
		getDefaultResolutions(resolutionMessage, marker);
	}

	private List<IMarkerResolution2> getDefaultResolutions(ResolutionMessage resolutionMessage, IMarker marker) {
		resolutions.add(new IgnoreResolution(resolutionMessage, marker));

		return resolutions;
	}

	protected List<IMarkerResolution2> getCommandInjectionResolutions() {
		resolutions.add(new WindowsEncodingResolution());
		resolutions.add(new UnixEncodingResolution());

		return resolutions;
	}

	protected List<IMarkerResolution2> getCookiePoisoningResolutions() {
		return resolutions;
	}

	protected List<IMarkerResolution2> getCrossSiteScriptingResolutions() {
		resolutions.add(new HTMLEncodingResolution());
		resolutions.add(new HTMLAttributeEncodingResolution());
		resolutions.add(new XMLEncodingResolution());
		resolutions.add(new XMLAttributeEncodingResolution());
		resolutions.add(new CSSEncodingResolution());
		resolutions.add(new JavaScriptEncodingResolution());
		resolutions.add(new VBScriptEncodingResolution());

		return resolutions;
	}

	protected List<IMarkerResolution2> getHTTPResponseSplittingResolutions() {
		return resolutions;
	}

	protected List<IMarkerResolution2> getLDAPInjectionResolutions() {
		resolutions.add(new LDAPEncodingResolution());
		resolutions.add(new DNEncodingResolution());

		return resolutions;
	}

	protected List<IMarkerResolution2> getLogForgingResolutions() {
		return resolutions;
	}

	protected List<IMarkerResolution2> getPathTraversalResolutions() {
		return resolutions;
	}

	protected List<IMarkerResolution2> getReflectionInjectionResolutions() {
		return resolutions;
	}

	protected List<IMarkerResolution2> getSecurityMisconfigurationResolutions() {
		return resolutions;
	}

	protected List<IMarkerResolution2> getSQLInjectionResolutions() {
		resolutions.add(new MySQLEncodingResolution());
		resolutions.add(new OracleEncodingResolution());
		resolutions.add(new DB2EncodingResolution());

		return resolutions;
	}

	protected List<IMarkerResolution2> getXPathInjectionResolutions() {
		resolutions.add(new XPathEncodingResolution());

		return resolutions;
	}
}