package scriptease.model.complex.behaviours;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * A independent task is a subclass of Task with only one subject.
 * 
 * @author jyuen
 * 
 */
public class IndependentTask extends Task {

	private List<ScriptIt> effects;
	
	/**
	 * Constructor. Creates a new independent task with the given name
	 * 
	 * @param name
	 */
	public IndependentTask(String name) {
		super(name);
		
		this.effects = new ArrayList<ScriptIt>();
		
		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processIndependentTask(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}

	public List<ScriptIt> getEffects() {
		return effects;
	}
	
	public void setEffects(List<ScriptIt> effects) {
		this.effects = effects;
	}
	
	@Override
	public IndependentTask clone() {
		final IndependentTask component = (IndependentTask) super.clone();

		component.effects = new ArrayList<ScriptIt>(
				this.effects.size());

		// clone the effects
		for (ScriptIt effect : this.effects) {
			component.effects.add(effect.clone());
		}

		return component;
	}
}
