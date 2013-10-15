package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.controller.io.converter.storycomponent.ComplexStoryComponentConverter;
import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
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

	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_PRIORITY = "priority";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final Behaviour behaviour = (Behaviour) source;

		// type
		writer.addAttribute(ATTRIBUTE_TYPE, behaviour.getType().toString());

		// priority
		writer.addAttribute(ATTRIBUTE_PRIORITY, behaviour.getPriority()
				.toString());

		super.marshal(source, writer, context);

		// start task
		writer.startNode(TAG_START_TASK);
		context.convertAnother(behaviour.getStartTask());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {

		String type = null;
		String priority = null;
		Task startTask = null;

		type = reader.getAttribute(ATTRIBUTE_TYPE);
		priority = reader.getAttribute(ATTRIBUTE_PRIORITY);

		final Behaviour behaviour = (Behaviour) super
				.unmarshal(reader, context);

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();
			
			if (nodeName.equals(TAG_START_TASK)) {
				
				if (type == Behaviour.Type.INDEPENDENT.toString()) {
					startTask = (IndependentTask) context
							.convertAnother(behaviour, IndependentTask.class);
					
				} else if (type == Behaviour.Type.COLLABORATIVE.toString()) {
					startTask = (CollaborativeTask) context
							.convertAnother(behaviour, CollaborativeTask.class);
				}
			}

			reader.moveUp();
		}

		behaviour.setType(Behaviour.Type.valueOf(type.toUpperCase()));
		behaviour.setPriority(new Integer(priority));
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
