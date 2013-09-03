package scriptease.gui.SEGraph.controllers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.swing.JComponent;

import scriptease.gui.SEGraph.SEGraph;

/**
 * Handles all graph grouping functionalities.
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
	 * Adds the <code>node</code> to the group.
	 * 
	 * @param node
	 * @return
	 */
	public void addNodeToGroup(E node) {
		if (this.group.isEmpty() && this.isValidGroupStart(node)) {
			this.startNode = node;
			this.group.add(node);
		} else if (startNode != null) {
			// Check whether the node is already in the group
			if (group.contains(node) && node != startNode) {
				group.remove(node);
				this.removeGroupOrphans(node);
			} else {
				// Now we can start forming a group

				// We don't want to add anything behind the start node.
				if (!this.graph.model.getParents(node).contains(startNode)) {
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
						group = this.findGroupPaths(node, group,
								this.startNode);
						group.add(node);
					}

				} else {
					// Parent is already in graph, don't do extra work
					group.add(node);
				}

			}
		}

		if (this.isGroup(group)) {
			// TODO do something here later - like change all their colors
			for (E n : this.group) {

			}
		} else {
			// Let's do some repainting on the components.
			for (E groupableNode : this.group) {
				final JComponent component;
				component = this.graph.nodesToComponents
						.getValue(groupableNode);

				// this.renderer.setComponentAppearance(component,
				// groupableNode, backgroundColour)
			}
		}
	}

	/**
	 * Clears the current group.
	 */
	public void resetGroup() {
		this.group.clear();
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
	 * Checks whether the current group contains <code>node</code>
	 * @return
	 */
	public boolean containsNode(E node) {
		return this.group.contains(node);
	}

	/**
	 * Checks whether the node is a valid group start
	 * 
	 * @param node
	 */
	private boolean isValidGroupStart(E node) {
		final Queue<E> nodes = new LinkedList<E>();

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
					} else if (!visited.contains(child)){
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
}
