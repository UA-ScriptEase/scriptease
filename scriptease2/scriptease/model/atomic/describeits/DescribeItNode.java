package scriptease.model.atomic.describeits;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;

/**
 * 
 * @author kschenk
 * 
 */
public class DescribeItNode {
	private static final String NEW_DESCRIBEIT_NODE = "New Node";
	private static int describeItNodeCounter = 1;

	private KnowIt knowIt;
	private String name;

	private final Set<DescribeItNode> successors;

	public DescribeItNode(String name, KnowIt knowIt) {
		super();
		this.successors = new HashSet<DescribeItNode>();

		if (name.equals("") || name == null) {
			name = NEW_DESCRIBEIT_NODE + " " + describeItNodeCounter++;
		}

		this.setKnowIt(knowIt);
		this.setName(name);

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setKnowIt(KnowIt knowIt) {
		this.knowIt = knowIt;
	}

	public KnowIt getKnowIt() {
		return this.knowIt;
	}

	/**
	 * Gets the immediate successors of the StoryPoint.
	 * 
	 * @return
	 */
	public Collection<DescribeItNode> getSuccessors() {
		return this.successors;
	}

	/**
	 * Adds a successor to the StoryPoint.
	 * 
	 * @param successor
	 */
	public boolean addSuccessor(DescribeItNode successor) {
		if (successor != this && !successor.getSuccessors().contains(this)) {
			if (this.successors.add(successor)) {
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
	public void addSuccessors(Collection<DescribeItNode> successors) {
		for (DescribeItNode successor : successors) {
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
	public boolean removeSuccessor(DescribeItNode successor) {
		if (this.successors.remove(successor)) {
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
	public Set<DescribeItNode> getDescendants() {
		if (this.successors.contains(this)) {
			throw new IllegalStateException(
					"DescribeItNode contains itself as a child!");
		}

		Set<DescribeItNode> descendants;
		descendants = new HashSet<DescribeItNode>();

		descendants.add(this);
		for (DescribeItNode successor : this.successors) {
			descendants.add(successor);
			descendants.addAll(successor.getDescendants());
		}
		return descendants;
	}
}
