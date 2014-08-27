package net.thecodemaster.esvd.verifier.security;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio
 */
public class VerifierLDAPInjection extends Verifier {

	public VerifierLDAPInjection() {
		super(Constant.VERIFIER_ID_LDAP_INJECTION, Message.Plugin.VERIFIER_NAME_LDAP_INJECTION,
				Constant.VERIFIER_PRIORITY_LDAP_INJECTION);
	}

}
