package net.thecodemaster.esvd.marker.resolution;

import java.util.List;

import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

public class ResolutionManager {

	private int	position	= 0;

	private int getPosition() {
		return ++position;
	}

	protected IMarkerResolution[] getCommandInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new WindowsEncodingResolution(getPosition()));
		resolutions.add(new UnixEncodingResolution(getPosition()));

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getCookiePoisoningResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getCrossSiteScriptingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new HTMLEncodingResolution(getPosition()));
		resolutions.add(new HTMLAttributeEncodingResolution(getPosition()));
		resolutions.add(new XMLEncodingResolution(getPosition()));
		resolutions.add(new XMLAttributeEncodingResolution(getPosition()));
		resolutions.add(new CSSEncodingResolution(getPosition()));
		resolutions.add(new JavaScriptEncodingResolution(getPosition()));
		resolutions.add(new VBScriptEncodingResolution(getPosition()));

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getHTTPResponseSplittingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getLDAPInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new LDAPEncodingResolution(getPosition()));
		resolutions.add(new DNEncodingResolution(getPosition()));

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
		resolutions.add(new MySQLEncodingResolution(getPosition()));
		resolutions.add(new OracleEncodingResolution(getPosition()));
		resolutions.add(new DB2EncodingResolution(getPosition()));

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	protected IMarkerResolution[] getXPathInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new XPathEncodingResolution(getPosition()));

		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}
}