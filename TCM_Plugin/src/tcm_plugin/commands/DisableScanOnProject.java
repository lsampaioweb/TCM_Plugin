package tcm_plugin.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * If the current selected project is being monitored, remove it from the list.
 * 
 * @author Luciano Sampaio
 */

public class DisableScanOnProject extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    return null;
  }

}
