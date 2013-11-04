package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.IndependentTask;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the IndependentTask {@link IndependentTask} to and from xml.
 * 
 * @author jyuen
 */
public class IndependentTaskConverter extends TaskConverter {

	public static final String TAG_INDEPENDENT_TASK = "IndependentTask";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		super.marshal(source, writer, context);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final IndependentTask independentTask = (IndependentTask) super
				.unmarshal(reader, context);

		return independentTask;
	}

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
