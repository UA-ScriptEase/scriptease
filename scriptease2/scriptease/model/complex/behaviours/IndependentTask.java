package scriptease.model.complex.behaviours;

import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * A independent task is a subclass of Task with only one subject.
 * 
 * @author jyuen
 * 
 */
public class IndependentTask extends Task {

	/**
	 * Constructor. Creates a new independent task with the given name
	 * 
	 * @param name
	 */
	public IndependentTask(String name) {
		super(name);
		
		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
	}
}
