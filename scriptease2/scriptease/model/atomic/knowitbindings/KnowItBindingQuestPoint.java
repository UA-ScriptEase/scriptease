package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingVisitor;
import scriptease.gui.quests.QuestPoint;

/**
 * This class represents a <b>Quest Point</b> binding for a <code>KnowIt</code>.
 * 
 * @author remiller
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingQuestPoint extends KnowItBinding {
	private final QuestPoint point;

	/**
	 * Creates a new binding that wraps the given quest point.
	 * 
	 * @param point
	 *            The quest point to wrap.
	 */
	public KnowItBindingQuestPoint(QuestPoint point) {
		this.point = point;
	}

	/**
	 * Creates a new binding based on the given quest point.
	 * 
	 * @param other
	 *            The binding to mimic.
	 */
	public KnowItBindingQuestPoint(KnowItBindingQuestPoint other) {
		this.point = other.point;
	}

	@Override
	public String getScriptValue() {
		return this.point.getDisplayText();
	}

	@Override
	public QuestPoint getValue() {
		return this.point;
	}

	@Override
	public Collection<String> getTypes() {
		List<String> types = new ArrayList<String>();
		types.add(QuestPoint.QUEST_POINT_TYPE);
		return types;
	}

	@Override
	public String toString() {
		return this.getValue().getDisplayText();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingQuestPoint)
				&& ((KnowItBindingQuestPoint) other).point.equals(this.point);
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	@Override
	public KnowItBinding clone() {
		KnowItBindingQuestPoint clone = new KnowItBindingQuestPoint(this);

		return clone;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processQuestPoint(this);
	}
}
