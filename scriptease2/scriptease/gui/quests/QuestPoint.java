package scriptease.gui.quests;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * QuestPoints are the basic units used to build stories. Each QuestPoint holds
 * the StoryComponents that dictate the behaviour of the game world in the
 * current state of the game.
 * 
 * QuestPoints also have a fanIn property, which is the number of parent
 * QuestPoints which must have succeeded before this point can become active.
 * 
 * @author mfchurch
 * @author graves
 */
public class QuestPoint extends ComplexStoryComponent {
	public static String QUEST_POINT_TYPE = "questPoint";

	private static final int DEFAULT_FAN_IN = 1;
	private static final String NEW_QUEST_POINT = "New Quest Point";
	private static int questPointCounter = 1;

	private int fanIn;
	private final Set<QuestPoint> successors;

	/**
	 * Creates a new Quest Point with the given name and a default fan-in value.
	 * 
	 * @param name
	 *            The name for this quest point.
	 */
	public QuestPoint(String name) {
		this(name, QuestPoint.DEFAULT_FAN_IN);
	}

	/**
	 * Creates a new QuestPoint.
	 * 
	 * @param name
	 *            If name is null or empty string, it gives a default name of
	 *            NEW_QUEST_POINT and the current quest point count.
	 * @param fanIn
	 *            The fan-in value to use.
	 */
	public QuestPoint(String name, int fanIn) {
		super();
		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		if ((name.equals("")) || (name == null)) {
			name = NEW_QUEST_POINT + " " + questPointCounter++;
		}

		this.setDisplayText(name);
		this.fanIn = fanIn;
		this.successors = new HashSet<QuestPoint>();
	}

	/**
	 * Only accepts Causes, not effects, as children.
	 */
	@Override
	public boolean canAcceptChild(StoryComponent potentialChild) {
		// Only accept causes, not effects
		if (potentialChild instanceof ScriptIt) {
			if (((ScriptIt) potentialChild).isCause())
				return super.canAcceptChild(potentialChild);
		}
		return false;
	}

	/**
	 * Returns the Fan In for the QuestPoint.
	 * 
	 * @return
	 */
	public Integer getFanIn() {
		return this.fanIn;
	}

	/**
	 * Sets the Fan In for the QuestPoint.
	 * 
	 * @param fanIn
	 */
	public void setFanIn(Integer fanIn) {
		this.fanIn = fanIn;
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processQuestPoint(this);
	}

	@Override
	public String toString() {
		return "QuestPoint (\"" + this.getDisplayText() + "\")";
	}

	/**
	 * Gets the immediate successors of the QuestPoint.
	 * 
	 * @return
	 */
	public Collection<QuestPoint> getSuccessors() {
		return this.successors;
	}

	/**
	 * Adds a successor to the QuestPoint.
	 * 
	 * @param successor
	 */
	public void addSuccessor(QuestPoint successor) {
		this.successors.add(successor);
	}

	/**
	 * Removes a successor from the QuestPoint.
	 * 
	 * @param successor
	 */
	public void removeSuccessor(QuestPoint successor) {
		this.successors.remove(successor);
	}

	/**
	 * Gets all descendants of the QuestPoint, including the Quest Point itself.
	 * That is, the successors, the successors of the successors, etc.
	 * 
	 * @return
	 */
	public Set<QuestPoint> getDescendants() {
		Set<QuestPoint> descendants;
		descendants = new HashSet<QuestPoint>();

		descendants.add(this);
		for (QuestPoint successor : this.successors) {
			descendants.add(successor);
			descendants.addAll(successor.getDescendants());
		}

		return descendants;
	}
}
