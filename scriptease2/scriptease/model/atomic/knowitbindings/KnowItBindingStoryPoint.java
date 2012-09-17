package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingVisitor;
import scriptease.model.complex.StoryPoint;

/**
 * This class represents a <b>Story Point</b> binding for a <code>KnowIt</code>.
 * 
 * @author remiller
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingStoryPoint extends KnowItBinding {
	private final StoryPoint point;

	/**
	 * Creates a new binding that wraps the given story point.
	 * 
	 * @param point
	 *            The story point to wrap.
	 */
	public KnowItBindingStoryPoint(StoryPoint point) {
		this.point = point;
	}

	/**
	 * Creates a new binding based on the given story point.
	 * 
	 * @param other
	 *            The binding to mimic.
	 */
	public KnowItBindingStoryPoint(KnowItBindingStoryPoint other) {
		this.point = other.point;
	}

	@Override
	public String getScriptValue() {
		return this.point.getDisplayText();
	}

	@Override
	public StoryPoint getValue() {
		return this.point;
	}

	@Override
	public Collection<String> getTypes() {
		List<String> types = new ArrayList<String>();
		types.add(StoryPoint.STORY_POINT_TYPE);
		return types;
	}

	@Override
	public String toString() {
		return this.getValue().getDisplayText();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingStoryPoint)
				&& ((KnowItBindingStoryPoint) other).point.equals(this.point);
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	@Override
	public KnowItBinding clone() {
		KnowItBindingStoryPoint clone = new KnowItBindingStoryPoint(this);

		return clone;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processStoryPoint(this);
	}
}
