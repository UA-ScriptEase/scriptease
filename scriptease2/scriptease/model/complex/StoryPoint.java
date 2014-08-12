package scriptease.model.complex;

import java.util.HashSet;
import java.util.regex.Pattern;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.semodel.SEModelManager;
import scriptease.translator.io.model.GameType;
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
 * @author jyuen
 */
public class StoryPoint extends StoryNode {
	private static final int DEFAULT_FAN_IN = 1;
	private static final String NEW_STORY_POINT = "New Story Point";

	private int fanIn;

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
		this.successors = new HashSet<StoryNode>();
		this.parents = new HashSet<StoryNode>();
		this.uniqueID = this.getNextStoryNodeCounter();

		this.registerChildType(CauseIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(Note.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		if (name == null || name.equals("")) {
			name = NEW_STORY_POINT;
		}

		this.setDisplayText(name);
	}

	@Override
	public StoryPoint clone() {
		return (StoryPoint) super.clone();
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

		String name = nameTag + this.getUniqueID();
		// Handle Legal Values the type can take
		final String regex;
		final Pattern regexPattern;

		regex = SEModelManager.getInstance().getActiveModel()
				.getType(GameType.STORY_POINT_TYPE).getReg();

		regexPattern = Pattern.compile(regex);

		name = StringOp.removeIllegalCharacters(name, regexPattern, false);

		return name;
	}

	@Override
	public boolean equals(Object other) {
		return this.hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.getUniqueID();
	}

	@Override
	public boolean addStoryChild(StoryComponent newChild) {
		final boolean addChild = super.addStoryChild(newChild);

		if (addChild && newChild instanceof CauseIt) {
			for (StoryComponent child : ((CauseIt) newChild).getChildren()) {
				if (child instanceof KnowIt
						&& child.getDisplayText().equals("Is Active")) {
					final KnowIt description = (KnowIt) child;

					((ScriptIt) description.getBinding().getValue())
							.getParameter("Story Point").setBinding(this);
				}
			}
		}

		return addChild;
	}

	/**
	 * Checks if the story point is the root node by checking if it has no
	 * parents and if it has no owner component (I.e. it's not in a group) We
	 * can do this because the start node can't be in a group.
	 * 
	 * @return
	 */
	public boolean isRoot() {
		return this.getParents().isEmpty() && this.ownerComponent == null;
	}

}
