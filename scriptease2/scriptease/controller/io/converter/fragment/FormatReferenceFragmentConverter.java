package scriptease.controller.io.converter.fragment;

import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.FormatReferenceType;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FormatReferenceFragmentConverter implements Converter {
	private static final String REF_TAG = "ref";
	private static final String DATA_TAG = "data";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final FormatReferenceFragment reference = (FormatReferenceFragment) source;

		writer.addAttribute(DATA_TAG, reference.getType().name());
		// Ref Tag
		writer.addAttribute(REF_TAG, reference.getDirectiveText());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String ref;
		final String data;

		// Ref Tag
		ref = reader.getAttribute(REF_TAG);
		data = reader.getAttribute(DATA_TAG);

		// Start vanilla
		final FormatReferenceFragment reference;

		if (data != null && !data.isEmpty())
			reference = new FormatReferenceFragment(ref,
					FormatReferenceType.valueOf(data.toUpperCase()));
		else
			reference = new FormatReferenceFragment(ref);

		return reference;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(FormatReferenceFragment.class);
	}
}
