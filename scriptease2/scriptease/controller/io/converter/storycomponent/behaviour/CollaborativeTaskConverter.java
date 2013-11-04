package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.CollaborativeTask;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the CollaborativeTask {@link CollaborativeTask} to and from xml.
 * 
 * @author jyuen
 */
public class CollaborativeTaskConverter extends TaskConverter {

	public static final String TAG_COLLABORATIVE_TASK = "CollaborativeTask";

	public static final String ATTRIBUTE_INITIATE = "Initiate";
	public static final String ATTRIBUTE_RESPOND = "Respond";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final CollaborativeTask collaborativeTask = (CollaborativeTask) source;
		super.marshal(source, writer, context);

		// initiate name
		writer.addAttribute(ATTRIBUTE_INITIATE,
				collaborativeTask.getInitiatorName());

		// responder name
		writer.addAttribute(ATTRIBUTE_RESPOND,
				collaborativeTask.getResponderName());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final CollaborativeTask collaborativeTask = (CollaborativeTask) super
				.unmarshal(reader, context);

		String initiatorName = reader.getAttribute(ATTRIBUTE_INITIATE);
		String responderName = reader.getAttribute(ATTRIBUTE_RESPOND);

		collaborativeTask.setInitiatorName(initiatorName);
		collaborativeTask.setResponderName(responderName);

		return collaborativeTask;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CollaborativeTask.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final CollaborativeTask task = new CollaborativeTask("", "");
		// remove the default generated initiator and responder containers.
		task.removeStoryChild(task.getInitiatorContainer());
		task.removeStoryChild(task.getResponderContainer());
		return task;
	}
}
