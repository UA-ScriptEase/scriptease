package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ActivityIt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts {@link ActivityIt}s to/from XML.
 * 
 * @author jyuen
 */
public class ActivityItConverter extends ScriptItConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ActivityIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new ActivityIt("");
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		super.marshal(source, writer, context);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		ActivityIt activity;

		activity = (ActivityIt) super.unmarshal(reader, context);

		// TODO: Refactor this grossness.

		// Hack to get a clone returned for a activity if we are loading it in
		// the Story, otherwise just return the activity. This is done because
		// KnowItBindingUninitialized don't reference the correct slot otherwise
		try {
			return activity.clone();
		} catch (Exception e) {
			return activity;
		}
	}
}
