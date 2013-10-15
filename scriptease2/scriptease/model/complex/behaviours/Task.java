package scriptease.model.complex.behaviours;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * A task is a series of effects. Each task has a probability of execution
 * depending on it's sibling tasks.
 * 
 * @author jyuen
 * 
 */
public abstract class Task extends ComplexStoryComponent {

	private Set<Task> successors;
	private Set<Task> parents;
	
	private double chance;

	/**
	 * Constructor. Creates a new task with the given name.
	 * 
	 * @param name
	 */
	protected Task(String name) {
		super(name);

		this.successors = new HashSet<Task>();
		this.parents = new HashSet<Task>();
		this.chance = 100;
	}

	/**
	 * Adds a successor to this task.
	 * 
	 * @param successor
	 */
	public boolean addSuccessor(Task successor) {

		// Prevent self assignment
		if (successor == this)
			return false;

		// Prevent adding duplicate successors
		if (this.successors.contains(this))
			return false;

		this.successors.add(successor);
		successor.parents.add(this);

		this.notifyObservers(new StoryComponentEvent(successor,
				StoryComponentChangeEnum.TASK_SUCCESSOR_ADDED));

		return true;
	}
	
	/**
	 * Adds a collection of successors to this task.
	 * 
	 * @param successors
	 */
	public void addSuccessors(Collection<Task> successors) {
		for (Task successor : successors) {
			this.addSuccessor(successor);
		}
	}

	/**
	 * Removes a successor from this task.
	 * 
	 * @param successor
	 */
	public boolean removeSuccessor(Task successor) {
		if (this.successors.remove(successor)) {
			successor.parents.remove(this);

			this.notifyObservers(new StoryComponentEvent(successor,
					StoryComponentChangeEnum.TASK_SUCCESSOR_REMOVED));
			return true;
		}

		return false;
	}
	
	// ************* GETTERS AND SETTERS ********************//

	/**
	 * @return the chance
	 */
	public Double getChance() {
		return chance;
	}

	/**
	 * @param chance
	 *            the chance to set
	 */
	public void setChance(double chance) {
		this.chance = chance;
	}

	/**
	 * @return the successors
	 */
	public Set<Task> getSuccessors() {
		return successors;
	}

	/**
	 * @param successors
	 *            the successors to set
	 */
	public void setSuccessors(Set<Task> successors) {
		this.successors = successors;
	}

	/**
	 * @return the parents
	 */
	public Set<Task> getParents() {
		return parents;
	}

	/**
	 * @param parents
	 *            the parents to set
	 */
	public void setParents(Set<Task> parents) {
		this.parents = parents;
	}
}
