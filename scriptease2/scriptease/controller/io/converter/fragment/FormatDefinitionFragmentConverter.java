package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.io.XMLAttribute;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.FormatDefinitionFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FormatDefinitionFragmentConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final FormatDefinitionFragment format = (FormatDefinitionFragment) source;

		XMLAttribute.ID.write(writer, format.getDirectiveText());

		// Write Sub Fragments
		context.convertAnother(format.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String id = XMLAttribute.ID.read(reader);
		final List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();

		// Read sub fragments
		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<AbstractFragment>) context
					.convertAnother(null, ArrayList.class));
		}

		return new FormatDefinitionFragment(id, subFragments);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(FormatDefinitionFragment.class);
	}
}
