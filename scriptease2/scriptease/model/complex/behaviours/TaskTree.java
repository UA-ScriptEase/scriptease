package scriptease.model.complex.behaviours;

import scriptease.model.complex.ComplexStoryComponent;

/**
 * A TaskTree is a series of tasks.
 * 
 * @author jyuen
 */
public abstract class TaskTree extends ComplexStoryComponent {

	private Task startTask;
	
	public TaskTree(String name, Task startTask) {
		super(name);
		this.setStartTask(startTask);

		this.registerChildType(Task.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
	}

	/**
	 * @return the startTask
	 */
	public Task getStartTask() {
		return startTask;
	}

	/**
	 * @param startTask the startTask to set
	 */
	public void setStartTask(Task startTask) {
		this.startTask = startTask;
	}
}
