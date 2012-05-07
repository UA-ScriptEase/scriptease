package scriptease.gui.quests;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.io.converter.graphnode.GraphNodeConverter;
import scriptease.gui.graph.nodes.GraphNode;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * QuestNodeConverter converts QuestNodes to and from XML
 * 
 * @author mfchurch
 * @author graves 
 */
public class QuestNodeConverter extends GraphNodeConverter {
	public static final String TAG_QUEST_NODE = "QuestNode";
	public static final String TAG_COLLAPSED = "Collapsed";
	private static final String TAG_NAME = "Name";
	private static final String TAG_START = "StartPoint";
	private static final String TAG_END = "EndPoint";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final QuestNode questNode = (QuestNode) source;
		
		// GraphNodeConverter
		super.marshal(source, writer, context);

		// Collapsed
		writer.startNode(TAG_COLLAPSED);
		writer.setValue(questNode.isCollapsed() ? "true":"false");
		writer.endNode();

		// Name
		writer.startNode(TAG_NAME);
		writer.setValue(questNode.getName());
		writer.endNode();

		// StartPoint
		writer.startNode(TAG_START);
		final GraphNode startNode = questNode.getStartPoint();
		startNode.process(new AbstractNoOpGraphNodeVisitor() {

			@Override
			public void processQuestPointNode(QuestPointNode questPointNode) {
				writer.startNode(QuestPointNodeConverter.TAG_QUESTPOINT_NODE);
				context.convertAnother(questPointNode);
				writer.endNode();
			}

			@Override
			public void processQuestNode(QuestNode questNode) {
				writer.startNode(QuestNodeConverter.TAG_QUEST_NODE);
				context.convertAnother(questNode);
				writer.endNode();
			}
		});
		writer.endNode();

		// EndPoint
		writer.startNode(TAG_END);
		final GraphNode endNode = questNode.getEndPoint();
		endNode.process(new AbstractNoOpGraphNodeVisitor() {

			@Override
			public void processQuestPointNode(QuestPointNode questPointNode) {
				writer.startNode(QuestPointNodeConverter.TAG_QUESTPOINT_NODE);
				context.convertAnother(questPointNode);
				writer.endNode();
			}

			@Override
			public void processQuestNode(QuestNode questNode) {
				writer.startNode(QuestNodeConverter.TAG_QUEST_NODE);
				context.convertAnother(questNode);
				writer.endNode();
			}
		});
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		QuestNode questNode = null;
		String collapsed = null;
		String name = null;
		GraphNode start = null;
		GraphNode end = null;

		questNode = (QuestNode) super.unmarshal(reader, context);

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();
			// can't think of a better way to handle this
			if (nodeName.equals(TAG_START)) {
				reader.moveDown();
				start = readQuestNodeorQuestPointNode(reader, context);
				reader.moveUp();
			} else if (nodeName.equals(TAG_END)) {
				reader.moveDown();
				end = readQuestNodeorQuestPointNode(reader, context);
				reader.moveUp();
			} else if (nodeName.equals(TAG_NAME)) {
				name = reader.getValue();
			} else if (nodeName.equals(TAG_COLLAPSED)) {
				collapsed = reader.getValue();
			}
			reader.moveUp();
		}

		questNode.setStartPoint(start);
		questNode.setEndPoint(end);
		questNode.setName(name);
		questNode.setCollapsed(collapsed.equalsIgnoreCase("true"));
		return questNode;
	}

	/**
	 * Reads either a QuestNode or a QuestPointNode and returns it
	 * 
	 * @param reader
	 * @param context
	 * @return
	 */
	private GraphNode readQuestNodeorQuestPointNode(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		if (reader.getNodeName().equals(
				QuestPointNodeConverter.TAG_QUESTPOINT_NODE))
			return (GraphNode) context.convertAnother(null,
					QuestPointNode.class);
		else if (reader.getNodeName().equals(QuestNodeConverter.TAG_QUEST_NODE))
			return (GraphNode) context.convertAnother(null, QuestNode.class);
		else {
			String errorMessage = "Unable to read Quest or QuestPoint";
			System.err.println(errorMessage);
			throw new IllegalStateException(errorMessage);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(QuestNode.class);
	}

	@Override
	protected GraphNode buildNode(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new QuestNode();
	}
}
