package scriptease.controller.io.converter.storycomponent.behaviour;

import java.util.HashSet;
import java.util.Set;

import scriptease.controller.io.XMLNode;
import scriptease.controller.io.converter.storycomponent.ComplexStoryComponentConverter;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the Task {@link Task} to and from xml.
 * 
 * @author jyuen
 */
public abstract class TaskConverter extends ComplexStoryComponentConverter {

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final Task task = (Task) source;
		super.marshal(source, writer, context);

		XMLNode.CHANCE.writeString(writer, task.getChance().toString());
		XMLNode.SUCCESSORS.writeObject(writer, context, task.getSuccessors());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final Task task = (Task) super.unmarshal(reader, context);

		final Set<Task> successors = new HashSet<Task>();
		final String chance;

		if (reader.hasMoreChildren()) {
			chance = XMLNode.CHANCE.readString(reader);
		} else
			chance = "";

		if (reader.hasMoreChildren()) {
			reader.moveDown();
			final String successorXML = XMLNode.SUCCESSORS.getName();
			if (reader.getNodeName().equalsIgnoreCase(successorXML)) {

				while (reader.hasMoreChildren()) {
					reader.moveDown();
					final String nodeName = reader.getNodeName();

					if (nodeName.equals(XMLNode.INDEPENDENT_TASK.getName())) {
						successors.add((IndependentTask) context
								.convertAnother(task, IndependentTask.class));
					} else if (nodeName.equals(XMLNode.COLLABORATIVE_TASK
							.getName())) {
						successors.add((CollaborativeTask) context
								.convertAnother(task, CollaborativeTask.class));
					} else {
						System.err.println("Trying to read a non Task ("
								+ reader.getNodeName() + ") successor from "
								+ task);
					}
					reader.moveUp();
				}

			} else {
				System.err.println("Expected " + successorXML + " but found "
						+ reader.getNodeName());
			}

			reader.moveUp();
		}

		task.setChance(Double.parseDouble(chance));
		task.setSuccessors(successors);
		return task;
	}
}
