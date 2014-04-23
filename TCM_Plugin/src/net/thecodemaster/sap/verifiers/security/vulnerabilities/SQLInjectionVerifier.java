package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.verifiers.Verifier;

/**
 * @author Luciano Sampaio
 */
public class SQLInjectionVerifier extends Verifier {

  public SQLInjectionVerifier() {
    super(Messages.Plugin.SQL_INJECTION_VERIFIER);
  }

  @Override
  protected void run() {
  }

}
