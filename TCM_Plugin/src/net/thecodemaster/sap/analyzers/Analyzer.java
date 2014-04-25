package net.thecodemaster.sap.analyzers;

import java.util.List;

import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.core.resources.IResource;

/**
 * @author Luciano Sampaio
 */
public abstract class Analyzer {

  protected List<Verifier> verifiers;

  public Analyzer() {
    verifiers = Creator.newList();
  }

  public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
    for (Verifier verifier : verifiers) {
      verifier.run(resources, callGraph, reporter);
    }
  }

}
