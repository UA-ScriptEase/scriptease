package scriptease.model.complex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;

/**
 * StoryPoints are the basic units used to build stories. Each StoryPoint holds
 * the StoryComponents that dictate the behaviour of the game world in the
 * current state of the game.
 * 
 * StoryPoints also have a fanIn property, which is the number of parent
 * StoryPoints which must have succeeded before this point can become active.
 * 
 * @author mfchurch
 * @author graves
 * @author kschenk
 */
public class StoryPoint extends ComplexStoryComponent {
	public static String STORY_POINT_TYPE = "storyPoint";

	private static final int DEFAULT_FAN_IN = 1;
	private static final String NEW_STORY_POINT = "New Story Point";
	private static int storyPointCounter = 1;

	private int fanIn;
	private final Set<StoryPoint> successors;

	/**
	 * Creates a new Quest Point with the given name and a default fan-in value.
	 * 
	 * @param name
	 *            If name is null or empty string, it gives a default name of
	 *            NEW_QUEST_POINT and the current quest point count.
	 */
	public StoryPoint(String name) {
		super();
		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		if ((name.equals("")) || (name == null)) {
			name = NEW_STORY_POINT + " " + storyPointCounter++;
		}

		this.setDisplayText(name);
		this.fanIn = DEFAULT_FAN_IN;
		this.successors = new HashSet<StoryPoint>();
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
	 * Returns the Fan In for the StoryPoint.
	 * 
	 * @return
	 */
	public Integer getFanIn() {
		return this.fanIn;
	}

	/**
	 * Sets the Fan In for the StoryPoint.
	 * 
	 * @param fanIn
	 */
	public void setFanIn(Integer fanIn) {
		this.fanIn = fanIn;

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_FAN_IN));
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processStoryPoint(this);
	}

	@Override
	public String toString() {
		return "StoryPoint (\"" + this.getDisplayText() + "\")";
	}

	/**
	 * Gets the immediate successors of the StoryPoint.
	 * 
	 * @return
	 */
	public Collection<StoryPoint> getSuccessors() {
		return this.successors;
	}

	/**
	 * Adds a successor to the StoryPoint.
	 * 
	 * @param successor
	 */
	public void addSuccessor(StoryPoint successor) {
		if (successor != this && !successor.getSuccessors().contains(this)) {
			this.successors.add(successor);

			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_ADDED));
		}
	}

	/**
	 * Adds multiple successors to the StoryPoint.
	 * 
	 * @param successors
	 */
	public void addSuccessors(Collection<StoryPoint> successors) {
		for (StoryPoint successor : successors) {
			if (successor != this) {
				this.addSuccessor(successor);
			}
		}
	}

	/**
	 * Removes a successor from the StoryPoint.
	 * 
	 * @param successor
	 */
	public void removeSuccessor(StoryPoint successor) {
		this.successors.remove(successor);

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_REMOVED));
	}

	/**
	 * Gets all descendants of the StoryPoint, including the StoryPoint itself.
	 * That is, the successors, the successors of the successors, etc.
	 * 
	 * @return
	 */
	public Set<StoryPoint> getDescendants() {
		if (this.successors.contains(this)) {
			throw new IllegalStateException(
					"Story Point contains itself as a child!");
		}

		Set<StoryPoint> descendants;
		descendants = new HashSet<StoryPoint>();

		descendants.add(this);
		for (StoryPoint successor : this.successors) {
			descendants.add(successor);
			descendants.addAll(successor.getDescendants());
		}

		return descendants;
	}
}
