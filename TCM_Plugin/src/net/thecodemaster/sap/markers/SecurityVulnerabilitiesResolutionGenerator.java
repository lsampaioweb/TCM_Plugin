package net.thecodemaster.sap.markers;

import java.util.ArrayList;
import java.util.List;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.loggers.PluginLogger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class SecurityVulnerabilitiesResolutionGenerator implements IMarkerResolutionGenerator2 {

  private int getTypeVulnerability(IMarker marker) {
    return marker.getAttribute(Constants.Marker.TYPE_SECURITY_VULNERABILITY, 0);
  }

  @Override
  public boolean hasResolutions(IMarker marker) {
    switch (getTypeVulnerability(marker)) {
      case Constants.Plugin.COOKIE_POISONING_VERIFIER_ID:
        return true;
      case Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID:
        return true;
      case Constants.Plugin.SQL_INJECTION_VERIFIER_ID:
        return true;
      case Constants.Plugin.XSS_VERIFIER_ID:
        return true;
      default:
        PluginLogger.logInfo("SecurityVulnerabilitiesResolutionGenerator Default Case");
        return false;
    }
  }

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    List<IMarkerResolution2> resolutions = new ArrayList<IMarkerResolution2>();

    switch (getTypeVulnerability(marker)) {
      case Constants.Plugin.COOKIE_POISONING_VERIFIER_ID:
        resolutions.add(new SecurityMisconfigurationResolution());
        break;
      case Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID:
        resolutions.add(new SecurityMisconfigurationResolution());
        break;
      case Constants.Plugin.SQL_INJECTION_VERIFIER_ID:
        resolutions.add(new SecurityMisconfigurationResolution());
        break;
      case Constants.Plugin.XSS_VERIFIER_ID:
        resolutions.add(new SecurityMisconfigurationResolution());
        break;
      default:
        PluginLogger.logInfo("getResolutions Default Case");
    }

    return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
  }

}
