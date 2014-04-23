package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.verifiers.Verifier;

/**
 * @author Luciano Sampaio
 */
public class CookiePoisoningVerifier extends Verifier {

  public CookiePoisoningVerifier() {
    super(Messages.Plugin.COOKIE_POISONING_VERIFIER);
  }

  @Override
  protected void run() {
  }

}
