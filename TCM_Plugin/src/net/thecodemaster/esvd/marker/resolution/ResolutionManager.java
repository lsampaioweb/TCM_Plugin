package net.thecodemaster.esvd.marker.resolution;

import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.xmlloader.LoaderResolutionMessages;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution2;

public class ResolutionManager {

	private final List<IMarkerResolution2>					resolutions;
	private static Map<Integer, ResolutionMessage>	resolutionMessages;
	private static ResolutionMessage								resolutionMessage;
	private final int																typeVulnerability;
	private final IMarker														marker;

	public ResolutionManager(IMarker marker) {
		this.marker = marker;
		resolutions = Creator.newList();
		typeVulnerability = getTypeVulnerability(marker);
		resolutionMessage = getResolutionMessages().get(typeVulnerability);
	}

	private int getTypeVulnerability(IMarker marker) {
		return marker.getAttribute(Constant.Marker.TYPE_SECURITY_VULNERABILITY, 0);
	}

	private Map<Integer, ResolutionMessage> getResolutionMessages() {
		if (null == resolutionMessages) {
			resolutionMessages = (new LoaderResolutionMessages()).load();
		}

		return resolutionMessages;
	}

	protected List<IMarkerResolution2> getResolutions() {
		switch (typeVulnerability) {
			case Constant.VERIFIER_ID_COMMAND_INJECTION:
				resolutions.addAll(getCommandInjectionResolutions());
				break;
			case Constant.VERIFIER_ID_COOKIE_POISONING:
				resolutions.addAll(getCookiePoisoningResolutions());
				break;
			case Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING:
				resolutions.addAll(getCrossSiteScriptingResolutions());
				break;
			case Constant.VERIFIER_ID_HTTP_RESPONSE_SPLITTING:
				resolutions.addAll(getHTTPResponseSplittingResolutions());
				break;
			case Constant.VERIFIER_ID_LDAP_INJECTION:
				resolutions.addAll(getLDAPInjectionResolutions());
				break;
			case Constant.VERIFIER_ID_LOG_FORGING:
				resolutions.addAll(getLogForgingResolutions());
				break;
			case Constant.VERIFIER_ID_PATH_TRAVERSAL:
				resolutions.addAll(getPathTraversalResolutions());
				break;
			case Constant.VERIFIER_ID_REFLECTION_INJECTION:
				resolutions.addAll(getReflectionInjectionResolutions());
				break;
			case Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION:
				resolutions.addAll(getSecurityMisconfigurationResolutions());
				break;
			case Constant.VERIFIER_ID_SQL_INJECTION:
				resolutions.addAll(getSQLInjectionResolutions());
				break;
			case Constant.VERIFIER_ID_XPATH_INJECTION:
				resolutions.addAll(getXPathInjectionResolutions());
				break;
		}

		resolutions.add(new IgnoreResolution(resolutionMessage, marker));
		return resolutions;
	}

	private List<IMarkerResolution2> getCommandInjectionResolutions() {
		resolutions.add(new WindowsEncodingResolution());
		resolutions.add(new UnixEncodingResolution());

		return resolutions;
	}

	private List<IMarkerResolution2> getCookiePoisoningResolutions() {
		return resolutions;
	}

	private List<IMarkerResolution2> getCrossSiteScriptingResolutions() {
		resolutions.add(new HTMLEncodingResolution());
		resolutions.add(new HTMLAttributeEncodingResolution());
		resolutions.add(new XMLEncodingResolution());
		resolutions.add(new XMLAttributeEncodingResolution());
		resolutions.add(new CSSEncodingResolution());
		resolutions.add(new JavaScriptEncodingResolution());
		resolutions.add(new VBScriptEncodingResolution());

		return resolutions;
	}

	private List<IMarkerResolution2> getHTTPResponseSplittingResolutions() {
		return resolutions;
	}

	private List<IMarkerResolution2> getLDAPInjectionResolutions() {
		resolutions.add(new LDAPEncodingResolution());
		resolutions.add(new DNEncodingResolution());

		return resolutions;
	}

	private List<IMarkerResolution2> getLogForgingResolutions() {
		return resolutions;
	}

	private List<IMarkerResolution2> getPathTraversalResolutions() {
		return resolutions;
	}

	private List<IMarkerResolution2> getReflectionInjectionResolutions() {
		return resolutions;
	}

	private List<IMarkerResolution2> getSecurityMisconfigurationResolutions() {
		return resolutions;
	}

	private List<IMarkerResolution2> getSQLInjectionResolutions() {
		resolutions.add(new MySQLEncodingResolution());
		resolutions.add(new OracleEncodingResolution());
		resolutions.add(new DB2EncodingResolution());

		return resolutions;
	}

	private List<IMarkerResolution2> getXPathInjectionResolutions() {
		resolutions.add(new XPathEncodingResolution());

		return resolutions;
	}
}