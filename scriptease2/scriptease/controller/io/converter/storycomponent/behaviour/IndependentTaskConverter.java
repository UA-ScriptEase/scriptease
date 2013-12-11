package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.IndependentTask;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Converts the IndependentTask {@link IndependentTask} to and from xml.
 * 
 * @author jyuen
 */
public class IndependentTaskConverter extends TaskConverter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(IndependentTask.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final IndependentTask task = new IndependentTask("");
		// remove the default generated initiator container.
		task.removeStoryChild(task.getInitiatorContainer());
		return task;
	}
}
