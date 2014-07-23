package scriptease.controller.io.converter.storycomponent;

import scriptease.model.complex.ActivityIt;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

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
	protected ActivityIt buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library) {
		return new ActivityIt(library, "");
	}

	@Override
	public ActivityIt unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return (ActivityIt) super.unmarshal(reader, context);
	}
}
