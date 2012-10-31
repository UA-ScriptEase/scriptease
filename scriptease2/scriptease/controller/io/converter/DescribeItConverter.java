package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.controller.io.FileIO;
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

	private static final String TAG_TYPE = "Type";
	private static final String TAG_TYPES = "Types";
	private static final String TAG_PATH_MAP = "PathMap";
	private static final String TAG_ENTRY = "Entry";
	private static final String TAG_PATH = "Path";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final DescribeIt describeIt = (DescribeIt) source;

		// types
		writer.startNode(TAG_TYPES);
		for (String type : describeIt.getTypes()) {
			writer.startNode(TAG_TYPE);
			writer.setValue(type);
			writer.endNode();
		}
		writer.endNode();

		// head node
		final DescribeItNode headNode = describeIt.getStartNode();
		writer.startNode(DescribeItNodeConverter.TAG_NODE_NAME);
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
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final Collection<String> types;

		types = new ArrayList<String>();

		DescribeIt describeIt = null;
		DescribeItNode headNode = null;
		Map<Collection<DescribeItNode>, ScriptIt> paths = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_TYPES)) {
				while (reader.hasMoreChildren()) {
					types.add(FileIO.readValue(reader, TAG_TYPE));
				}
			}
			// head node - can't think of a better way to handle this
			else if (nodeName.equals(DescribeItNodeConverter.TAG_NODE_NAME)) {
				headNode = (DescribeItNode) context.convertAnother(describeIt,
						DescribeItNode.class);
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
			reader.moveUp();
		}

		describeIt = new DescribeIt(headNode, paths, types);
		return describeIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(DescribeIt.class);
	}
}
