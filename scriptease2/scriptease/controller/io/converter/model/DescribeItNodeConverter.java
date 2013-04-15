package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import scriptease.controller.io.converter.storycomponent.KnowItConverter;
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
	public static final String TAG_NODE_NAME = "DescribeItNode";
	public static final String TAG_NAME = "Name";
	public static final String TAG_SUCCESSORS = "Successors";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {

		final DescribeItNode node = (DescribeItNode) source;

		writer.startNode(TAG_NAME);
		writer.setValue(node.getName());
		writer.endNode();

		final KnowIt value = node.getKnowIt();
		if (value != null) {
			writer.startNode(KnowItConverter.TAG_KNOWIT);
			context.convertAnother(value);
			writer.endNode();
		}

		writer.startNode(TAG_SUCCESSORS);
		context.convertAnother(node.getSuccessors());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final DescribeItNode node;
		final Collection<DescribeItNode> successors;

		String name = "";
		KnowIt knowIt = null;

		node = new DescribeItNode("", null);
		successors = new HashSet<DescribeItNode>();
		
		while (reader.hasMoreChildren()) {
			reader.moveDown();

			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_NAME))
				name = reader.getValue();
			else if (nodeName.equals(KnowItConverter.TAG_KNOWIT)) {
				knowIt = (KnowIt) context.convertAnother(node, KnowIt.class);
			} else if (nodeName.equals(TAG_SUCCESSORS)) {
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
