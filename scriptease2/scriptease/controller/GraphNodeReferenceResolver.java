package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.SEGraph.nodes.KnowItNode;
import scriptease.gui.SEGraph.nodes.TextNode;

/**
 * GraphNodeVisitor used to resolve referencing problems created when cloning
 * GraphNodes. Replaces references to GraphNode nodes with a reference to the
 * first found equal GraphNode.
 * 
 * @author mfchurch
 * 
 * @deprecated This needs to be removed. We have SEGraph now, which is more
 *             coder-friendly and does more things. This also shouldn't be its
 *             own class.
 * 
 */
public class GraphNodeReferenceResolver implements GraphNodeVisitor {
	private Collection<GraphNode> nodes;

	/**
	 * Iterate over the graph twice, first time compiling original nodes -
	 * second time replacing equal references with the original
	 * 
	 * @param graphNode
	 */
	public void resolveReferences(GraphNode graphNode) {
		this.nodes = new ArrayList<GraphNode>();

		// Use a set to group equal nodes together
		Set<GraphNode> equalSet = new HashSet<GraphNode>(graphNode
				.getNodeDepthMap().keySet());
		this.nodes.addAll(equalSet);

		// fix the problems
		graphNode.process(this);
	}

	@Override
	public void processTextNode(TextNode textNode) {
		this.processDefault(textNode);
	}

	@Override
	public void processKnowItNode(KnowItNode knowItNode) {
		this.processDefault(knowItNode);
	}

	private void processDefault(GraphNode graphNode) {
		final List<GraphNode> children = graphNode.getChildren();
		// for each child
		for (GraphNode child : children) {
			boolean replaced = false;
			// if it is not original
			for (GraphNode originalNode : this.nodes) {
				// replace it with the original
				if (originalNode.equals(child) && originalNode != child) {
					replaced = graphNode.replaceChild(child, originalNode);
					break;
				}
			}
			if (!replaced)
				child.process(this);
		}
	}
}
