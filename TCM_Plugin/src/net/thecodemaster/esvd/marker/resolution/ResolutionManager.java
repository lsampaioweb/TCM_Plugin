package net.thecodemaster.esvd.marker.resolution;

import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.xmlloader.LoaderResolutionMessages;

import org.eclipse.ui.IMarkerResolution2;

public class ResolutionManager {

	private int																			position	= 0;
	private static Map<Integer, ResolutionMessage>	resolutionMessages;

	private int getPosition() {
		return ++position;
	}

	private Map<Integer, ResolutionMessage> getResolutionMessages() {
		if (null == resolutionMessages) {
			resolutionMessages = (new LoaderResolutionMessages()).load();
		}

		return resolutionMessages;
	}

	protected List<IMarkerResolution2> getCommandInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new WindowsEncodingResolution(getPosition()));
		resolutions.add(new UnixEncodingResolution(getPosition()));

		return resolutions;
	}

	protected List<IMarkerResolution2> getCookiePoisoningResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions;
	}

	protected List<IMarkerResolution2> getCrossSiteScriptingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new HTMLEncodingResolution(getPosition()));
		resolutions.add(new HTMLAttributeEncodingResolution(getPosition()));
		resolutions.add(new XMLEncodingResolution(getPosition()));
		resolutions.add(new XMLAttributeEncodingResolution(getPosition()));
		resolutions.add(new CSSEncodingResolution(getPosition()));
		resolutions.add(new JavaScriptEncodingResolution(getPosition()));
		resolutions.add(new VBScriptEncodingResolution(getPosition()));

		return resolutions;
	}

	protected List<IMarkerResolution2> getHTTPResponseSplittingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions;
	}

	protected List<IMarkerResolution2> getLDAPInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new LDAPEncodingResolution(getPosition()));
		resolutions.add(new DNEncodingResolution(getPosition()));

		return resolutions;
	}

	protected List<IMarkerResolution2> getLogForgingResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions;
	}

	protected List<IMarkerResolution2> getPathTraversalResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions;
	}

	protected List<IMarkerResolution2> getReflectionInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions;
	}

	protected List<IMarkerResolution2> getSecurityMisconfigurationResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();

		return resolutions;
	}

	protected List<IMarkerResolution2> getSQLInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new MySQLEncodingResolution(getPosition()));
		resolutions.add(new OracleEncodingResolution(getPosition()));
		resolutions.add(new DB2EncodingResolution(getPosition()));

		return resolutions;
	}

	protected List<IMarkerResolution2> getXPathInjectionResolutions() {
		List<IMarkerResolution2> resolutions = Creator.newList();
		resolutions.add(new XPathEncodingResolution(getPosition()));

		return resolutions;
	}
}