package scriptease.controller.io.converter.graphnode;

import scriptease.controller.io.converter.KnowItConverter;
import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.SEGraph.nodes.KnowItNode;
import scriptease.model.atomic.KnowIt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @deprecated This needs to be removed. We have SEGraph now, which is more
 *             coder-friendly and does more things.
 */
public class KnowItNodeConverter extends GraphNodeConverter {
	public static final String TAG_KNOWIT_NODE = "KnowItNode";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final KnowItNode knowItNode = (KnowItNode) source;
		super.marshal(source, writer, context);

		// KnowIt
		writer.startNode(KnowItConverter.TAG_KNOWIT);
		context.convertAnother(knowItNode.getKnowIt());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		KnowItNode knowItNode = null;
		KnowIt knowIt = null;

		knowItNode = (KnowItNode) super.unmarshal(reader, context);

		// KnowIt
		reader.moveDown();
		knowIt = (KnowIt) context.convertAnother(knowItNode, KnowIt.class);
		reader.moveUp();
		knowItNode.setKnowIt(knowIt);

		return knowItNode;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(KnowItNode.class);
	}

	@Override
	protected GraphNode buildNode(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new KnowItNode(new KnowIt());
	}
}
