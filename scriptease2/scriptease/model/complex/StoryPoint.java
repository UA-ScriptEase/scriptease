package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	private int uniqueID;
	private Set<StoryPoint> successors;
	private Set<StoryPoint> parents;

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
		this.parents = new HashSet<StoryPoint>();
		this.uniqueID = storyPointCounter;

		StoryPoint.storyPointCounter++;

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
		final StoryPoint component = (StoryPoint) super.clone();

		return component;
	}

	/**
	 * Clones the current story point wit no successors and parents and retains
	 * the existing unique id. Unless you plan on deleting the existing
	 * StoryPoint, you should use the <code>clone</code> method or possibly run
	 * into many issues.
	 * 
	 * @return
	 */
	public StoryPoint shallowClone() {
		final StoryPoint component = this.clone();

		component.successors = new HashSet<StoryPoint>();
		component.parents = new HashSet<StoryPoint>();
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
	 * Only accepts Causes, not effects, as children.
	 */
	@Override
	public boolean canAcceptChild(StoryComponent potentialChild) {
		// Only accept causes, not effects
		if (potentialChild instanceof CauseIt) {
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
	 * Gets the immediate parents of the StoryPoint.
	 * 
	 * @return
	 */
	public Collection<StoryPoint> getParents() {
		return this.parents;
	}

	/**
	 * Adds a successor to the StoryPoint.
	 * 
	 * @param successor
	 */
	public boolean addSuccessor(StoryPoint successor) {
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
			successor.parents.remove(this);

			this.notifyObservers(new StoryComponentEvent(successor,
					StoryComponentChangeEnum.STORY_POINT_SUCCESSOR_REMOVED));
			return true;
		}
		return false;
	}

	/**
	 * Gets all descendants of the StoryPoint in an unordered set, including the
	 * StoryPoint itself. That is, the successors, the successors of the
	 * successors, etc. This is computationally expensive, and should thus be
	 * used carefully. Try to cache the descendants somewhere if they need to be
	 * accessed more than once. If order matters, use
	 * {@link #getOrderedDescendants()}.
	 * 
	 * @see #getOrderedDescendants()
	 * @return An unordered set of the story point's descendants.
	 */
	public Set<StoryPoint> getDescendants() {
		return this.addDescendants(new HashSet<StoryPoint>());
	}

	/**
	 * Gets all descendants of the StoryPoint, including the StoryPoint itself.
	 * This is much slower than {@link #getDescendants()}, so it should be used
	 * sparingly.
	 * 
	 * @see #getDescendants()
	 * @return An ordered list of the story point's descendants.
	 */
	public List<StoryPoint> getOrderedDescendants() {
		return this.addDescendants(new ArrayList<StoryPoint>());
	}

	/**
	 * Adds the descendants to the passed in collection. You should use
	 * {@link #getDescendants()} or {@link #getOrderedDescendants()} instead of
	 * this method. Having this method separate lets us keep our descendant
	 * collecting code in one method for different types of collections. It also
	 * lets us avoid going through the graph multiple times in cases where we
	 * branch around.
	 * 
	 * @see #getDescendants()
	 * @see #getOrderedDescendants()
	 * @param descendants
	 *            The collection to add to and to return.
	 * @return The same collection that was passed in, but with descendants
	 *         added.
	 */
	private <E extends Collection<StoryPoint>> E addDescendants(E descendants) {
		descendants.add(this);

		for (StoryPoint successor : this.getSuccessors()) {
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

	/**
	 * Gets a mapping of the depth of each StoryPoint. The depth corresponds to
	 * the longest path it will take to get to the Story Point. This is very
	 * computationally expensive, so it should not be used too often.
	 * 
	 * @return
	 */
	public final Map<StoryPoint, Integer> createDepthMap() {
		final Map<StoryPoint, Integer> depthMap = new IdentityHashMap<StoryPoint, Integer>();

		// Goes through every child of the node
		for (StoryPoint child : this.getSuccessors()) {
			// Gets the depth map of every child
			final Map<StoryPoint, Integer> childDepthMap = child
					.createDepthMap();

			for (Entry<StoryPoint, Integer> entry : childDepthMap.entrySet()) {
				final StoryPoint childNode = entry.getKey();
				final Integer depth = entry.getValue() + 1;

				// If the node is already in the depthMap and the new depth is
				// greater, use the greater depth value.
				if (depthMap.containsKey(childNode)) {
					if (depth > depthMap.get(childNode))
						depthMap.put(childNode, depth);
				} else
					depthMap.put(childNode, depth);
			}
		}

		if (!depthMap.containsKey(this))
			depthMap.put(this, 0);

		return depthMap;
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
