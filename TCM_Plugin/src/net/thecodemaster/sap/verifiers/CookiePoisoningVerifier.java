package net.thecodemaster.sap.verifiers;

import net.thecodemaster.sap.exitpoints.ExitPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * @author Luciano Sampaio
 */
public class CookiePoisoningVerifier extends Verifier {

  public CookiePoisoningVerifier() {
    super(Messages.Plugin.COOKIE_POISONING_VERIFIER);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void run(IMethodBinding method, ExitPoint exitPoint) {
  }

}
