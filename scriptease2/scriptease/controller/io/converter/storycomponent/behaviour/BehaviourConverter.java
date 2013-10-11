package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.controller.io.converter.storycomponent.ComplexStoryComponentConverter;
import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.Task;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Reads and writes a Behaviour {@link Behaviour} from xml.
 * 
 * @author jyuen
 */
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
		final Behaviour behaviour = (Behaviour) super
				.unmarshal(reader, context);

		Behaviour.Type type = null;
		Task startTask = null;
		Integer priority = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_TYPE)) {
				if (reader.getValue().equals(
						Behaviour.Type.INDEPENDENT.toString()))
					type = Behaviour.Type.INDEPENDENT;
				else if (reader.getValue().equals(
						Behaviour.Type.COLLABORATIVE.toString()))
					type = Behaviour.Type.COLLABORATIVE;
			} else if (nodeName.equals(TAG_PRIORITY)) {
				priority = new Integer(reader.getValue());
			} else if (nodeName.equals(TAG_START_TASK)) {
				startTask = (Task) context
						.convertAnother(behaviour, Task.class);
			}

			reader.moveUp();
		}

		behaviour.setType(type);
		behaviour.setPriority(priority);
		behaviour.setStartTask(startTask);

		return behaviour;
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
