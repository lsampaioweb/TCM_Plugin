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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class HTMLEncodingResolution extends AbstractResolution {

	private final String	esapiEncoderMethodName	= "encodeForHTML";

	public HTMLEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	@Override
	public void run(IMarker marker) {
		try {
			List<ViewDataModel> vdms = getViewDataModelsFromMarker(marker);
			ViewDataModel vdm = vdms.get(0);
			Expression expression = vdm.getExpr();
			CompilationUnit cUnit = BindingResolver.getCompilationUnit(expression);

			int offset = marker.getAttribute(IMarker.CHAR_START, -1);
			int length = marker.getAttribute(IMarker.CHAR_END, -1) - offset;

			IEditorPart part = JavaUI.openInEditor(cUnit.getJavaElement(), true, true);
			if (part == null) {
				return;
			}
			IEditorInput input = part.getEditorInput();
			if (input == null) {
				return;
			}
			IDocument document = JavaUI.getDocumentProvider().getDocument(input);

			generateEncoding(cUnit, document, offset, length);
			insertEncodingImports(cUnit, document);
			runInsertComment(marker, "//TEST COMMENT");

			IJavaProject javaProject = cUnit.getJavaElement().getJavaProject();
			IProject project = javaProject.getProject();

			ESAPIConfigurationJob job = new ESAPIConfigurationJob("ESAPI Configuration", project, javaProject);

			job.scheduleInteractive();
		} catch (MalformedTreeException | BadLocationException | CoreException e) {
			PluginLogger.logError(e);
		}
	}

	private void generateEncoding(CompilationUnit cUnit, IDocument document, int offset, int length)
			throws MalformedTreeException, BadLocationException, JavaModelException, IllegalArgumentException {
		// FIXME REMOVE THIS CONSTANTS FROM HERE
		final String ESAPI = "ESAPI";
		final String ESAPI_ENCODER = "encoder";

		ASTNode node = NodeFinder.perform(cUnit, offset, length);
		if (!(node instanceof Expression)) {
			return;
		}

		MethodDeclaration declaration = BindingResolver.getParentMethodDeclaration(node);
		if (declaration == null) {
			return;
		}

		Block body = declaration.getBody();
		AST ast = body.getAST();

		ASTRewrite astRewrite = ASTRewrite.create(ast);
		TextEdit textEdits = null;

		MethodInvocation replacement = ast.newMethodInvocation();
		MethodInvocation expression = ast.newMethodInvocation();
		expression.setExpression(ast.newSimpleName(ESAPI));
		expression.setName(ast.newSimpleName(ESAPI_ENCODER));
		replacement.setExpression(expression);
		replacement.setName(ast.newSimpleName(esapiEncoderMethodName));

		astRewrite.replace(node, replacement, null);

		textEdits = astRewrite.rewriteAST();

		textEdits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);

		// TODO CHECK IF THIS IS NECESSARY
		ITrackedNodePosition replacementPositionTracking = astRewrite.track(replacement);
	}

	private void insertEncodingImports(CompilationUnit cUnit, IDocument document) throws MalformedTreeException,
			BadLocationException, CoreException {
		// FIXME REMOVE THIS CONSTANT AND RECEIVE THE IMPORT AS A PARAMETER
		final String ESAPI_IMPORT = "org.owasp.esapi.ESAPI";

		ImportRewrite fImportRewrite = ImportRewrite.create(cUnit, true);
		fImportRewrite.addImport(ESAPI_IMPORT);

		TextEdit importEdits = null;
		importEdits = fImportRewrite.rewriteImports(null);
		importEdits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
	}

	private String generateLabel() {
		return "HTML Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		description = "Use HTML Encoding when the data you get from an outside source is to be written directly into your HTML body.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}
}
