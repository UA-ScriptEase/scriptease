package scriptease.controller.io.converter.fragment;

import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FormatReferenceFragmentConverter implements Converter {
	private static final String REF_TAG = "ref";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final FormatReferenceFragment reference = (FormatReferenceFragment) source;

		// Ref Tag
		writer.addAttribute(REF_TAG, reference.getDirectiveText());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String ref;

		// Ref Tag
		ref = reader.getAttribute(REF_TAG);

		// Start vanilla
		FormatReferenceFragment reference = new FormatReferenceFragment(ref);

		return reference;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(FormatReferenceFragment.class);
	}
}
