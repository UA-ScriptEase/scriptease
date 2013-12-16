package scriptease.gui.SEGraph.models;

import java.util.Collection;

import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;

/**
 * A graph model for tasks {@link Task}
 * 
 * @author jyuen
 */
public class TaskGraphModel extends SEGraphModel<Task> {

	final Task start;

	public TaskGraphModel(Task start) {
		super(start);
		this.start = start;
	}

	@Override
	public Task createNewNode() {
		if (start instanceof IndependentTask)
			return new IndependentTask("");
		else
			return new CollaborativeTask("", "");
	}

	@Override
	public boolean addChild(Task child, Task existingNode) {
		return existingNode.addSuccessor(child);
	}

	@Override
	public boolean removeChild(Task child, Task existingNode) {
		return existingNode.removeSuccessor(child);
	}

	@Override
	public Collection<Task> getChildren(Task node) {
		return node.getSuccessors();
	}

	@Override
	public Collection<Task> getParents(Task node) {
		return node.getParents();
	}

	@Override
	public boolean overwriteNodeData(Task existingNode, Task node) {
		if (existingNode == node)
			return false;

		for (Task parent : existingNode.getParents())
			parent.addSuccessor(node);

		for (Task child : existingNode.getSuccessors())
			node.addSuccessor(child);

		this.removeNode(existingNode);

		return true;
	}
}
