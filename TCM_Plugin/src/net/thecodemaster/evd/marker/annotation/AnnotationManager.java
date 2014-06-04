package net.thecodemaster.evd.marker.annotation;

import java.util.Iterator;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * This class knows where and how to report the vulnerabilities.
 * 
 * @author Luciano Sampaio
 */
public class AnnotationManager {

	/**
	 * Returns the text file buffer managed for the file at the given location or null if there is no such text file
	 * buffer.
	 * 
	 * @param path
	 *          The path that will be used to retrieve the ITextFileBuffer.
	 * @return The text file buffer managed for the file at the given location or null if there is no such text file
	 *         buffer.
	 */
	private static ITextFileBuffer getTextFilebuffer(IPath path) {
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		return manager.getTextFileBuffer(path, LocationKind.IFILE);
	}

	private static IAnnotationModel getAnnotationModel(IPath path) {
		// 01 - Get the object that knows how to get the annotation model.
		ITextFileBuffer buffer = getTextFilebuffer(path);
		return (null != buffer) ? buffer.getAnnotationModel() : null;
	}

	private static IPath getPath(CompilationUnit cu) {
		return (null != cu) ? cu.getJavaElement().getPath() : null;
	}

	/**
	 * Add our invisible annotation into the source code.
	 * 
	 * @param marker
	 * @param buffer
	 */
	public static void addInvisibleAnnotation(ASTNode node) {
		IAnnotationModel model = getAnnotationModel(getPath(BindingResolver.getParentCompilationUnit(node)));
		if (null != model) {
			Annotation annotation = new Annotation(Constant.MARKER_ID_ANNOTATION_INVISIBLE, true, null);
			int offset = node.getStartPosition();
			int length = node.getLength();

			// 01 - Add our invisible annotation into the source code.
			Position position = new Position(offset, length);
			model.addAnnotation(annotation, position);
		}
	}

	public static boolean hasAnnotationAtPosition(ASTNode node) {
		IAnnotationModel model = getAnnotationModel(getPath(BindingResolver.getParentCompilationUnit(node)));
		if (null != model) {
			Iterator<?> iterator = model.getAnnotationIterator();

			int charStart = node.getStartPosition();
			int length = node.getLength();
			while (iterator.hasNext()) {
				Annotation annotation = (Annotation) iterator.next();
				if (annotation.getType().equals(Constant.MARKER_ID_ANNOTATION_INVISIBLE)) {
					Position position = model.getPosition(annotation);
					if (position.getOffset() == charStart && position.getLength() == length) {
						return true;
					}
				}
			}

		}
		return false;
	}

}
