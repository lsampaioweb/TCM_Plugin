package net.thecodemaster.esvd.marker.resolution;

import java.util.ArrayList;
import java.util.List;

import net.thecodemaster.esvd.Activator;
import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.reporter.IReporter;
import net.thecodemaster.esvd.reporter.Reporter;
import net.thecodemaster.esvd.reporter.ReporterView;
import net.thecodemaster.esvd.ui.view.ViewDataModel;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution2;

public abstract class AbstractResolution implements IMarkerResolution2 {

	private String				description;
	private ReporterView	reporter;

	@Override
	public Image getImage() {
		return Activator.getImageDescriptor(Constant.Icons.SECURITY_VULNERABILITY_QUICK_FIX_OPTION).createImage();
	}

	protected final void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public ReporterView getReporter() {
		if (null == reporter) {
			reporter = (ReporterView) Reporter.getInstance().getReporter(IReporter.SECURITY_VIEW);
		}

		return reporter;
	}

	/**
	 * Get the ViewDataModel of this marker.
	 * 
	 * @param marker
	 *          The marker that will be used to retrieve the ViewDataModel.
	 * @return the ViewDataModel of this marker.
	 */
	protected List<ViewDataModel> getViewDataModelsFromMarker(IMarker marker) {
		return getReporter().getViewDataModels(marker);
	}

	protected void clearProblem(ViewDataModel vdm, boolean removeChildren) {
		getReporter().clearProblem(vdm, removeChildren);
	}

	protected void insertComment(ASTRewrite rewriter, ListRewrite listRewrite, ASTNode node, String comment) {
		Statement placeHolder = (Statement) rewriter.createStringPlaceholder(comment, ASTNode.EMPTY_STATEMENT);
		listRewrite.insertBefore(placeHolder, node, null);
	}

	protected void insertImport(CompilationUnit cUnit, IDocument document, String qualifiedTypeName)
			throws MalformedTreeException, BadLocationException, CoreException {

		List<String> listImport = new ArrayList<String>();
		listImport.add(qualifiedTypeName);
		insertImport(cUnit, document, listImport);
	}

	protected void insertImport(CompilationUnit cUnit, IDocument document, List<String> listQualifiedTypeName)
			throws MalformedTreeException, BadLocationException, CoreException {

		if (listQualifiedTypeName != null && !listQualifiedTypeName.isEmpty()) {
			ImportRewrite fImportRewrite = ImportRewrite.create(cUnit, true);

			for (String qualifiedTypeName : listQualifiedTypeName) {
				fImportRewrite.addImport(qualifiedTypeName);
			}

			TextEdit importEdits = null;
			importEdits = fImportRewrite.rewriteImports(null);
			importEdits.apply(document, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
		}
	}

	protected void applyChanges(CompilationUnit cUnit, ASTRewrite rewriter) {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath path = cUnit.getJavaElement().getPath();
		LocationKind locationKind = LocationKind.IFILE;
		try {
			bufferManager.connect(path, locationKind, null);
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, locationKind);
			IDocument document = textFileBuffer.getDocument();

			TextEdit edits = rewriter.rewriteAST(document, null);
			edits.apply(document);

			textFileBuffer.commit(null, false);
		} catch (CoreException | MalformedTreeException | BadLocationException e) {
			PluginLogger.logError(e);
		} finally {
			try {
				bufferManager.disconnect(path, locationKind, null);
			} catch (CoreException e) {
				PluginLogger.logError(e);
			}
		}
	}

	protected void runInsertComment(IMarker marker, String comment) {
		// 01 - Get the ViewDataModel of this marker.
		List<ViewDataModel> vdms = getViewDataModelsFromMarker(marker);

		ViewDataModel vdm = vdms.get(0);

		// 02 - The node that was clicked at.
		Expression node = vdm.getExpr();

		// 03 - Get the CompilationUnit from the root element.
		CompilationUnit cUnit = BindingResolver.getCompilationUnit(node);

		// 04 - Creates a new instance for describing manipulations of the given AST.
		ASTRewrite rewriter = ASTRewrite.create(cUnit.getAST());

		// 05 - Get the first parent block
		ASTNode parent = BindingResolver.getParentBlock(node);
		ListRewrite listRewrite = rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);

		// 06 - Get the first node that be receive some node before.
		ASTNode nodeToInsertBefore = BindingResolver.getFirstParentBeforeBlock(node);

		// 07 - Insert the comment.
		insertComment(rewriter, listRewrite, nodeToInsertBefore, comment);

		// 08 - Save the modifications into the source code file.
		applyChanges(cUnit, rewriter);
	}

	protected String getFullPath(ViewDataModel vdm) {
		if (0 >= getNrChildren(vdm)) {
			// 01 - EntryPoint.
			return vdm.getFullPath();
		} else {
			// 02 - ExitPoint.
			StringBuilder sb = new StringBuilder();
			for (ViewDataModel vdmChildren : vdm.getChildren()) {
				sb.append(vdmChildren.getFullPath());
				sb.append("<br/>");
			}
			return sb.toString();
		}
	}

	protected int getNrChildren(ViewDataModel vdm) {
		return (null != vdm) ? vdm.getChildren().size() : -1;
	}

}