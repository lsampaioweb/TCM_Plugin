package net.thecodemaster.evd.marker.annotation;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class AnnotationInvisible {

	private Annotation	annotation;
	private Position		position;

	/**
	 * @param annotation
	 *          the annotation to add, may not be <code>null</code>
	 * @param position
	 *          the position describing the range covered by this annotation, may not be <code>null</code>
	 */
	public AnnotationInvisible(Annotation annotation, Position position) {
		this.setAnnotation(annotation);
		this.setPosition(position);
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public final void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public Position getPosition() {
		return position;
	}

	public final void setPosition(Position position) {
		this.position = position;
	}

}
