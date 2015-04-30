package net.thecodemaster.esvd.marker.resolution;

import java.util.List;

import net.thecodemaster.esvd.esapi.EsapiDependencyConfigurationJob;
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
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public abstract class AbstractEncodingResolution extends AbstractResolution {

	// FIXME REMOVE THIS CONSTANTS FROM HERE
	private static final String	ESAPI_IMPORT	= "org.owasp.esapi.ESAPI";
	private static final String	ESAPI					= "ESAPI";
	private static final String	ESAPI_ENCODER	= "encoder";

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
			IEditorInput input = part.getEditorInput();
			IDocument document = JavaUI.getDocumentProvider().getDocument(input);

			generateEncoding(cUnit, document, offset, length);
			insertImport(cUnit, document, ESAPI_IMPORT);

			IJavaProject javaProject = cUnit.getJavaElement().getJavaProject();
			IProject project = javaProject.getProject();

			EsapiDependencyConfigurationJob job = new EsapiDependencyConfigurationJob("ESAPI Configuration", project,
					javaProject);

			job.scheduleInteractive();
		} catch (MalformedTreeException | BadLocationException | CoreException e) {
			PluginLogger.logError(e);
		}
	}

	protected void generateEncoding(CompilationUnit cUnit, IDocument document, int offset, int length)
			throws MalformedTreeException, BadLocationException, JavaModelException, IllegalArgumentException {

		ASTNode node = NodeFinder.perform(cUnit, offset, length);
		MethodDeclaration declaration = BindingResolver.getParentMethodDeclaration(node);
		Block body = declaration.getBody();
		AST ast = body.getAST();

		MethodInvocation expression = ast.newMethodInvocation();
		expression.setExpression(ast.newSimpleName(ESAPI));
		expression.setName(ast.newSimpleName(ESAPI_ENCODER));

		MethodInvocation replacement = ast.newMethodInvocation();
		replacement.setExpression(expression);
		replacement.setName(ast.newSimpleName(getEsapiEncoderMethodName()));

		ASTRewrite astRewrite = ASTRewrite.create(ast);

		Expression copyOfCoveredNode = (Expression) astRewrite.createCopyTarget(node);
		List<Expression> args = replacement.arguments();
		args.add(0, copyOfCoveredNode);

		astRewrite.replace(node, replacement, null);

		TextEdit textEdits = astRewrite.rewriteAST();
		textEdits.apply(document, TextEdit.UPDATE_REGIONS);

		// TODO CHECK IF THIS IS NECESSARY
		ITrackedNodePosition replacementPositionTracking = astRewrite.track(replacement);
	}

	protected abstract String getEsapiEncoderMethodName();
}
