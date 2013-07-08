package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import scriptease.controller.io.XMLNode;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeItNode;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts DescribeItNodes to and from XML.
 * 
 * @author kschenk
 * 
 */
public class DescribeItNodeConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {

		final DescribeItNode node = (DescribeItNode) source;
		final KnowIt value = node.getKnowIt();

		XMLNode.NAME.writeString(writer, node.getName());

		// Not all KnowIts have a value attached.
		if (value != null) {
			XMLNode.KNOWIT.writeObject(writer, context, value);
		}

		XMLNode.SUCCESSORS.writeObject(writer, context, node.getSuccessors());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final DescribeItNode node;
		final Collection<DescribeItNode> successors;

		final String name;
		KnowIt knowIt = null;

		node = new DescribeItNode("", null);
		successors = new HashSet<DescribeItNode>();

		name = XMLNode.NAME.readString(reader);

		while (reader.hasMoreChildren()) {
			reader.moveDown();

			final String nodeName = reader.getNodeName();

			// TODO Can't refactor this with current XMLNode methods because
			// it's optional, and we can't go backwards when reading.
			if (nodeName.equals(XMLNode.KNOWIT.getName())) {
				knowIt = (KnowIt) context.convertAnother(node, KnowIt.class);
			} else if (nodeName.equals(XMLNode.SUCCESSORS.getName())) {
				successors.addAll((Collection<DescribeItNode>) context
						.convertAnother(node, ArrayList.class));
			}

			reader.moveUp();
		}

		node.setName(name);
		node.setKnowIt(knowIt);
		node.addSuccessors(successors);

		return node;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(DescribeItNode.class);
	}
}
