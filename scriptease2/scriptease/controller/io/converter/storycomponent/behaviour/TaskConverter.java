package scriptease.controller.io.converter.storycomponent.behaviour;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.converter.storycomponent.ComplexStoryComponentConverter;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryPoint;
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

		final Collection<Task> successors = new ArrayList<Task>();

		reader.moveDown();
		
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_CHANCE))
				System.err.println("Expected " + TAG_CHANCE + " but found " + reader.getNodeName());
			else {
				final String chance = reader.getValue();
				
				task.setChance(new Double(chance));
			}
		}
		
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_SUCCESSORS))
				System.err.println("Expected successors list, but found "
						+ reader.getNodeName());
			else {
				while (reader.hasMoreChildren()) {
					reader.moveDown();
					final String nodeName = reader.getNodeName();
					
					if (nodeName.equals(IndependentTaskConverter.TAG_INDEPENDENT_TASK)) {
						successors.add((IndependentTask) context.convertAnother(
								task, StoryPoint.class));
					} else if (nodeName
							.equals(CollaborativeTaskConverter.TAG_COLLABORATIVE_TASK)) {
						successors.add((CollaborativeTask) context.convertAnother(
								task, StoryGroup.class));
					} else {
						System.err
								.println("Trying to read a non Task ("
										+ reader.getNodeName()
										+ ") successor from " + task);
					}
					reader.moveUp();
				}
				
				task.addSuccessors(successors);
			}
		}
		
		reader.moveUp();

		return task;
	}
}
