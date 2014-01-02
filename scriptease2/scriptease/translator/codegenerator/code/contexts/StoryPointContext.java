package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;

/**
 * Context representing a StoryPoint
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 * 
 */
public class StoryPointContext extends StoryNodeContext {

	/**
	 * Creates a new StoryPointContext with a previous context and the
	 * {@link StoryPoint} source.
	 * 
	 * @param other
	 * @param source
	 */
	public StoryPointContext(Context other, StoryPoint source) {
		super(other, source);
	}

	@Override
	public String getNameOf(StoryComponent component) {
		return this.getNamifier().getUniqueName(component, null);
	}

	@Override
	public String getUnique32CharName() {
		return this.getComponent().getUnique32CharName();
	}

	@Override
	public String getUniqueName(Pattern legalFormat) {
		return this.getNamifier().getUniqueName(this.getComponent(),
				legalFormat);
	}

	@Override
	public Collection<StoryPoint> getStoryPointParents() {
		final Collection<StoryPoint> parents;
		final StoryPoint storyPoint;

		parents = new ArrayList<StoryPoint>();
		storyPoint = this.getComponent();

		// Check if this node belongs to a group.
		final StoryComponent owner = this.getComponent().getOwner();
		if (owner instanceof StoryGroup) {
			StoryGroup group = (StoryGroup) owner;

			// Check if this node is the start node of a group.
			if (group.getStartNode() == storyPoint) {

				// Then we return the parents of this group if it is a
				// StoryPoint. If not, we return the first StoryPoint exit node
				// of the story group.

				// Must be cautious of groups within groups that are also the
				// start node because they will have no parents. Must get the
				// parents of the highest level group.
				while (group.getParents().isEmpty()
						&& group.getOwner() instanceof StoryGroup)
					group = (StoryGroup) group.getOwner();

				parents.addAll(this.getParents(group));
			} else {
				// This is some other node in the group that isn't the start
				// node.
				parents.addAll(this.getParents(storyPoint));
			}
		} else {
			// The node doesn't belong to a group. So let's just get the
			// parents.
			parents.addAll(this.getParents(storyPoint));
		}

		return parents;
	}

	/**
	 * Retrieves all the parents of @param node. If the parent is a StoryPoint
	 * it is just automatically added. If it is a group, we add the first
	 * instance of the exit node that is a StoryPoint.
	 * 
	 * @return
	 */
	private Collection<StoryPoint> getParents(StoryNode node) {
		final Collection<StoryPoint> parents = new ArrayList<StoryPoint>();

		for (StoryNode parent : node.getParents()) {
			if (parent instanceof StoryPoint) {
				parents.add((StoryPoint) parent);
			} else if (parent instanceof StoryGroup) {
				StoryNode exitNode = ((StoryGroup) parent).getExitNode();

				while (exitNode instanceof StoryGroup) {
					exitNode = ((StoryGroup) exitNode).getExitNode();
				}

				if (exitNode instanceof StoryPoint)
					parents.add((StoryPoint) exitNode);
			}
		}

		return parents;
	}

	@Override
	public Collection<StoryPoint> getStoryPointChildren() {
		final Collection<StoryPoint> successors;
		final StoryPoint storyPoint;

		successors = new ArrayList<StoryPoint>();
		storyPoint = this.getComponent();

		// Check if this node belongs to a group.
		final StoryComponent owner = this.getComponent().getOwner();
		if (owner instanceof StoryGroup) {
			StoryGroup group = (StoryGroup) owner;

			// Check if this node is the exit node of a group.
			if (group.getExitNode() == storyPoint) {

				// Then we return the successors of this group if it is a
				// StoryPoint. If not, we return the first StoryPoint start node
				// of the story group.

				// Must be cautious of groups within groups that are also the
				// exit node because they will have no successors. Must get the
				// successors of the highest level group.
				while (group.getSuccessors().isEmpty()
						&& group.getOwner() instanceof StoryGroup)
					group = (StoryGroup) group.getOwner();

				successors.addAll(this.getSuccessors(group));
			} else {
				// This is some other node in the group that isn't the exit
				// node.
				successors.addAll(this.getSuccessors(storyPoint));
			}
		} else {
			// The node doesn't belong to a group. So let's just get the
			// successors
			successors.addAll(this.getSuccessors(storyPoint));
		}

		return successors;
	}

	/**
	 * Retrieves all the successors of @param node. If the successor is a
	 * StoryPoint it is just automatically added. If it is a group, we add the
	 * first instance of the start node that is a StoryPoint.
	 * 
	 * @return
	 */
	private Collection<StoryPoint> getSuccessors(StoryNode node) {
		final Collection<StoryPoint> successors = new ArrayList<StoryPoint>();

		for (StoryNode successor : node.getSuccessors()) {
			if (successor instanceof StoryPoint) {
				successors.add((StoryPoint) successor);
			} else if (successor instanceof StoryGroup) {
				// Get the first instance of the start node of this StoryGroup
				// that is a StoryPoint.
				StoryNode startNode = ((StoryGroup) successor).getStartNode();

				while (startNode instanceof StoryGroup) {
					startNode = ((StoryGroup) startNode).getStartNode();
				}

				if (startNode instanceof StoryPoint)
					successors.add((StoryPoint) startNode);
			}
		}

		return successors;
	}

	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;

		typeFormat = this.getTranslator().getLibrary()
				.getType(GameType.STORY_POINT_TYPE).getFormat();
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	@Override
	public String getFanIn() {
		return this.getComponent().getFanIn().toString();
	}

	@Override
	public StoryPoint getComponent() {
		return (StoryPoint) super.getComponent();
	}
}
