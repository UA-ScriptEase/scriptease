package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.controller.io.converter.storycomponent.ScriptItConverter;
import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Reads and writes a Behaviour {@link Behaviour} from xml.
 * 
 * @author jyuen
 */
public class BehaviourConverter extends ScriptItConverter {

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final Behaviour behaviour = (Behaviour) source;

		XMLAttribute.TYPE.write(writer, behaviour.getType().toString());
		XMLAttribute.PRIORITY.write(writer, behaviour.getPriority().toString());

		super.marshal(source, writer, context);

		XMLNode.START_TASK.writeObject(writer, context,
				behaviour.getStartTask());
	}

	@Override
	public StoryComponent unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {

		final String type = XMLAttribute.TYPE.read(reader);
		final String priority = XMLAttribute.PRIORITY.read(reader);
		Task startTask = null;

		final StoryComponent component = super.unmarshal(reader, context);

		if (!(component instanceof Behaviour))
			return component;

		final Behaviour behaviour = (Behaviour) component;

		if (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(XMLNode.START_TASK.getName())) {

				if (type.equals(Behaviour.Type.INDEPENDENT.toString())) {
					startTask = (IndependentTask) context.convertAnother(
							behaviour, IndependentTask.class);

				} else if (type.equals(Behaviour.Type.COLLABORATIVE.toString())) {
					startTask = (CollaborativeTask) context.convertAnother(
							behaviour, CollaborativeTask.class);
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
			UnmarshallingContext context, LibraryModel library) {
		return new Behaviour(library, "");
	}
}
