package scriptease.gui.SEGraph.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import scriptease.controller.StoryAdapter;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WindowFactory;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.UserInformationPane.UserInformationType;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModelManager;
import sun.awt.util.IdentityArrayList;

/**
 * Handles all graph grouping functionalities. A graph group represents a set of
 * nodes in a graph {@link SEGraph} that meet the the conditions to form a
 * group. These conditions including having at max one exit and at minimum two
 * nodes.
 * 
 * @author jyuen
 * @author neesha
 */
public class GraphGroupController<E> {

	private final SEGraph<E> graph;

	private Set<E> group;
	private E startNode;

	public GraphGroupController(SEGraph<E> graph) {
		this.graph = graph;

		this.group = new HashSet<E>();
		this.startNode = null;
	}

	/**
	 * Returns true if the provided node can legally be added to the current
	 * group, false otherwise.
	 * 
	 * @param node
	 * @return
	 */
	public boolean isNodeLegal(E node) {
		if (node == graph.getStartNode())
			return false;

		if (this.group.isEmpty()) {
			if (this.isValidGroupStart(node))
				return true;
			else
				return false;
			// Don't want anything before the start node.
		} else if (this.graph.model.getAncestors(node).contains(this.startNode)
				|| this.startNode == node)
			return true;

		return false;
	}

	/**
	 * Adds the <code>node</code> to the group.
	 * 
	 * @param node
	 * @return
	 */
	public void addNodeToGroup(E node) {
		if (this.group.isEmpty() && this.isValidGroupStart(node)) {
			this.startNode = node;
			this.group.add(node);
		} else if (this.startNode != null) {
			// Check whether the node is already in the group
			if (group.contains(node) && node != startNode) {
				group.remove(node);
				this.removeGroupOrphans(node);
			} else {
				// Now we can start forming a group

				// We don't want to add anything behind the start node.
				if (!this.graph.model.getParents(node).contains(this.startNode)) {
					// We need to find path back to start
					final Set<E> tempGroup = new HashSet<E>();
					final Queue<E> backQueue = new LinkedList<E>();
					boolean foundStart = false;

					// Must always include start in set
					tempGroup.add(startNode);
					tempGroup.add(node);
					backQueue.add(node);

					while (!backQueue.isEmpty() && !foundStart) {
						final E currNode = backQueue.poll();

						for (E parent : this.graph.model.getParents(currNode)) {
							if (parent == startNode) {
								foundStart = true;
							}

							if (!tempGroup.contains(parent)) {
								tempGroup.add(parent);

								if (!backQueue.contains(parent)) {
									backQueue.offer(parent);
								}
							}
						}
					}

					if (foundStart) {
						// There is at least once path from start to node, time
						// to find all of them and add nodes
						group = this
								.findGroupPaths(node, group, this.startNode);
						group.add(node);
					}

				} else {
					// Parent is already in graph, don't do extra work
					group.add(node);
				}
			}
		}
	}

	/**
	 * Ungroups the passed in group node.
	 * 
	 * @param group
	 */
	public void unformGroup(StoryGroup group) {

		if (!UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction("UnGroup");

		final Collection<StoryNode> parents = new ArrayList<StoryNode>();
		final Collection<StoryNode> successors = new ArrayList<StoryNode>();

		final StoryNode startNode = group.getStartNode();
		final StoryNode exitNode = group.getExitNode();

		parents.addAll(group.getParents());
		successors.addAll(group.getSuccessors());

		for (StoryNode child : successors) {
			exitNode.addSuccessor(child);
			group.removeSuccessor(child);
		}

		for (StoryNode parent : parents) {
			parent.addSuccessor(startNode);
			parent.removeSuccessor(group);
		}

		group.removeStoryChildren(group.getChildren());

		// Lets help garbage collection a little by setting the group to null
		group = null;

		if (UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().endUndoableAction();
	}

	/**
	 * Forms the passed in group nodes.
	 * 
	 * @param ungroup
	 */
	public void formGroup() {

		if (!UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction("Group");

		// Make sure we even have a group.
		if (!this.isGroup()) {
			WindowFactory
					.getInstance()
					.showUserInformationBox(
							"You can't form a group yet. All selected nodes must be green.",
							UserInformationType.ERROR);
			return;
		}

		// Not dealing with dialogue groups for now
		if (!(this.startNode instanceof StoryNode))
			return;

		final StoryNode exitNode = (StoryNode) this.getExitNode();

		final StoryNode startNode = (StoryNode) this.startNode;

		// Order the group first
		final List<StoryNode> groupToForm = new IdentityArrayList<StoryNode>();

		for (E node : this.group) {
			final StoryNode storyNode = (StoryNode) node;

			SEModelManager.getInstance().getActiveRoot()
					.process(new StoryAdapter() {
						@Override
						public void processStoryGroup(StoryGroup storyGroup) {
							if (storyNode == storyGroup) {
								if (!groupToForm.contains(storyNode))
									groupToForm.add(storyNode);
							}

							for (StoryNode successor : storyGroup
									.getSuccessors()) {
								if (!groupToForm.contains(successor))
									successor.process(this);
							}
						}

						@Override
						public void processStoryPoint(StoryPoint storyPoint) {
							if (storyNode == storyPoint) {
								if (!groupToForm.contains(storyNode))
									groupToForm.add(storyPoint);
							}

							for (StoryNode successor : storyPoint
									.getSuccessors())
								if (!groupToForm.contains(successor))
									successor.process(this);
						}
					});
		}

		Collections.reverse(groupToForm);

		final StoryGroup newGroup = new StoryGroup(null, groupToForm,
				startNode, exitNode, true);

		// Connect the children of the exit node to the new group node and
		// remove the child from the exit node.
		final Collection<StoryNode> children = new ArrayList<StoryNode>();
		children.addAll(exitNode.getSuccessors());
		if (exitNode != null) {
			for (StoryNode child : children) {
				if (!this.group.contains(child)) {
					newGroup.addSuccessor(child);
					exitNode.removeSuccessor(child);
				}
			}
		}

		// Connect the parents of the start node to the new group node. and
		// remove this parent from the start node.
		final Collection<StoryNode> parents = new ArrayList<StoryNode>();
		parents.addAll(startNode.getParents());
		for (StoryNode parent : parents) {
			parent.addSuccessor(newGroup);
			parent.removeSuccessor(startNode);
		}

		this.graph.repaint();
		this.resetGroup();

		if (UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().endUndoableAction();
	}

	/**
	 * Clears the current group.
	 */
	public void resetGroup() {
		this.group.clear();
		this.startNode = null;
	}

	/**
	 * Checks whether the current group is a valid group.
	 * 
	 * @return
	 */
	public boolean isGroup() {
		return this.isGroup(this.group);
	}

	/**
	 * Checks whether the current group contains <code>node</code>.
	 * 
	 * @return
	 */
	public boolean containsNode(E node) {
		return this.group.contains(node);
	}

	/**
	 * Gets the current group being formed.
	 * 
	 * @return
	 */
	public Set<E> getCurrentGroup() {
		return this.group;
	}

	/**
	 * Gets the start node of the group being formed.
	 * 
	 * @return
	 */
	public E getStartNode() {
		return this.startNode;
	}

	/**
	 * Checks whether the node is a valid group start
	 * 
	 * @param node
	 */
	public boolean isValidGroupStart(E node) {
		final Queue<E> nodes = new LinkedList<E>();

		// If this is the graph start node, return false immediately.
		if (node == this.graph.model.getStartNode()) {
			WindowFactory.getInstance().showUserInformationBox(
					"The start node can't be part of a group.",
					UserInformationType.ERROR);
			return false;
		}

		for (E child : this.graph.model.getChildren(node)) {
			nodes.add(child);
		}

		while (nodes.size() >= 1) {
			final E currentNode = nodes.poll();

			Set<E> currentGroup = new HashSet<E>();

			// Must always include start in the set
			currentGroup.add(node);
			currentGroup = this.findGroupPaths(currentNode, currentGroup, null);

			if (this.isGroup(currentGroup)) {
				return true;
			} else {
				for (E temp : this.graph.model.getChildren(currentNode))
					nodes.add(temp);
			}
		}

		return false;
	}

	/**
	 * Find children who are now all orphaned and remove them from the group.
	 * 
	 * @param node
	 */
	private void removeGroupOrphans(E node) {
		final Queue<E> examine = new LinkedList<E>();
		examine.add(node);

		while (!examine.isEmpty()) {
			final E currentNode = examine.poll();

			for (E child : this.graph.model.getChildren(currentNode)) {
				boolean parentInGroup = false;

				for (E parent : this.graph.model.getParents(child)) {
					if (group.contains(parent)) {
						parentInGroup = true;
					}
				}

				if (!parentInGroup) {
					examine.add(child);
					group.remove(child);
				}
			}
		}
	}

	/**
	 * A nodes should be visited when: 1. All of it's children are visited. 2.
	 * It's been used to find a path to the goal.
	 * 
	 * Retreat once we find a child that is either the end or part of the group.
	 * Retreat when there are no children. When retreating, go back one, see if
	 * there are other children to visit.
	 * 
	 * @param endNode
	 * @param group
	 * @return
	 */
	private Set<E> findGroupPaths(E endNode, Set<E> group, E startNode) {
		final Stack<E> nodes = new Stack<E>();
		final Set<E> visited = new HashSet<E>();

		nodes.push(endNode);

		boolean notDone = true;
		boolean found = false;

		while (!nodes.isEmpty() && notDone) {
			E currentNode = nodes.peek();

			boolean foundChildInGroup = false;
			if (currentNode == startNode || group.contains(currentNode)
					&& currentNode != startNode) {

				// We found an end point, back up one
				visited.add(currentNode);
				nodes.pop();
				group.add(currentNode);

			} else if (this.graph.model.getParents(currentNode).size() > 0) {
				found = false;

				for (E child : this.graph.model.getParents(currentNode)) {
					if (group.contains(child)) {
						foundChildInGroup = true;
					} else if (!visited.contains(child)) {
						// not done yet add child
						found = true;
						nodes.push(child);
					}

					if (found)
						break;
				}

				if (!found) {
					// no children were found
					final E node = nodes.pop();
					visited.add(node);
					// if one child is in group, add parent...
					if (foundChildInGroup) {
						group.add(node);
					}
				}

			} else {
				// no children, so back up one
				visited.add(nodes.pop());
			}
		}

		return group;
	}

	/**
	 * Checks whether <code>nodes</code> is a valid group.
	 * 
	 * @param nodes
	 * @return
	 */
	private boolean isGroup(Set<E> nodes) {
		int numChild = 0;
		int numParents = 0;

		// Can't group only one node.
		if (nodes.size() <= 1)
			return false;

		for (E node : nodes) {
			for (E child : this.graph.model.getChildren(node)) {
				if (!nodes.contains(child)) {
					numChild++;
					break;
				}
			}

			for (E parent : this.graph.model.getParents(node)) {
				if (!nodes.contains(parent)) {
					numParents++;
					break;
				}
			}

			if (numParents > 1 || numChild > 1) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Get the exit node of the current group. By default, the exit node is the
	 * node that contains children not within the group. If no such node exists,
	 * then the exit node is the first node encountered that is at the deepest
	 * level in the graph.
	 * 
	 * @return
	 */
	private E getExitNode() {
		if (!this.isGroup())
			return null;

		E exitNode = null;
		for (E node : this.group) {
			if (!this.group.containsAll(this.graph.getChildren(node))) {
				exitNode = node;
				break;
			}
		}

		if (exitNode == null) {
			final Map<E, Integer> depthMap = this.graph.model.getDepthMap();

			int deepestLevel = -1;
			for (E node : depthMap.keySet()) {
				if (this.group.contains(node)) {
					if (depthMap.get(node) > deepestLevel) {
						deepestLevel = depthMap.get(node);
						exitNode = node;
					}
				}
			}
		}

		return exitNode;
	}
}
