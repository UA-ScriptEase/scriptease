package scriptease.controller.io.converter.storycomponent.behaviour;

import java.util.ArrayList;
import java.util.List;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
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

	public static final String TAG_INITIATOR = "Initiator";
	public static final String TAG_RESPONDER = "Responder";

	public static final String TAG_INITIATOR_EFFECTS = "InitiatorEffects";
	public static final String TAG_RESPONDER_EFFECTS = "ResponderEffects";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final CollaborativeTask collaborativeTask = (CollaborativeTask) source;
		super.marshal(source, writer, context);

		// initiator name
		writer.startNode(TAG_INITIATOR);
		writer.setValue(collaborativeTask.getInitiatorName());
		writer.endNode();

		// responder name
		writer.startNode(TAG_RESPONDER);
		writer.setValue(collaborativeTask.getResponderName());
		writer.endNode();

		// initiator effects container
		writer.startNode(TAG_INITIATOR_EFFECTS);
		context.convertAnother(collaborativeTask.getInitiatorEffects());
		writer.endNode();

		// responder effects container
		writer.startNode(TAG_RESPONDER_EFFECTS);
		context.convertAnother(collaborativeTask.getResponderEffects());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final CollaborativeTask collaborativeTask = (CollaborativeTask) super
				.unmarshal(reader, context);

		String initiatorName = null;
		String responderName = null;

		List<ScriptIt> initiatorEffects = null;
		List<ScriptIt> responderEffects = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_INITIATOR)) {
				initiatorName = reader.getValue();
			} else if (nodeName.equals(TAG_RESPONDER)) {
				responderName = reader.getValue();
			} else if (nodeName.equals(TAG_INITIATOR_EFFECTS)) {

				initiatorEffects = (List<ScriptIt>) context.convertAnother(
						collaborativeTask, ArrayList.class);

			} else if (nodeName.equals(TAG_RESPONDER_EFFECTS)) {

				responderEffects = (List<ScriptIt>) context.convertAnother(
						collaborativeTask, ArrayList.class);
			}

			reader.moveUp();
		}

		collaborativeTask.setInitiatorName(initiatorName);
		collaborativeTask.setResponderName(responderName);

		collaborativeTask.setInitiatorEffects(initiatorEffects);
		collaborativeTask.setResponderEffects(responderEffects);

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
		return new CollaborativeTask("", "");
	}
}
