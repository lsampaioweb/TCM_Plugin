package net.thecodemaster.sap.analyzers;

import java.util.List;

import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.UtilProjects;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Luciano Sampaio
 */
public abstract class Analyzer {

  protected List<Verifier>      verifiers;
  protected static List<String> resourceTypes;

  /**
   * Check if the detection should be performed in this resource or not.
   * 
   * @param resource The resource that will be tested.
   * @return True if the detection should be performed in this resource, otherwise false.
   */
  protected boolean isToPerformDetection(IResource resource) {
    if (resource instanceof IFile) {
      if (null == resourceTypes) {
        resourceTypes = getResourceTypesToPerformDetection();
      }

      for (String resourceType : resourceTypes) {
        if (resource.getFileExtension().equalsIgnoreCase(resourceType)) {
          return true;
        }
      }
    }
    // If it reaches this point, it means that the detection should not be performed in this resource.
    return false;
  }

  protected List<String> getResourceTypesToPerformDetection() {
    return UtilProjects.getResourceTypesToPerformDetection();
  }

  /**
   * Reads a ICompilationUnit and creates the AST DOM for manipulating the Java source file.
   * 
   * @param unit
   * @return A compilation unit.
   */
  protected CompilationUnit parse(ICompilationUnit unit) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null); // Parse.
  }

  public abstract boolean run(IResource resource, Reporter reporter) throws CoreException;

}
