package scriptease.controller.io.converter.storycomponent.behaviour;

import java.util.ArrayList;
import java.util.List;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.IndependentTask;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the IndependentTask {@link IndependentTask} to and from xml.
 * 
 * @author jyuen
 */
public class IndependentTaskConverter extends TaskConverter {

	public static final String TAG_INDEPENDENT_TASK = "IndependentTask";

	public static final String TAG_EFFECTS = "Effects";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final IndependentTask independentTask = (IndependentTask) source;
		super.marshal(source, writer, context);

		writer.startNode(TAG_EFFECTS);
		context.convertAnother(independentTask.getEffects());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final IndependentTask independentTask = (IndependentTask) super
				.unmarshal(reader, context);

		List<ScriptIt> effects = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_EFFECTS)) {
				effects = (List<ScriptIt>) context.convertAnother(
						independentTask, ArrayList.class);
			}

			reader.moveUp();
		}

		independentTask.setEffects(effects);

		return independentTask;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(IndependentTask.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new IndependentTask("");
	}
}
