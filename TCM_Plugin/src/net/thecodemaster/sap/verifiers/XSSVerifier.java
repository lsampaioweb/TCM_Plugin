package net.thecodemaster.sap.verifiers;

import java.util.List;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.points.ExitPoint;
import net.thecodemaster.sap.ui.l10n.Messages;

import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class XSSVerifier extends Verifier {

  static {
    // 01 - Loads all the ExitPoints of this verifier.
    loadExitPoints(Constants.Plugin.XSS_VERIFIER_ID);
  }

  public XSSVerifier(List<EntryPoint> entryPoints) {
    super(Messages.Plugin.XSS_VERIFIER_NAME, Constants.Plugin.XSS_VERIFIER_ID, entryPoints);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void run(Expression method, ExitPoint exitPoint) {

  }

}
