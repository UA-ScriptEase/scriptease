package scriptease.model.complex.storygraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.semodel.SEModelManager;
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
	public static final String STORY_POINT_TYPE = "storyPoint";

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

		if (name.equals("") || name == null) {
			name = NEW_STORY_POINT;
		}

		this.setDisplayText(name);
	}

	@Override
	public StoryPoint clone() {
		return (StoryPoint) super.clone();
	}

	/**
	 * Clones the current story point with no successors and parents and retains
	 * the existing unique id. Unless you plan on deleting the existing
	 * StoryPoint, you should use the <code>clone</code> method or possibly run
	 * into many issues.
	 * 
	 * @return
	 */
	@Override
	public StoryNode shallowClone() {
		final StoryPoint component = this.clone();

		component.successors = new HashSet<StoryNode>();
		component.parents = new HashSet<StoryNode>();
		component.uniqueID = this.uniqueID;

		return component;
	}

	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		final boolean accepted = super.addStoryChildBefore(newChild, sibling);

		if (accepted && newChild instanceof CauseIt) {
			for (StoryComponent child : ((CauseIt) newChild).getChildren())
				if (child instanceof KnowIt
						&& child.getDisplayText().contains("Is Active")) {
					((KnowIt) child).getBinding().process(new BindingAdapter() {
						@Override
						public void processFunction(
								KnowItBindingFunction function) {
							for (KnowIt param : function.getValue()
									.getParameters()) {
								if (param.getBinding() instanceof KnowItBindingNull
										&& param.getTypes().contains(
												StoryPoint.STORY_POINT_TYPE)) {
									param.setBinding(StoryPoint.this);
								}
							}
						}
					});

				}
		}

		return accepted;
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
	 * Adds a successor to the StoryPoint.
	 * 
	 * @param successor
	 */
	@Override
	public boolean addSuccessor(StoryNode successor) {
		if (successor != this && !successor.getSuccessors().contains(this)) {
			if (this.successors.add(successor)) {
				successor.parents.add(this);

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
	@Override
	public void addSuccessors(Collection<StoryNode> successors) {
		for (StoryNode successor : successors) {
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
	@Override
	public boolean removeSuccessor(StoryNode successor) {
		if (this.successors.remove(successor)) {
			successor.parents.remove(this);

			this.notifyObservers(new StoryComponentEvent(successor,
					StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_REMOVED));
			return true;
		}
		return false;
	}

	@Override
	protected <E extends Collection<StoryNode>> E addDescendants(E descendants) {
		descendants.add(this);

		for (StoryNode successor : this.getSuccessors()) {
			/*
			 * This check prevents us from going over paths twice, which saves a
			 * ton of time in complex stories. Note that the contains
			 * implementation in Sets is much faster, which is why
			 * getDescendants is faster than getOrderedDescendants.
			 */
			if (!descendants.contains(successor))
				successor.addDescendants(descendants);
		}

		return descendants;
	}

	@Override
	public void setEnabled(Boolean isDisabled) {
		// Do nothing - don't want to be able to disable story points
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
		final String regex = SEModelManager.getInstance().getActiveModel()
				.getTypeRegex(StoryPoint.STORY_POINT_TYPE);
		final Pattern regexPattern = Pattern.compile(regex);
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

}
