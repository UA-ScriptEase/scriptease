package scriptease.controller.io.converter.storycomponent.behaviour;

import java.util.HashSet;
import java.util.Set;

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
	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class.
	public static final String TAG_CHANCE = "Chance";
	public static final String TAG_SUCCESSORS = "Successors";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final Task task = (Task) source;
		super.marshal(source, writer, context);

		writer.startNode(TAG_CHANCE);
		writer.setValue(task.getChance().toString());
		writer.endNode();

		writer.startNode(TAG_SUCCESSORS);
		context.convertAnother(task.getSuccessors());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final Task task = (Task) super.unmarshal(reader, context);

		final Set<Task> successors = new HashSet<Task>();
		String chance = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();

			if (reader.getNodeName().equalsIgnoreCase(TAG_CHANCE)) {
				chance = reader.getValue();
				
			} else if (reader.getNodeName().equalsIgnoreCase(TAG_SUCCESSORS)) {
				
				while (reader.hasMoreChildren()) {
					reader.moveDown();
					final String nodeName = reader.getNodeName();

					if (nodeName
							.equals(IndependentTaskConverter.TAG_INDEPENDENT_TASK)) {
						successors.add((IndependentTask) context
								.convertAnother(task, IndependentTask.class));
					} else if (nodeName
							.equals(CollaborativeTaskConverter.TAG_COLLABORATIVE_TASK)) {
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
				System.err
						.println("Expected " + TAG_CHANCE + " or "
								+ TAG_SUCCESSORS + " but found "
								+ reader.getNodeName());
			}

			reader.moveUp();
		}

		task.setChance(Double.parseDouble(chance));
		task.setSuccessors(successors);
		return task;
	}
}
