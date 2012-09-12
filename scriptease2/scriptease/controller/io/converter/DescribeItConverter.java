package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.controller.GraphNodeAdapter;
import scriptease.controller.io.converter.graphnode.KnowItNodeConverter;
import scriptease.controller.io.converter.graphnode.TextNodeConverter;
import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.SEGraph.nodes.KnowItNode;
import scriptease.gui.SEGraph.nodes.TextNode;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.complex.ScriptIt;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter used for DescribeIt file I/O
 * 
 * @author mfchurch
 * 
 */
public class DescribeItConverter implements Converter {
	public static final String TAG_DESCRIBEIT = "DescribeIt";
	private static final String TAG_DEFAULT_PATH = "DefaultPath";
	private static final String TAG_SELECTED_PATH = "SelectedPath";
	private static final String TAG_HEAD = "Head";
	private static final String TAG_PATH_MAP = "PathMap";
	private static final String TAG_ENTRY = "Entry";
	private static final String TAG_PATH = "Path";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final DescribeIt describeIt = (DescribeIt) source;

		// head node
		final GraphNode headNode = describeIt.getHeadNode();
		writer.startNode(TAG_HEAD);
		headNode.process(new GraphNodeAdapter() {
			
			@Override
			public void processTextNode(TextNode textNode) {
				writer.startNode(TextNodeConverter.TAG_TEXT_NODE);
				defaultProcess();
			} 

			@Override
			public void processKnowItNode(KnowItNode knowItNode) {
				writer.startNode(KnowItNodeConverter.TAG_KNOWIT_NODE);
				defaultProcess();
			}

			private void defaultProcess() {
				context.convertAnother(headNode);
				writer.endNode();
			}
		});

		writer.endNode();

		// paths
		writer.startNode(TAG_PATH_MAP);
		final Collection<List<GraphNode>> paths = describeIt.getPaths();
		for (List<GraphNode> path : paths) {
			// path with the consisting nodes and the resulting doIt
			writer.startNode(TAG_ENTRY);
			// nodes
			writer.startNode(TAG_PATH);
			context.convertAnother(new ArrayList<GraphNode>(path));
			writer.endNode();

			// optional doIt value
			final ScriptIt value = describeIt.getScriptItForPath(path);
			if (value != null) {
				writer.startNode(ScriptItConverter.TAG_SCRIPTIT);
				context.convertAnother(value);
				writer.endNode();
			}
			writer.endNode();
		}
		writer.endNode();

		// default path
		final List<GraphNode> defaultPath = describeIt.getDefaultPath();
		writer.startNode(TAG_DEFAULT_PATH);
		context.convertAnother(new ArrayList<GraphNode>(defaultPath));
		writer.endNode();

		// optional selected path
		final List<GraphNode> selectedPath = describeIt.getSelectedPath();
		if (selectedPath != null) {
			writer.startNode(TAG_SELECTED_PATH);
			context.convertAnother(new ArrayList<GraphNode>(selectedPath));
			writer.endNode();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		DescribeIt describeIt = null;
		GraphNode headNode = null;
		Map<List<GraphNode>, ScriptIt> paths = null;
		List<GraphNode> defaultPath = null;
		List<GraphNode> selectedPath = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();
			// head node - can't think of a better way to handle this
			if (nodeName.equals(TAG_HEAD)) {
				reader.moveDown();
				/*
				 * I want to use a visitor, but we're breaking on a String not a
				 * class. So until we find a better way to do this make sure
				 * this encompasses all of the possible head node types :(
				 */
				if (reader.getNodeName()
						.equals(TextNodeConverter.TAG_TEXT_NODE))
					headNode = (GraphNode) context.convertAnother(describeIt,
							TextNode.class);
				else if (reader.getNodeName().equals(
						KnowItNodeConverter.TAG_KNOWIT_NODE))
					headNode = (GraphNode) context.convertAnother(describeIt,
							KnowItNode.class);
				reader.moveUp();
			}
			// paths
			else if (nodeName.equals(TAG_PATH_MAP)) {
				paths = new HashMap<List<GraphNode>, ScriptIt>();
				// read all of the paths
				while (reader.hasMoreChildren()) {
					reader.moveDown();
					// read the path
					if (reader.getNodeName().equals(TAG_ENTRY)) {
						List<GraphNode> nodes = null;
						ScriptIt doIt = null;
						while (reader.hasMoreChildren()) {
							reader.moveDown();
							// read the nodes
							if (reader.getNodeName().equals(TAG_PATH)) {
								nodes = new ArrayList<GraphNode>();
								nodes.addAll((Collection<GraphNode>) context
										.convertAnother(describeIt,
												ArrayList.class));
							}
							// read the doIt
							else if (reader.getNodeName().equals(
									ScriptItConverter.TAG_SCRIPTIT)) {
								doIt = (ScriptIt) context.convertAnother(
										describeIt, ScriptIt.class);
							}
							reader.moveUp();
						}
						if (nodes != null && !nodes.isEmpty())
							paths.put(nodes, doIt);
					}
					reader.moveUp();
				}
			}
			// default path
			else if (nodeName.equals(TAG_DEFAULT_PATH)) {
				defaultPath = new ArrayList<GraphNode>();
				defaultPath.addAll((Collection<GraphNode>) context
						.convertAnother(describeIt, ArrayList.class));
			}
			// selected path
			else if (nodeName.equals(TAG_SELECTED_PATH)) {
				selectedPath = new ArrayList<GraphNode>();
				selectedPath.addAll((Collection<GraphNode>) context
						.convertAnother(describeIt, ArrayList.class));
			}
			reader.moveUp();
		}

		describeIt = new DescribeIt(headNode, paths, defaultPath, selectedPath);
		return describeIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(DescribeIt.class);
	}
}
