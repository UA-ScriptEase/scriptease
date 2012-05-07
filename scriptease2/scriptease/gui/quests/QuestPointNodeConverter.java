package scriptease.gui.quests;

import scriptease.controller.io.converter.graphnode.GraphNodeConverter;
import scriptease.gui.graph.nodes.GraphNode;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the QuestPointNode class to and from XML for use in file
 * input/output.
 * 
 * @author mfchurch
 * 
 */
public class QuestPointNodeConverter extends GraphNodeConverter {
	public static final String TAG_QUESTPOINT_NODE = "QuestPointNode";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final QuestPointNode questNode = (QuestPointNode) source;
		super.marshal(source, writer, context);

		// QuestPointNode
		writer.startNode(QuestPointConverter.TAG_QUESTPOINT);
		context.convertAnother(questNode.getQuestPoint());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		QuestPointNode questPointNode = null;
		QuestPoint questPoint = null;

		questPointNode = (QuestPointNode) super.unmarshal(reader, context);

		// QuestPoint
		reader.moveDown();
		questPoint = (QuestPoint) context.convertAnother(questPointNode,
				QuestPoint.class);
		reader.moveUp();
		questPointNode.setQuestPoint(questPoint);

		return questPointNode;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(QuestPointNode.class);
	}

	@Override
	protected GraphNode buildNode(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new QuestPointNode();
	}
}
