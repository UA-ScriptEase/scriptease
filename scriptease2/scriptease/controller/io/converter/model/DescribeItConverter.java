package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.controller.io.XMLNode;
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

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final DescribeIt describeIt = (DescribeIt) source;

		XMLNode.NAME.writeString(writer, describeIt.getName());

		XMLNode.TYPES.writeChildren(writer, describeIt.getTypes());

		XMLNode.DESCRIBEITNODE.writeObject(writer, context,
				describeIt.getStartNode());

		writer.startNode(XMLNode.PATHMAP.getName());
		for (Collection<DescribeItNode> path : describeIt.getPaths()) {
			// path with the consisting nodes and the resulting ScriptIt
			writer.startNode(XMLNode.ENTRY.getName());
			// nodes
			XMLNode.PATH.writeObject(writer, context, path);
			XMLNode.SCRIPTIT.writeObject(writer, context,
					describeIt.getScriptItForPath(path));

			writer.endNode();
		}
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final Collection<String> types;
		final Map<Collection<DescribeItNode>, ScriptIt> paths;

		final String name;
		final DescribeItNode headNode;

		types = new ArrayList<String>();
		paths = new HashMap<Collection<DescribeItNode>, ScriptIt>();

		name = XMLNode.NAME.readString(reader);
		types.addAll(XMLNode.TYPES.readStringCollection(reader));
		headNode = XMLNode.DESCRIBEITNODE.readObject(reader, context,
				DescribeItNode.class);

		reader.moveDown();
		if (reader.getNodeName().equals(XMLNode.PATHMAP.getName())) {
			// read all of the paths
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				// read the path
				if (reader.getNodeName().equals(XMLNode.ENTRY.getName())) {
					final Collection<DescribeItNode> nodes;
					final ScriptIt scriptIt;

					nodes = XMLNode.PATH.readCollection(reader, context,
							DescribeItNode.class);
					scriptIt = XMLNode.SCRIPTIT.readObject(reader, context,
							ScriptIt.class);

					if (nodes != null && !nodes.isEmpty())
						paths.put(nodes, scriptIt);
				}
				reader.moveUp();
			}
		}
		reader.moveUp();

		return new DescribeIt(name, headNode, paths, types);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(DescribeIt.class);
	}
}
