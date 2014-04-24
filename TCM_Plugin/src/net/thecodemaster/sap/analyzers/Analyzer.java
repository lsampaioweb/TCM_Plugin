package net.thecodemaster.sap.analyzers;

import java.util.List;

import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Luciano Sampaio
 */
public abstract class Analyzer {

  protected List<Verifier> verifiers;

  public abstract boolean run(IResource resource, Reporter reporter) throws CoreException;

}
