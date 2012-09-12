package scriptease.controller.io.converter.graphnode;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.gui.SEGraph.nodes.GraphNode;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * GraphNodeConverter converts GraphNodes to/from XML
 * 
 * @author mfchurch
 * 
 * 
 * @deprecated GraphNodeConverter needs to be removed. We have SEGraph now,
 *             which is more coder-friendly and does more things.
 */
public abstract class GraphNodeConverter implements Converter {
	public static final String TAG_GRAPH_NODE = "GraphNode";
	private static final String TAG_TERMINAL = "terminal";
	private static final String TAG_CHILDREN = "Children";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final GraphNode graphNode = (GraphNode) source;

		// terminal
		final boolean terminal = graphNode.isTerminalNode();
		if (terminal)
			writer.addAttribute(TAG_TERMINAL, "true");

		// Children
		writer.startNode(TAG_CHILDREN);
		context.convertAnother(new ArrayList<GraphNode>(graphNode.getChildren()));
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final GraphNode graphNode;

		graphNode = this.buildNode(reader, context);

		// default to false if not found
		final String terminal_tag = reader.getAttribute(TAG_TERMINAL);
		final boolean isTerminal = (terminal_tag == null ? false : terminal_tag
				.equalsIgnoreCase("true"));
		graphNode.setTerminal(isTerminal);

		// children
		reader.moveDown();
		final String nodeName = reader.getNodeName();
		if (nodeName.equals(TAG_CHILDREN)) {
			graphNode.addChildren((Collection<GraphNode>) context
					.convertAnother(graphNode, ArrayList.class));
		}
		reader.moveUp();

		return graphNode;
	}

	/**
	 * Builds an instance of the specific GraphNode subclass that the converter
	 * is intended to convert. This is used as part of the unmarshalling
	 * process, before all other GraphNode properties have been read. The
	 * unmarshalling parameters can be used to read any information that is
	 * necessary for constructing the object.
	 * 
	 * @param reader
	 *            the reader to read from
	 * @param context
	 *            the context to read in
	 * 
	 * @return instance a GraphNode subclass
	 * @see #unmarshal(HierarchicalStreamReader, UnmarshallingContext)
	 */
	protected abstract GraphNode buildNode(HierarchicalStreamReader reader,
			UnmarshallingContext context);

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(GraphNode.class);
	}
}
