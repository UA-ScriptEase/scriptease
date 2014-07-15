package scriptease.model.complex.behaviours;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.semodel.librarymodel.LibraryModel;

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
	protected Task(LibraryModel library, String name) {
		super(library, name);

		this.successors = new HashSet<Task>();
		this.parents = new HashSet<Task>();
		this.chance = 100;

		// Tasks don't need to have childrens - yet.
		this.registerChildTypes(
				new ArrayList<Class<? extends StoryComponent>>(), 0);
	}

	/**
	 * Adds a successor to this task.
	 * 
	 * @param successor
	 * 
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
	 * Adds descendants to a collection to return them.
	 * 
	 * @param descendants
	 * @return
	 */
	protected <E extends Collection<Task>> E addDescendants(E descendants) {
		descendants.add(this);

		for (Task successor : this.getSuccessors()) {
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
	 * Returns all descendant tasks in an unordered collection.
	 * 
	 * @return
	 */
	public Collection<Task> getDescendants() {
		return this.addDescendants(new HashSet<Task>());
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

	@Override
	public Task clone() {
		final Task component = (Task) super.clone();

		component.chance = this.chance;

		component.successors = new HashSet<Task>(this.successors.size());
		component.parents = new HashSet<Task>(this.parents.size());

		// clone the successors
		for (Task task : this.successors) {
			component.successors.add(task.clone());
		}

		// clone the parents
		for (Task task : this.parents) {
			component.parents.add(task.clone());
		}

		return component;
	}
}
