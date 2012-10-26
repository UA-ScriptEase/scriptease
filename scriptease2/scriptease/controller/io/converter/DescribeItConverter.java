package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
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
 * @author kschenk
 * 
 */
public class DescribeItConverter implements Converter {
	public static final String TAG_DESCRIBEIT = "DescribeIt";
	private static final String TAG_SELECTED_PATH = "SelectedPath";
	private static final String TAG_HEAD = "HeadNode";
	private static final String TAG_PATH_MAP = "PathMap";
	private static final String TAG_ENTRY = "Entry";
	private static final String TAG_PATH = "Path";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final DescribeIt describeIt = (DescribeIt) source;

		// head node
		final DescribeItNode headNode = describeIt.getStartNode();
		writer.startNode(TAG_HEAD);
		context.convertAnother(headNode);
		writer.endNode();

		// paths
		writer.startNode(TAG_PATH_MAP);

		final Collection<Collection<DescribeItNode>> paths = describeIt
				.getPaths();
		for (Collection<DescribeItNode> path : paths) {
			// path with the consisting nodes and the resulting doIt
			writer.startNode(TAG_ENTRY);
			// nodes
			writer.startNode(TAG_PATH);
			context.convertAnother(new ArrayList<DescribeItNode>(path));
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

		// optional selected path
		final Collection<DescribeItNode> selectedPath = describeIt
				.getSelectedPath();
		if (selectedPath != null) {
			writer.startNode(TAG_SELECTED_PATH);
			context.convertAnother(new ArrayList<DescribeItNode>(selectedPath));
			writer.endNode();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		DescribeIt describeIt = null;
		DescribeItNode headNode = null;
		Map<Collection<DescribeItNode>, ScriptIt> paths = null;
		List<DescribeItNode> selectedPath = null;

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
				if (reader.getNodeName().equals(
						DescribeItNodeConverter.TAG_NODE_NAME))
					headNode = (DescribeItNode) context.convertAnother(
							describeIt, DescribeItNode.class);
				reader.moveUp();
			}
			// paths
			else if (nodeName.equals(TAG_PATH_MAP)) {
				paths = new HashMap<Collection<DescribeItNode>, ScriptIt>();
				// read all of the paths
				while (reader.hasMoreChildren()) {
					reader.moveDown();
					// read the path
					if (reader.getNodeName().equals(TAG_ENTRY)) {
						List<DescribeItNode> nodes = null;
						ScriptIt doIt = null;
						while (reader.hasMoreChildren()) {
							reader.moveDown();
							// read the nodes
							if (reader.getNodeName().equals(TAG_PATH)) {
								nodes = new ArrayList<DescribeItNode>();
								nodes.addAll((Collection<DescribeItNode>) context
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
			// selected path
			else if (nodeName.equals(TAG_SELECTED_PATH)) {
				selectedPath = new ArrayList<DescribeItNode>();
				selectedPath.addAll((Collection<DescribeItNode>) context
						.convertAnother(describeIt, ArrayList.class));
			}
			reader.moveUp();
		}

		describeIt = new DescribeIt(headNode, paths, selectedPath);
		return describeIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(DescribeIt.class);
	}
}
