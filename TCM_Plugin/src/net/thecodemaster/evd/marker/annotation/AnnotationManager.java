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

	// private static Map<IPath, List<InvisibleAnnotation>> invisibleAnnotationsPerFile;

	/**
	 * Default constructor.
	 */
	public AnnotationManager() {
		// invisibleAnnotationsPerFile = Creator.newMap();
	}

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

			// addToInternalList(getPath(cu), new InvisibleAnnotation(annotation, position));
		}
	}

	// public static boolean hasAnnotationAtPosition(ASTNode node,
	// Map<IPath, List<InvisibleAnnotation>> invisibleAnnotationsPerFile) {
	// // 01 - Get the Compilation Unit which this node belongs to.
	// IPath path = getPath(BindingResolver.getParentCompilationUnit(node));
	// if ((null != path) && (invisibleAnnotationsPerFile.size() > 0)) {
	// // 02 - Get the list of annotations in the current file.
	// List<InvisibleAnnotation> invisibleAnnotations = invisibleAnnotationsPerFile.get(path);
	//
	// if (null != invisibleAnnotations) {
	// int offset = node.getStartPosition();
	// int length = node.getLength();
	//
	// for (InvisibleAnnotation annotation : invisibleAnnotations) {
	// if (annotation.getAnnotation().getType().equals(Constant.MARKER_ID_ANNOTATION_INVISIBLE)) {
	// Position position = annotation.getPosition();
	// if (position.getOffset() == offset && position.getLength() == length) {
	// return true;
	// }
	// }
	// }
	// }
	//
	// }
	//
	// return false;
	//
	// }

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

	//
	// private void addToInternalList(IPath path, InvisibleAnnotation annotation) {
	// if (null != annotation) {
	// // 01 - Check if the current file is already in the list.
	// if (!invisibleAnnotationsPerFile.containsKey(path)) {
	// List<InvisibleAnnotation> invisibleAnnotations = Creator.newList();
	//
	// invisibleAnnotationsPerFile.put(path, invisibleAnnotations);
	// }
	//
	// // 02 - Get the list of annotations in the current file.
	// List<InvisibleAnnotation> invisibleAnnotations = invisibleAnnotationsPerFile.get(path);
	//
	// // 03 - Add the annotation to the list.
	// if (!invisibleAnnotations.contains(annotation)) {
	// invisibleAnnotations.add(annotation);
	// }
	// }
	// }

}
