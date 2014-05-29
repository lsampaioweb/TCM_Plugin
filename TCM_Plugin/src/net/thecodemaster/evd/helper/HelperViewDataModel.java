package net.thecodemaster.evd.helper;

import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public abstract class HelperViewDataModel {

	public static String getMessageByNumberOfVulnerablePaths(String name, int nrOfChildren) {
		String messageTemplate = "";

		if (nrOfChildren > 1) {
			messageTemplate = Message.View.MULTIPLE_VULNERABILITIES;
		} else {
			messageTemplate = Message.View.SINGLE_VULNERABILITY;
		}

		return String.format(messageTemplate, name, nrOfChildren);
	}
}