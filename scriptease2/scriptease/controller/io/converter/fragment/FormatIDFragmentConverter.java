package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.Fragment;
import scriptease.translator.codegenerator.code.fragments.FormatDefinitionFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FormatIDFragmentConverter implements Converter {
	private static final String ID_TAG = "id";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final FormatDefinitionFragment format = (FormatDefinitionFragment) source;

		// Write the Fragment's ID
		writer.addAttribute(ID_TAG, format.getDirectiveText());

		// Write Sub Fragments
		context.convertAnother(format.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String id;
		final List<Fragment> subFragments;
		FormatDefinitionFragment format = null;

		// Read the Fragment's ID
		id = reader.getAttribute(ID_TAG);

		// Read sub fragments
		subFragments = new ArrayList<Fragment>();
		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<Fragment>) context.convertAnother(
					format, ArrayList.class));
		}

		format = new FormatDefinitionFragment(id, subFragments);
		return format;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(FormatDefinitionFragment.class);
	}
}
