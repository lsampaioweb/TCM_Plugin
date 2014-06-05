package net.thecodemaster.evd.marker.resolution;

import java.util.List;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.reporter.IReporter;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.reporter.ReporterView;
import net.thecodemaster.evd.ui.view.ViewDataModel;

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
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution2;

public abstract class AbstractResolution implements IMarkerResolution2 {

	private final int			position;
	// private final IMarker marker;
	private String				label;
	private String				description;
	private ReporterView	reporter;

	public AbstractResolution(int position, IMarker marker) {
		this.position = position;
		// this.marker = marker;
	}

	private int getPosition() {
		return position;
	}

	protected String getStrPosition() {
		return String.format("%02d - ", getPosition());
	}

	protected final void setLabel(String label) {
		this.label = label;
	}

	private String getLocalLabel() {
		return label;
	}

	@Override
	public String getLabel() {
		return getStrPosition() + getLocalLabel();
	}

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
		CompilationUnit cUnit = BindingResolver.getParentCompilationUnit(node);

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

}