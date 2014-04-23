package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.verifiers.Verifier;

/**
 * @author Luciano Sampaio
 */
public class XSSVerifier extends Verifier {

  public XSSVerifier() {
    super(Messages.Plugin.XSS_VERIFIER);
  }

  @Override
  protected void run() {
  }

}
