package scriptease.model.complex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.Note;
import scriptease.util.StringOp;

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
	/**
	 * StoryPoints must be unique. This uniqueID helps maintain uniqueness. It
	 * only gets saved to the model, not written to any files. So it must get
	 * generated whenever we create a new StoryPoint.
	 */
	private final int uniqueID;
	private final Set<StoryPoint> successors;

	/**
	 * Creates a new Story Point with the given name and a default fan-in value.
	 * 
	 * @param name
	 *            If name is null or empty string, it gives a default name of
	 *            NEW_STORY_POINT and the current story point count.
	 */
	public StoryPoint(String name) {
		super();
		this.fanIn = DEFAULT_FAN_IN;
		this.successors = new HashSet<StoryPoint>();
		this.uniqueID = storyPointCounter;

		StoryPoint.storyPointCounter++;

		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(Note.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		if (name.equals("") || name == null) {
			name = NEW_STORY_POINT + " " + this.uniqueID;
		}

		this.setDisplayText(name);
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
		} else if (potentialChild instanceof Note) {
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
	public boolean addSuccessor(StoryPoint successor) {
		if (successor != this && !successor.getSuccessors().contains(this)) {
			if (this.successors.add(successor)) {
				this.notifyObservers(new StoryComponentEvent(successor,
						StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_ADDED));
				return true;
			}
		}
		return false;
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
	public boolean removeSuccessor(StoryPoint successor) {
		if (this.successors.remove(successor)) {
			this.notifyObservers(new StoryComponentEvent(successor,
					StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_REMOVED));
			return true;
		}
		return false;
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

	/**
	 * Returns a 32 character, lower case string that uses the unique id to
	 * generate a unique name for the story point.
	 * 
	 * @return
	 */
	public String getUnique32CharName() {
		if (this.uniqueID < 0) {
			throw new IllegalArgumentException("UniqueID (" + this.uniqueID
					+ ") for " + this + " cannot be less than 0.");
		}

		final int MAX_LEN = 32;

		final String noWhiteSpace;
		final String nameTag;

		noWhiteSpace = StringOp.removeWhiteSpace(this.getDisplayText());

		if (noWhiteSpace.length() > MAX_LEN / 2) {
			nameTag = noWhiteSpace.substring(0, MAX_LEN / 2).toLowerCase();
		} else
			nameTag = noWhiteSpace.toLowerCase();

		return nameTag + this.getUniqueID();
	}

	/**
	 * Returns the unique ID. Unique IDs are generated on ScriptEase startup and
	 * not saved to file, so remember not to implement code that requires unique
	 * IDs to be persistent across different saves.
	 * 
	 * @return
	 */
	public Integer getUniqueID() {
		return this.uniqueID;
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren())
			child.revalidateKnowItBindings();
	}

	@Override
	public boolean equals(Object other) {
		return this.hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.getUniqueID();
	}
}
