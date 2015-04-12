package net.thecodemaster.esvd.marker.resolution;

import java.util.List;

import net.thecodemaster.esvd.esapi.ESAPIConfigurationJob;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.ui.view.ViewDataModel;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class EncodingResolution extends AbstractResolution {

	public EncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel("Testing new Resolution");
		setDescription("Testing new Resolution");
	}

	@Override
	public void run(IMarker marker) {
		try {
			List<ViewDataModel> vdms = getViewDataModelsFromMarker(marker);
			ViewDataModel vdm = vdms.get(0);
			Expression expression = vdm.getExpr();
			CompilationUnit cUnit = BindingResolver.getCompilationUnit(expression);

			insertEncodingImports(cUnit);
			
			IJavaProject javaProject = cUnit.getJavaElement().getJavaProject();
			IProject project = javaProject.getProject();
			
			ESAPIConfigurationJob job = new ESAPIConfigurationJob("ESAPI Configuration", project, javaProject);
			
			job.scheduleInteractive();
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}

	public void insertEncodingImports(CompilationUnit cUnit) {
		try {
			//FIXME REMOVE THIS CONSTANT AND RECEIVE THE IMPORT AS A PARAMETER
			final String ESAPI_IMPORT = "org.owasp.esapi.ESAPI";

			ImportRewrite fImportRewrite = ImportRewrite.create(cUnit, true);
			IEditorPart part = JavaUI.openInEditor(cUnit.getJavaElement(), true, true);
			if (part == null) {
				return;
			}
			IEditorInput input = part.getEditorInput();
			if (input == null) {
				return;
			}
			IDocument document = JavaUI.getDocumentProvider()
					.getDocument(input);

			TextEdit importEdits = null;
			fImportRewrite.addImport(ESAPI_IMPORT);
			importEdits = fImportRewrite.rewriteImports(null);
			importEdits.apply(document, TextEdit.CREATE_UNDO
					| TextEdit.UPDATE_REGIONS);
		} catch (MalformedTreeException | BadLocationException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
