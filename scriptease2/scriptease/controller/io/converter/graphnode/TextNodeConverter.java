package scriptease.controller.io.converter.graphnode;

import scriptease.controller.io.FileIO;
import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.SEGraph.nodes.TextNode;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @deprecated This needs to be removed. We have SEGraph now, which is more
 *             coder-friendly and does more things.
 */
public class TextNodeConverter extends GraphNodeConverter {
	public static final String TAG_TEXT_NODE = "TextNode";
	private static final String TAG_TEXT = "Text";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final TextNode textNode = (TextNode) source;
		super.marshal(source, writer, context);

		// Text
		writer.startNode(TAG_TEXT);
		writer.setValue(textNode.getText());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		TextNode textNode = null;
		String text = null;

		textNode = (TextNode) super.unmarshal(reader, context);

		// Text
		text = FileIO.readValue(reader, TAG_TEXT);
		textNode.setText(text);

		return textNode;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(TextNode.class);
	}

	@Override
	protected GraphNode buildNode(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new TextNode("");
	}
}
