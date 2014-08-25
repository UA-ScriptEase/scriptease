package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.io.XMLAttribute;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.ConditionalFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConditonalFragmentConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ConditionalFragment conditional = (ConditionalFragment) source;

		XMLAttribute.DATA.write(writer, conditional.getDirectiveText());

		// Write Sub Fragments
		context.convertAnother(conditional.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String data = XMLAttribute.DATA.read(reader);

		final List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();

		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<AbstractFragment>) context
					.convertAnother(null, ArrayList.class));
		}

		return new ConditionalFragment(data, subFragments);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ConditionalFragment.class);
	}
}
