package scriptease.gui.quests;

import scriptease.controller.io.converter.ComplexStoryComponentConverter;
import scriptease.model.StoryComponent;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the QuestPoint class to and from XML.
 * 
 * @author mfchurch
 */
public class QuestPointConverter extends ComplexStoryComponentConverter {
	public static final String TAG_QUESTPOINT = "QuestPoint";
	public static final String TAG_COMMITING = "Commiting";
	public static final String TAG_FAN_IN = "FanIn";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final QuestPoint questPoint = (QuestPoint) source;
		super.marshal(source, writer, context);

		// fan in
		writer.startNode(TAG_FAN_IN);
		writer.setValue(questPoint.getFanIn().toString());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final QuestPoint questPoint = (QuestPoint) super.unmarshal(reader,
				context);
		String fanIn = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_FAN_IN)) {
				fanIn = reader.getValue();
			} else if (nodeName.equals(TAG_COMMITING)) {
				/*
				 * This check left in for legacy file support. "Committing"
				 * used to be a property of quest points, where they would
				 * disable all of their sibling branches, but we've dumped that
				 * in favour of explicitly failing a quest point.
				 * 
				 * -remiller
				 */
			}
			reader.moveUp();
		}

		questPoint.setFanIn(new Integer(fanIn));
		return questPoint;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(QuestPoint.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new QuestPoint("");
	}
}
