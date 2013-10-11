package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.controller.io.converter.storycomponent.ComplexStoryComponentConverter;
import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class BehaviourConverter extends ComplexStoryComponentConverter {

	public static final String TAG_BEHAVIOUR = "Behaviour";
	
	public static final String TAG_START_TASK = "StartTask";

	// TODO consider making the type and priority a attribute.
	public static final String TAG_TYPE = "Type";
	public static final String TAG_PRIORITY = "Priority";
	
	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final Behaviour behaviour = (Behaviour) source;
		super.marshal(source, writer, context);

		// type
		writer.startNode(TAG_TYPE);
		writer.setValue(behaviour.getType().toString());
		writer.endNode();

		// priority
		writer.startNode(TAG_PRIORITY);
		writer.setValue(behaviour.getPriority().toString());
		writer.endNode();

		// start task
		writer.startNode(TAG_START_TASK);
		context.convertAnother(behaviour.getStartTask());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final CollaborativeTask collaborativeTask = (CollaborativeTask) super
				.unmarshal(reader, context);

		Behaviour.Type type = null;
		 priority = null;

		StoryComponentContainer initiatorEffectsContainer = null;
		StoryComponentContainer responderEffectsContainer = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_INITIATOR)) {
				initiatorName = reader.getValue();
			} else if (nodeName.equals(TAG_RESPONDER)) {
				responderName = reader.getValue();
			} else if (nodeName.equals(TAG_INITIATOR_EFFECTS_CONTAINER)) {
				initiatorEffectsContainer = (StoryComponentContainer) context
						.convertAnother(collaborativeTask,
								StoryComponentContainer.class);
			} else if (nodeName.equals(TAG_RESPONDER_EFFECTS_CONTAINER)) {
				responderEffectsContainer = (StoryComponentContainer) context
						.convertAnother(collaborativeTask,
								StoryComponentContainer.class);
			}

			reader.moveUp();
		}

		collaborativeTask.setInitiatorName(initiatorName);
		collaborativeTask.setResponderName(responderName);

		collaborativeTask
				.setInitiatorEffectsContainer(initiatorEffectsContainer);
		collaborativeTask
				.setResponderEffectsContainer(responderEffectsContainer);

		return collaborativeTask;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(Behaviour.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new Behaviour("");
	}
}
